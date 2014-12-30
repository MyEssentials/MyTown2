package mytown.datasource;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import mytown.config.Config;
import mytown.core.utils.config.ConfigProperty;
import mytown.core.utils.teleport.Teleport;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

// TODO Check connection for each command and error out if connection doesn't check out
// TODO Run DB writes (and maybe reads?) on a separate thread
// TODO Ensure thread safety!

/**
 * @author Joe Goett
 */
public abstract class MyTownDatasource_SQL extends MyTownDatasource {
    @ConfigProperty(category = "datasource.sql", comment = "The prefix of each of the tables. <prefix>tablename")
    protected String prefix = "";

    @ConfigProperty(category = "datasource.sql", comment = "User defined properties to be passed to the connection.\nFormat: key=value;key=value...")
    protected String[] userProperties = {};

    protected String AUTO_INCREMENT = ""; // Only because SQLite and MySQL are different >.>

    protected Properties dbProperties = new Properties();
    protected String dsn = "";
    protected Connection conn = null;

    public boolean initialize() {
        setup();

        // Add user-defined properties
        for (String prop : userProperties) {
            String[] pair = prop.split("=");
            if (pair.length < 2) continue;
            dbProperties.put(pair[0], pair[1]);
        }

        // Register driver if needed
        try {
            Driver driver = (Driver) Class.forName(getDriver()).newInstance();
            DriverManager.registerDriver(driver);
        } catch (Exception ex) {
            log.error("Driver error", ex);
        }

        // Attempt connection
        if (createConnection()) {
            log.info("Connected to database");
        } else {
            log.error("Failed to connect to the database!");
            return false;
        }

        // Run updates
        try {
            setupUpdates();
            doUpdates();
        } catch (SQLException e) {
            return false;
        }

        // Initialization was successful! Yay!
        return true;
    }

    // TODO Change this to checkConnection() and call on each command?
    protected boolean createConnection() {
        try {
            if (conn == null || conn.isClosed() || (!Config.dbType.equalsIgnoreCase("sqlite") && conn.isValid(1))) {
                if (conn != null && !conn.isClosed()) {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                    } // Ignore since we are just closing an old connection
                    conn = null;
                }

                conn = DriverManager.getConnection(dsn, dbProperties);

                if (conn == null || conn.isClosed()) {
                    return false;
                }
            }
            return true;
        } catch (SQLException ex) {
            log.error("Failed to get SQL connection! %s", ex, dsn);
        }
        return false;
    }

    public Connection getConnection() {
        return conn;
    }

    /**
     * Sets up the DSN and Properties for this Datasource
     */
    protected abstract void setup();

    /**
     * Returns the class of the Driver being used
     *
     * @return
     */
    protected abstract String getDriver();

    /* ----- Read ----- */

    @Override
    public boolean loadAll() {
        return super.loadAll() && loadRankPermissions() && loadResidentsToTowns() && loadTownsToNations() && loadResidentsToPlots();
    }

    @Override
    protected boolean loadWorlds() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "Worlds", true);
            ResultSet rs = s.executeQuery();

            while (rs.next()) {
                MyTownUniverse.getInstance().addWorld(rs.getInt("dim"));
            }

        } catch (SQLException e) {
            log.error("Failed to load worlds from the database!");
            return false;
        }

        for (World world : MinecraftServer.getServer().worldServers) {
            if (!MyTownUniverse.getInstance().hasWorld(world.provider.dimensionId)) {
                saveWorld(world.provider.dimensionId);
            }
        }
        for (int dim : MyTownUniverse.getInstance().getWorldsList()) {
            if (DimensionManager.getWorld(dim) == null) {
                deleteWorld(dim);
            }
        }

        return true;
    }

    @Override
    protected boolean loadTowns() {
        try {
            PreparedStatement loadTownsStatement = prepare("SELECT * FROM " + prefix + "Towns", true);
            ResultSet rs = loadTownsStatement.executeQuery();

            while (rs.next()) {
                Town town;
                if (rs.getBoolean("isAdminTown")) {
                    town = new AdminTown(rs.getString("name"));
                } else {
                    town = new Town(rs.getString("name"));
                }
                // TODO: Maybe spawn is not needed when town is an admintown?
                town.setSpawn(new Teleport(rs.getInt("spawnDim"), rs.getFloat("spawnX"), rs.getFloat("spawnY"), rs.getFloat("spawnZ"), rs.getFloat("cameraYaw"), rs.getFloat("cameraPitch")));
                town.setExtraBlocks(rs.getInt("extraBlocks"));
                town.setMaxPlots(rs.getInt("maxPlots"));

                MyTownUniverse.getInstance().addTown(town);
            }
        } catch (SQLException e) {
            log.error("Failed to load Towns!", e);
            return false;
        }

        return true;
    }

    @Override
    protected boolean loadBlocks() {
        try {
            PreparedStatement loadBlocksStatement = prepare("SELECT * FROM " + prefix + "Blocks", true);
            ResultSet rs = loadBlocksStatement.executeQuery();

            while (rs.next()) {
                Town town = MyTownUniverse.getInstance().getTown(rs.getString("townName"));
                if (town == null) {
                    // Deleting row if town no longer exists.
                    log.error("Failed to load Block (%s, %s, %s) due to missing Town (%s)", rs.getInt("dim"), rs.getInt("x"), rs.getInt("z"), rs.getString("townName"));
                    /*
                    PreparedStatement deleteStatement = prepare("DELETE FROM Blocks WHERE dim=? AND x=? AND z=?", false);
                    deleteStatement.setInt(1, rs.getInt("dim"));
                    deleteStatement.setInt(2, rs.getInt("x"));
                    deleteStatement.setInt(3, rs.getInt("z"));
                    deleteStatement.executeUpdate();
                    log.info("Block deleted.");
                    */
                    continue;
                }
                TownBlock block = new TownBlock(rs.getInt("dim"), rs.getInt("x"), rs.getInt("z"), town);
                MyTownUniverse.getInstance().addTownBlock(block);
                block.getTown().addBlock(block);
            }
        } catch (SQLException e) {
            log.error("Failed to load blocks!", e);
            return false;
        }

        return true;
    }

    @Override
    protected boolean loadRanks() {
        try {
            PreparedStatement loadRanksStatement = prepare("SELECT * FROM " + prefix + "Ranks", true);
            ResultSet rs = loadRanksStatement.executeQuery();
            while (rs.next()) {
                Town town = MyTownUniverse.getInstance().getTown(rs.getString("townName"));
                if (town == null) {
                    // Deleting row if town no longer exists.

                    log.error("Failed to load Rank (%s) due to missing Town (%s)", rs.getString("name"), rs.getString("townName"));
                    /*
                    PreparedStatement deleteStatement = prepare("DELETE FROM Ranks WHERE name=? AND townName=?", false);
                    deleteStatement.setString(1, rs.getString("name"));
                    deleteStatement.setString(2, rs.getString("townName"));
                    deleteStatement.executeUpdate();
                    log.info("Rank deleted.");
                    */
                    continue;
                }

                Rank rank = new Rank(rs.getString("name"), town);
                log.debug("Loading Rank %s for Town %s", rank.getName(), town.getName());

                // Adding it before
                if (rs.getBoolean("isDefault")) {
                    town.setDefaultRank(rank);
                }

                MyTownUniverse.getInstance().addRank(rank);
                rank.getTown().addRank(rank);
            }
        } catch (SQLException e) {
            log.error("Failed to load a rank!", e);
        }
        return true;
    }

    protected boolean loadRankPermissions() {
        try {
            PreparedStatement loadRankPermsStatement = prepare("SELECT * FROM " + prefix + "RankPermissions", true);
            ResultSet rs = loadRankPermsStatement.executeQuery();
            while(rs.next()) {
                Town t = MyTownUniverse.getInstance().getTown(rs.getString("townName"));
                if (t == null) {
                    log.error("Failed to load RankPermission due to missing Town %s!", rs.getString("townName"));
                    continue;
                }
                Rank rank = getRank(rs.getString("rank"), t);
                if (rank == null) {
                    log.error("Failed to load RankPermission due to missing Rank %s!", rs.getString("rank"));
                    continue;
                }
                rank.addPermission(rs.getString("node"));
            }
        } catch (SQLException e) {
            log.error("Failed to load RankPermissions!", e);
            return false;
        }
        return true;
    }

    @Override
    protected boolean loadResidents() {
        try {
            PreparedStatement loadResidentsStatement = prepare("SELECT * FROM " + prefix + "Residents", true);
            ResultSet rs = loadResidentsStatement.executeQuery();

            while (rs.next()) {
                Resident res = new Resident(rs.getString("uuid"), rs.getString("name"), rs.getLong("joined"), rs.getLong("lastOnline"), rs.getInt("extraBlocks"));
                MyTownUniverse.getInstance().addResident(res);
            }
        } catch (SQLException e) {
            log.error("Failed to load Residents!", e);
            return false;
        }

        return true;
    }

    @Override
    protected boolean loadPlots() {
        try {
            PreparedStatement loadPlotsStatement = prepare("SELECT * FROM " + prefix + "Plots", true);
            ResultSet rs = loadPlotsStatement.executeQuery();

            while (rs.next()) {
                Town town = MyTownUniverse.getInstance().getTown(rs.getString("townName"));
                if (town == null) {
                    log.error("Failed to load Plot (%s) due to missing Town (%s)", rs.getString("name"), rs.getString("townName"));
                    continue; // TODO Should I just return out?
                }
                Plot plot = new Plot(rs.getString("name"), town, rs.getInt("dim"), rs.getInt("x1"), rs.getInt("y1"), rs.getInt("z1"), rs.getInt("x2"), rs.getInt("y2"), rs.getInt("z2"));
                plot.setDb_ID(rs.getInt("ID"));
                MyTownUniverse.getInstance().addPlot(plot);
                plot.getTown().addPlot(plot);
            }
        } catch (SQLException e) {
            log.error("Failed to load Plots!", e);
            return false;
        }

        return true;
    }

    @Override
    protected boolean loadNations() {
        try {
            PreparedStatement loadNationsStatement = prepare("SELECT * FROM " + prefix + "Nations", true);
            ResultSet rs = loadNationsStatement.executeQuery();

            while (rs.next()) {
                Nation nation = new Nation(rs.getString("name"));
                MyTownUniverse.getInstance().addNation(nation);
            }
        } catch (SQLException e) {
            log.error("Failed to load Nations!", e);
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean loadTownFlags() {
        try {
            PreparedStatement loadFlagsStatement = prepare("SELECT * FROM " + prefix + "TownFlags", true);
            ResultSet rs = loadFlagsStatement.executeQuery();

            while (rs.next()) {
                String townName = rs.getString("townName");
                String flagName = rs.getString("name");

                Gson gson = new GsonBuilder().create();
                try {
                    FlagType type = FlagType.valueOf(flagName);
                    Flag flag = new Flag(type, gson.fromJson(rs.getString("serializedValue"), FlagType.valueOf(flagName).getType()));

                    Town town = MyTownUniverse.getInstance().getTownsMap().get(townName);
                    if (town != null) {
                        if (flag.flagType.shouldLoad())
                            town.addFlag(flag);
                    } else {
                        log.error("Failed to load flag " + flagName + " because the town given was invalid!");
                    }
                } catch (IllegalArgumentException ex) {
                    log.error("Flag " + flagName + " does no longer exist... will be deleted shortly from the database.");
                    PreparedStatement removeFlag = prepare("DELETE FROM " + prefix + "TownFlags WHERE townName=? AND name=?", true);
                    removeFlag.setString(1, townName);
                    removeFlag.setString(2, flagName);
                    removeFlag.executeUpdate();
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean loadPlotFlags() {
        try {
            PreparedStatement loadFlagsStatement = prepare("SELECT * FROM " + prefix + "PlotFlags", true);
            ResultSet rs = loadFlagsStatement.executeQuery();

            while (rs.next()) {
                int plotID = rs.getInt("plotID");
                String flagName = rs.getString("name");

                Gson gson = new GsonBuilder().create();
                try {
                    FlagType type = FlagType.valueOf(flagName);
                    Flag flag = new Flag(type, gson.fromJson(rs.getString("serializedValue"), FlagType.valueOf(flagName).getType()));

                    Plot plot = MyTownUniverse.getInstance().getPlot(plotID);
                    if (plot != null) {
                        if (flag.flagType.shouldLoad())
                            plot.addFlag(flag);
                    } else {
                        log.error("Failed to load flag " + flagName + " because the town given was invalid!");
                    }
                } catch (IllegalArgumentException ex) {
                    log.error("Flag " + flagName + " does no longer exist. Deleting from database.");
                    PreparedStatement removeFlag = prepare("DELETE FROM " + prefix + "PlotFlags WHERE plotID=? AND name=?", true);
                    removeFlag.setInt(1, plotID);
                    removeFlag.setString(2, flagName);
                    removeFlag.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Loads the links between Residents and Towns
     *
     * @return
     */
    protected boolean loadResidentsToTowns() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "ResidentsToTowns", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Resident res = MyTownUniverse.getInstance().getResident(rs.getString("resident"));
                Town town = MyTownUniverse.getInstance().getTown(rs.getString("town"));
                if (res == null || town == null) {
                    log.error("Failed to link Resident %s to Town %s. Skipping!", rs.getString("resident"), rs.getString("town"));
                    continue;
                }
                Rank rank = MyTownUniverse.getInstance().getRank(String.format("%s;%s", town.getName(), rs.getString("rank")));
                if (rank == null) {
                    log.error("Failed to link Resident %s to Town %s because of unknown Rank %s. Skipping!", rs.getString("resident"), rs.getString("town"), rs.getString("rank"));
                    continue;
                }
                town.addResident(res, rank);
                res.addTown(town);
            }
        } catch (SQLException e) {
            log.error("Failed to link Residents to Towns!", e);
            return false;
        }

        return true;
    }

    @Override
    protected boolean loadBlockWhitelists() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "BlockWhitelists", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                // plotID will be 0 if it's a town's whitelist
                BlockWhitelist bw = new BlockWhitelist(rs.getInt("dim"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), FlagType.valueOf(rs.getString("flagName")));
                bw.setDb_ID(rs.getInt("ID"));
                Town town = MyTownUniverse.getInstance().getTownsMap().get(rs.getString("townName"));
                // This can't be null
                town.addBlockWhitelist(bw);
            }

        } catch (SQLException e) {
            log.error("Failed to load a Block whitelist");
            return false;
        }
        return true;
    }

    @Override
    protected boolean loadSelectedTowns() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "SelectedTown", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Resident res = MyTownUniverse.getInstance().getResidentsMap().get(rs.getString("resident"));
                Town town = MyTownUniverse.getInstance().getTownsMap().get(rs.getString("townName"));
                res.selectTown(town);
            }
        } catch (SQLException e) {
            log.error("Failed to load a town selection.");
            return false;
        }
        return true;
    }

    /**
     * Loads the links between Towns and Nations
     *
     * @return
     */
    protected boolean loadTownsToNations() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "TownsToNations", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Town town = MyTownUniverse.getInstance().getTown("");
                Nation nation = MyTownUniverse.getInstance().getNation("");
                if (town == null || nation == null) {
                    log.error("Failed to link Town %s to Nation %s. Skipping!", rs.getString("town"), rs.getString("nation"));
                    continue;
                }
                nation.addTown(town);
                nation.promoteTown(town, Nation.Rank.parse(rs.getString("rank")));
                town.setNation(nation);
            }
        } catch (SQLException e) {
            log.error("Failed to link Towns to Nations!", e);
            return false;
        }

        return true;
    }

    protected boolean loadResidentsToPlots() {
        try {
            PreparedStatement loadStatement = prepare("SELECT * FROM " + prefix + "ResidentsToPlots", true);
            ResultSet rs = loadStatement.executeQuery();

            while (rs.next()) {
                Plot plot = MyTownUniverse.getInstance().getPlotsMap().get(rs.getInt("plotID"));
                Resident res = MyTownUniverse.getInstance().getResidentsMap().get(rs.getString("resident"));

                if (rs.getBoolean("isOwner"))
                    plot.addOwner(res);

                plot.addResident(res);
            }
        } catch (SQLException e) {
            log.error("Failed to link Residents to Plots");
            return false;
        }
        return true;
    }

    @Override
    protected boolean loadFriends() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "Friends", true);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                Resident res1 = MyTownUniverse.getInstance().getResidentsMap().get(rs.getString("resident1"));
                Resident res2 = MyTownUniverse.getInstance().getResidentsMap().get(rs.getString("resident2"));

                res1.addFriend(res2);
                res2.addFriend(res1);
            }
        } catch (SQLException e) {
            log.error("Failed to load friends.");
            return false;
        }
        return true;
    }

    @Override
    protected boolean loadFriendRequests() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "FriendRequests", true);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                Resident res1 = MyTownUniverse.getInstance().getResidentsMap().get(rs.getString("resident"));
                Resident res2 = MyTownUniverse.getInstance().getResidentsMap().get(rs.getString("residentTarget"));

                res2.addFriendRequest(res1);
            }
        } catch (SQLException e) {
            log.error("Failed to load friend requests.");
            return false;
        }
        return true;
    }

    @Override
    protected boolean loadTownInvites() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "TownInvites", true);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                Resident res = MyTownUniverse.getInstance().getResident(rs.getString("resident"));
                Town town = MyTownUniverse.getInstance().getTown(rs.getString("townName"));
                res.addInvite(town);
            }
        } catch (SQLException e) {
            log.error("Failed to load town invites.");
            return false;
        }

        return true;
    }

    /* ----- Save ----- */

    @Override
    public boolean saveTown(Town town) {
        log.debug("Saving Town %s", town.getName());
        try {
            if (MyTownUniverse.getInstance().hasTown(town)) { // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "Towns SET name=?, spawnDim=?, spawnX=?, spawnY=?, spawnZ=?, cameraYaw=?, cameraPitch=?, extraBlocks=?, maxPlots=? WHERE name=?", true);
                updateStatement.setString(1, town.getName());
                updateStatement.setInt(2, town.getSpawn().getDim());
                updateStatement.setFloat(3, town.getSpawn().getX());
                updateStatement.setFloat(4, town.getSpawn().getY());
                updateStatement.setFloat(5, town.getSpawn().getZ());
                updateStatement.setFloat(6, town.getSpawn().getYaw());
                updateStatement.setFloat(7, town.getSpawn().getPitch());
                updateStatement.setInt(8, town.getExtraBlocks());
                updateStatement.setInt(9, town.getMaxPlots());

                if (town.getOldName() == null)
                    updateStatement.setString(10, town.getName());
                else
                    updateStatement.setString(10, town.getOldName());

                updateStatement.executeUpdate();

                // Need to move the Town in the map from the old name to the new
                if (town.getOldName() != null) {
                    MyTownUniverse.getInstance().removeTown(town);
                    // This updates the name
                    MyTownUniverse.getInstance().addTown(town);
                }
                town.resetOldName();
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Towns (name, spawnDim, spawnX, spawnY, spawnZ, cameraYaw, cameraPitch, isAdminTown, extraBlocks, maxPlots) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", true);
                insertStatement.setString(1, town.getName());
                insertStatement.setInt(2, town.getSpawn().getDim());
                insertStatement.setFloat(3, town.getSpawn().getX());
                insertStatement.setFloat(4, town.getSpawn().getY());
                insertStatement.setFloat(5, town.getSpawn().getZ());
                insertStatement.setFloat(6, town.getSpawn().getYaw());
                insertStatement.setFloat(7, town.getSpawn().getPitch());
                insertStatement.setBoolean(8, town instanceof AdminTown);
                insertStatement.setInt(9, town.getExtraBlocks());
                insertStatement.setInt(10, town.getMaxPlots());

                insertStatement.executeUpdate();

                // Put the Town in the Map
                MyTownUniverse.getInstance().addTown(town);
            }
        } catch (SQLException e) {
            log.error("Failed to save Town %s!", e, town.getName());
            return false;
        }

        return true;
    }

    @Override
    public boolean saveBlock(TownBlock block) {
        log.debug("Saving TownBlock %s", block.getKey());
        try {
            if (MyTownUniverse.getInstance().hasTownBlock(block)) { // Update
                // TODO Update Block (If needed?)
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Blocks (dim, x, z, townName) VALUES (?, ?, ?, ?)", true);
                insertStatement.setInt(1, block.getDim());
                insertStatement.setInt(2, block.getX());
                insertStatement.setInt(3, block.getZ());
                insertStatement.setString(4, block.getTown().getName());
                insertStatement.executeUpdate();

                // Put the Block in the Map
                MyTownUniverse.getInstance().addTownBlock(block);

                block.getTown().addBlock(block);
            }
        } catch (SQLException e) {
            log.error("Failed to save Block %s!", e, block.getKey());
            return false;
        }

        return true;
    }

    @Override
    public boolean saveRank(Rank rank, boolean isDefault) { // TODO Insert any new permissions to the RankPermission table
        log.debug("Saving Rank %s", rank.getKey());
        try {
            if (MyTownUniverse.getInstance().hasRank(rank)) { // Update
                // TODO Update
            } else { // Insert
                try {
                    getConnection().setAutoCommit(false);

                    PreparedStatement insertRankStatement = prepare("INSERT INTO " + prefix + "Ranks (name, townName, isDefault) VALUES(?, ?, ?)", true);
                    insertRankStatement.setString(1, rank.getName());
                    insertRankStatement.setString(2, rank.getTown().getName());
                    insertRankStatement.setBoolean(3, isDefault);
                    insertRankStatement.executeUpdate();

                    if (rank.getPermissions().size() > 0) {

                        PreparedStatement insertRankPermStatement = prepare("INSERT INTO " + prefix + "RankPermissions(node, rank, townName) VALUES(?, ?, ?)", true);
                        for (String perm : rank.getPermissions()) {
                            insertRankPermStatement.setString(1, perm);
                            insertRankPermStatement.setString(2, rank.getName());
                            insertRankPermStatement.setString(3, rank.getTown().getName());
                            insertRankPermStatement.addBatch();
                        }
                        insertRankPermStatement.executeBatch();
                    }

                    // Put the Rank in the Map
                    MyTownUniverse.getInstance().addRank(rank);
                    rank.getTown().addRank(rank);

                    if (isDefault)
                        rank.getTown().setDefaultRank(rank);
                } catch (SQLException e) {
                    log.error("Failed to insert Rank %s", rank.getKey());
                    getConnection().rollback();
                    e.printStackTrace();
                    return false;
                } finally {
                    getConnection().setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to save Rank %s!", rank.getKey());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean addRankPermission(Rank rank, String perm) {
        log.debug("Saving RankPermission %s for Rank %s", perm, rank.getKey());
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "RankPermissions (node, rank) VALUES(?, ?)", true);
            s.setString(1, perm);
            s.setString(2, rank.getName());
            s.execute();
        } catch (SQLException e) {
            log.error("Failed to add permission (%s) to Rank (%s)", e, perm, rank.getName());
            return false;
        }
        return true;
    }

    @Override
    public boolean saveResident(Resident resident) {
        log.debug("Saving Resident %s (%s)", resident.getUUID(), resident.getPlayerName());
        try {
            if (MyTownUniverse.getInstance().hasResident(resident)) { // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "Residents SET name=?, lastOnline=?, extraBlocks=? WHERE uuid=?", true);
                updateStatement.setString(1, resident.getPlayerName());
                updateStatement.setLong(2, resident.getLastOnline().getTime() / 1000L); // Stupid hack...
                updateStatement.setInt(3, resident.getExtraBlocks());
                updateStatement.setString(4, resident.getUUID().toString());
                updateStatement.executeUpdate();
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Residents (uuid, name, joined, lastOnline, extraBlocks) VALUES(?, ?, ?, ?, ?)", true);
                insertStatement.setString(1, resident.getUUID().toString());
                insertStatement.setString(2, resident.getPlayerName());
                insertStatement.setLong(3, resident.getJoinDate().getTime() / 1000L); // Stupid hack...
                insertStatement.setLong(4, resident.getLastOnline().getTime() / 1000L); // Stupid hack...
                insertStatement.setInt(5, resident.getExtraBlocks());
                insertStatement.executeUpdate();

                // Put the Resident in the Map
                MyTownUniverse.getInstance().addResident(resident);
            }
        } catch (SQLException e) {
            log.error("Failed to save resident %s!", e, resident.getUUID());
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean savePlot(Plot plot) {
        log.debug("Saving Plot %s for Town %s", plot.getKey(), plot.getTown().getName());
        try {
            if (MyTownUniverse.getInstance().hasPlot(plot)) { // Update
                PreparedStatement statement = prepare("UPDATE " + prefix + "Plots SET name=?, dim=?, x1=?, y1=?, z1=?, x2=?, y2=?, z2=? WHERE ID=?", true);
                statement.setString(1, plot.getName());
                statement.setInt(2, plot.getDim());
                statement.setInt(3, plot.getStartX());
                statement.setInt(4, plot.getStartY());
                statement.setInt(5, plot.getStartZ());
                statement.setInt(6, plot.getEndX());
                statement.setInt(7, plot.getEndY());
                statement.setInt(8, plot.getEndZ());
                statement.setInt(9, plot.getDb_ID());
                statement.executeUpdate();
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Plots (name, dim, x1, y1, z1, x2, y2, z2, townName) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", true);
                insertStatement.setString(1, plot.getName());
                insertStatement.setInt(2, plot.getDim());
                insertStatement.setInt(3, plot.getStartX());
                insertStatement.setInt(4, plot.getStartY());
                insertStatement.setInt(5, plot.getStartZ());
                insertStatement.setInt(6, plot.getEndX());
                insertStatement.setInt(7, plot.getEndY());
                insertStatement.setInt(8, plot.getEndZ());
                insertStatement.setString(9, plot.getTown().getName());
                insertStatement.executeUpdate();

                ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                if (generatedKeys.next())
                    plot.setDb_ID(generatedKeys.getInt(1));
                log.info("Saved plot with ID " + generatedKeys.getInt(1));
                for (Flag flag : plot.getTown().getFlags()) {
                    if (!flag.flagType.isTownOnly())
                        saveFlag(new Flag(flag.flagType, flag.getValue()), plot);
                }

                // Put the Plot in the Map
                MyTownUniverse.getInstance().addPlot(plot);
                plot.getTown().addPlot(plot);
            }
        } catch (SQLException e) {
            log.error("Failed to save Plot %s!", e, plot.getKey());
            return false;
        }

        return true;
    }

    @Override
    public boolean saveNation(Nation nation) { // TODO Link any new Towns to the given Nation
        log.debug("Saving Nation %s", nation.getName());
        try {
            if (MyTownUniverse.getInstance().hasNation(nation)) { // Update
                // TODO Update Nation (If needed?)
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Nations (name) VALUES(?)", true);
                insertStatement.setString(1, nation.getName());
                insertStatement.executeUpdate();

                // Put the Nation in the Map
                MyTownUniverse.getInstance().addNation(nation);
            }
        } catch (SQLException e) {
            log.error("Failed to save Nation %s!", nation.getName());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean saveFlag(Flag flag, Plot plot) {
        if (!flag.flagType.shouldLoad()) return false;
        log.debug("Saving Flag %s for Plot %s", flag.flagType.name(), plot.getKey());
        try {
            if (plot.hasFlag(flag.flagType)) {
                // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "PlotFlags SET serializedValue=? WHERE plotID=? AND name=?", true);
                updateStatement.setString(1, flag.serializeValue());
                updateStatement.setInt(2, plot.getDb_ID());
                updateStatement.setString(3, flag.flagType.toString());
                updateStatement.executeUpdate();


            } else {
                // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "PlotFlags(name, serializedValue, plotID) VALUES(?, ?, ?)", true);
                insertStatement.setString(1, flag.flagType.toString());
                insertStatement.setString(2, flag.serializeValue());
                insertStatement.setInt(3, plot.getDb_ID());
                insertStatement.executeUpdate();

                plot.addFlag(flag);
            }
        } catch (SQLException e) {
            log.error("Failed to save Flag %s!", flag.flagType.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean saveFlag(Flag flag, Town town) {
        if (!flag.flagType.shouldLoad()) return false;
        log.debug("Saving Flag %s for Town %s", flag.flagType.name(), town.getName());
        try {
            if (town.hasFlag(flag.flagType)) {
                // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "TownFlags SET serializedValue=? WHERE townName=? AND name=?", true);
                updateStatement.setString(1, flag.serializeValue());
                updateStatement.setString(2, town.getName());
                updateStatement.setString(3, flag.flagType.toString());
                updateStatement.executeUpdate();

            } else {
                // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "TownFlags(name,  serializedValue, townName) VALUES(?, ?, ?)", true);
                insertStatement.setString(1, flag.flagType.toString());
                insertStatement.setString(2, flag.serializeValue());
                insertStatement.setString(3, town.getName());
                insertStatement.executeUpdate();

                town.addFlag(flag);
            }
        } catch (SQLException e) {
            log.error("Failed to save Flag %s!", flag.flagType.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //Why?
    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public boolean saveBlockWhitelist(BlockWhitelist bw, Town town) {
        try {
            if (!town.hasBlockWhitelist(bw)) {
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "BlockWhitelists(dim, x, y, z, flagName, townName) VALUES(?, ?, ?, ?, ?, ?)", true);
                insertStatement.setInt(1, bw.dim);
                insertStatement.setInt(2, bw.x);
                insertStatement.setInt(3, bw.y);
                insertStatement.setInt(4, bw.z);
                insertStatement.setString(5, bw.getFlagType().toString());
                insertStatement.setString(6, town.getName());

                insertStatement.executeUpdate();

                ResultSet keys = insertStatement.getGeneratedKeys();
                if (keys.next())
                    bw.setDb_ID(keys.getInt(1));

                town.addBlockWhitelist(bw);
            }
            // NO update since ID can't change
        } catch (SQLException e) {
            log.error("Failed to save a Block Whitelist!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean saveSelectedTown(Resident res, Town town) {
        try {
            if (res.selectedTown == null) {
                PreparedStatement statement = prepare("INSERT INTO " + prefix + "SelectedTown(resident, townName) VALUES(?, ?)", true);
                statement.setString(1, res.getUUID().toString());
                statement.setString(2, town.getName());
                statement.executeUpdate();
            } else {
                PreparedStatement statement = prepare("UPDATE " + prefix + "SelectedTown SET townName=? WHERE resident=?", true);
                statement.setString(1, town.getName());
                statement.setString(2, res.getUUID().toString());
                statement.executeUpdate();
            }
            res.selectTown(town);

        } catch (SQLException e) {
            log.error("Failed to save a town selection!");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean saveFriendLink(Resident res1, Resident res2) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "Friends(resident1, resident2) VALUES(?, ?)", true);
            s.setString(1, res1.getUUID().toString());
            s.setString(2, res2.getUUID().toString());
            s.executeUpdate();

            /*
            Adding them BEFORE saving, only when a friend request is accepted
            res1.addFriend(res2);
            res2.addFriend(res1);
            */
        } catch (SQLException e) {
            log.error("Failed to save friend link between " + res1.getPlayerName() + " and " + res2.getPlayerName());
            return false;
        }
        return true;
    }

    @Override
    public boolean saveFriendRequest(Resident res1, Resident res2) {
        try {
            if (res2.addFriendRequest(res1)) {
                PreparedStatement s = prepare("INSERT INTO " + prefix + "FriendRequests(resident, residentTarget) VALUES(?, ?)", true);
                s.setString(1, res1.getUUID().toString());
                s.setString(2, res2.getUUID().toString());
                s.executeUpdate();
            } else {
                return false;
            }
        } catch (SQLException e) {
            log.error("Failed to save friend request from " + res1.getPlayerName() + " to " + res2.getPlayerName());
            return false;
        }
        return true;
    }

    @Override
    public boolean saveTownInvite(Resident res, Town town) {
        try {
            if (!res.hasInvite(town)) {
                PreparedStatement s = prepare("INSERT INTO " + prefix + "TownInvites(resident, townName) VALUES(?, ?)", true);
                s.setString(1, res.getUUID().toString());
                s.setString(2, town.getName());
                s.executeUpdate();

                res.addInvite(town);
            } else {
                log.error("Failed to save an invite to the datasource!");
                return false;
            }
        } catch (SQLException e) {
            log.error("Failed to load town invite: " + res.getPlayerName() + " for town " + town.getName());
            return false;
        }

        return true;
    }

    @Override
    public boolean saveWorld(int dim) {
        log.debug("Saving World %s", dim);
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "Worlds(dim) VALUES(?)", true);
            s.setInt(1, dim);
            s.executeUpdate();

            MyTownUniverse.getInstance().addWorld(dim);
        } catch (SQLException e) {
            log.error("Failed to save world with dimension id " + dim);
            return false;
        }
        return true;
    }

    /* ----- Link ----- */

    @Override
    public boolean linkResidentToTown(Resident res, Town town, Rank rank) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "ResidentsToTowns (resident, town, rank) VALUES(?, ?, ?)", true);
            s.setString(1, res.getUUID().toString());
            s.setString(2, town.getName());
            // You need rank since this method is the one that adds the resident to the town and vice-versa
            s.setString(3, rank.getName());
            s.execute();

            res.addTown(town);
            town.addResident(res, rank);
        } catch (SQLException e) {
            log.error("Failed to link Resident %s (%s) with Town %s", e, res.getPlayerName(), res.getUUID(), town.getName());
            return false;
        }
        return true;
    }

    @Override
    public boolean unlinkResidentFromTown(Resident res, Town town) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "ResidentsToTowns WHERE resident = ? AND town = ?", true);
            s.setString(1, res.getUUID().toString());
            s.setString(2, town.getName());
            s.execute();

            res.removeTown(town);
            town.removeResident(res);
        } catch (SQLException e) {
            log.error("Failed to unlink Resident %s (%s) with Town %s", e, res.getPlayerName(), res.getUUID(), town.getName());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateResidentToTownLink(Resident res, Town town, Rank rank) {
        try {
            PreparedStatement s = prepare("UPDATE " + prefix + "ResidentsToTowns SET rank = ? WHERE resident = ? AND town = ?", true);
            s.setString(1, rank.getName());
            s.setString(2, res.getUUID().toString());
            s.setString(3, town.getName());
            s.executeUpdate();

            town.promoteResident(res, rank);
        } catch (SQLException e) {
            log.error("Failed to update link between Resident %s (%s) with Town %s", e, res.getPlayerName(), res.getUUID(), town.getName());
            return false;
        }
        return true;
    }

    @Override
    public boolean linkTownToNation(Town town, Nation nation) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "TownsToNations (town, nation, rank) VALUES(?, ?, ?);", true);
            s.setString(1, town.getName());
            s.setString(2, nation.getName());
            s.setString(3, nation.getTownRank(town).toString());
            s.execute();
        } catch (SQLException e) {
            log.error("", e);
        }
        return true;
    }

    @Override
    public boolean unlinkTownFromNation(Town town, Nation nation) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "TownsToNations WHERE town = ? AND nation = ?", true);
            s.setString(1, town.getName());
            s.setString(2, nation.getName());
            s.execute();
        } catch (SQLException e) {
            log.error("", e);
        }
        return true;
    }

    @Override
    public boolean updateTownToNationLink(Town town, Nation nation) {
        try {
            PreparedStatement s = prepare("UPDATE " + prefix + "TownsToNations SET rank = ? WHERE town = ?, nation = ?", true);
            s.setString(1, nation.getTownRank(town).toString());
            s.setString(2, town.getName());
            s.setString(3, nation.getName());
            s.executeUpdate();
        } catch (SQLException e) {
            log.error("", e);
        }
        return true;
    }

    @Override
    public boolean linkResidentToPlot(Resident res, Plot plot, boolean isOwner) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "ResidentsToPlots(resident, plotID, isOwner) VALUES(?, ?, ?)", true);
            s.setString(1, res.getUUID().toString());
            s.setInt(2, plot.getDb_ID());
            s.setBoolean(3, isOwner);
            s.executeUpdate();

            if (isOwner)
                plot.addOwner(res);
            plot.addResident(res);

        } catch (SQLException e) {
            log.error("Failed to link " + res.getPlayerName() + " to plot " + plot.getName() + " in town " + plot.getTown().getName());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean unlinkResidentFromPlot(Resident res, Plot plot) {
        try {
            PreparedStatement s = prepare("DELETE FROM" + prefix + "ResidentsToPlots WHERE resident=? AND plotID=?", true);
            s.setString(1, res.getUUID().toString());
            s.setInt(2, plot.getDb_ID());
            s.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to link " + res.getPlayerName() + " to plot " + plot.getName() + " in town " + plot.getTown().getName());
            return false;
        }
        return true;
    }

    @Override
    public boolean updateResidentToPlotLink(Resident res, Plot plot, boolean isOwner) {
        try {
            PreparedStatement s = prepare("UPDATE" + prefix + "ResidentsToPlots SET isOwner=? WHERE resident=? AND plotID=?", true);
            s.setBoolean(1, isOwner);
            s.setString(2, res.getUUID().toString());
            s.setInt(3, plot.getDb_ID());
            s.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to link " + res.getPlayerName() + " to plot " + plot.getName() + " in town " + plot.getTown().getName());
            return false;
        }
        return true;
    }

    /* ----- Delete ----- */

    @Override
    public boolean deleteTown(Town town) {
        try {
            // Delete Town from Datasource
            PreparedStatement deleteTownStatement = prepare("DELETE FROM " + prefix + "Towns WHERE name=?", true);
            deleteTownStatement.setString(1, town.getName());
            deleteTownStatement.execute();

            // Remove all Blocks owned by the Town
            for (TownBlock b : town.getBlocks()) {
                MyTownUniverse.getInstance().removeTownBlock(b);
            }
            // Remove all Plots owned by the Town
            for (Plot p : town.getPlots()) {
                MyTownUniverse.getInstance().removePlot(p);
            }
            // Remove all Ranks owned by this Town
            for (Rank r : town.getRanks()) {
                MyTownUniverse.getInstance().removeRank(r);
            }
            for (Resident res : town.getResidents()) {
                res.removeTown(town);
                if (res.selectedTown == town)
                    deleteSelectedTown(res);
            }
            // Remove the Town from the Map
            MyTownUniverse.getInstance().removeTown(town);
        } catch (SQLException e) {
            log.error("Failed to delete Town %s", e, town.getName());
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteBlock(TownBlock block) {
        try {
            // Delete Block from Datasource
            PreparedStatement deleteBlockStatement = prepare("DELETE FROM " + prefix + "Blocks WHERE dim=? AND x=? AND z=?", true);
            deleteBlockStatement.setInt(1, block.getDim());
            deleteBlockStatement.setInt(2, block.getX());
            deleteBlockStatement.setInt(3, block.getZ());
            deleteBlockStatement.execute();

            // Delete Block from Town
            block.getTown().removeBlock(block);

            // Delete Plots contained in the Block
            for (Plot p : block.getPlots()) {
                deletePlot(p);
            }
            // Remove Block from Map
            MyTownUniverse.getInstance().removeTownBlock(block);
        } catch (SQLException e) {
            log.error("Failed to delete Block %s!", e, block.getKey());
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteRank(Rank rank) {
        try {
            // Delete Rank from Datasource
            PreparedStatement deleteRankStatement = prepare("DELETE FROM " + prefix + "Ranks WHERE name=? AND townName=?", true);
            deleteRankStatement.setString(1, rank.getName());
            deleteRankStatement.setString(2, rank.getTown().getName());
            deleteRankStatement.execute();

            // Remove Rank from Map
            MyTownUniverse.getInstance().removeRank(rank);
        } catch (SQLException e) {
            log.error("Failed to delete Rank %s!", e, rank.getKey());
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteResident(Resident resident) {
        try {
            // Delete Resident from Datasource
            PreparedStatement deleteResidentStatement = prepare("DELETE FROM " + prefix + "Residents WHERE uuid=?", true);
            deleteResidentStatement.setString(1, resident.getUUID().toString());
            deleteResidentStatement.execute();

            // Remove Resident from Map
            MyTownUniverse.getInstance().removeResident(resident);
        } catch (SQLException e) {
            log.error("Failed to delete Resident %s!", e, resident.getUUID());
            return false;
        }

        return true;
    }

    @Override
    public boolean deletePlot(Plot plot) {
        try {
            // Delete Plot from Datasource
            PreparedStatement deletePlotStatement = prepare("DELETE FROM " + prefix + "Plots WHERE ID=?", true);
            deletePlotStatement.setInt(1, plot.getDb_ID());
            deletePlotStatement.execute();

            // Remove Plot from Map
            MyTownUniverse.getInstance().removePlot(plot);
            plot.getTown().removePlot(plot);
        } catch (SQLException e) {
            log.error("Failed to delete Plot %s!", e, plot.getKey());
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteNation(Nation nation) {
        try {
            // Delete Nation from Datsource
            PreparedStatement deleteNationStatement = prepare("DELETE FROM " + prefix + "Nations WHERE name=?", true);
            deleteNationStatement.setString(1, nation.getName());
            deleteNationStatement.execute();

            // Remove Nation from Map
            MyTownUniverse.getInstance().removeNation(nation);
        } catch (SQLException e) {
            log.error("Failed to delete Nation $s!", e, nation.getName());
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteBlockWhitelist(BlockWhitelist bw, Town town) {
        try {
            PreparedStatement deleteStatement = prepare("DELETE FROM " + prefix + "BlockWhitelists WHERE ID=?", false);
            deleteStatement.setInt(1, bw.getDb_id());
            deleteStatement.executeUpdate();

            town.removeBlockWhitelist(bw);
        } catch (SQLException e) {
            log.error("Failed to delete BlockWhitelist!");
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteSelectedTown(Resident res) {
        try {
            if (res.getTowns().size() > 0) {
                PreparedStatement s = prepare("UPDATE FROM " + prefix + "SelectedTown SET townName=? WHERE resdent=?", true);
                s.setString(1, res.getTowns().get(0).getName());
                s.setString(2, res.getUUID().toString());
                s.executeUpdate();

                res.selectedTown = res.getTowns().get(0);
            } else {
                PreparedStatement statement = prepare("DELETE FROM " + prefix + "SelectedTown WHERE resident=?", true);
                statement.setString(1, res.getUUID().toString());
                statement.executeUpdate();

                res.selectedTown = null;
            }
        } catch (Exception e) {
            log.error("Failed to delete a town selection!");
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteFriendLink(Resident res1, Resident res2) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "Friends WHERE resident1=? AND resident2=?", true);
            s.setString(1, res1.getUUID().toString());
            s.setString(2, res2.getUUID().toString());
            s.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete link between " + res1.getPlayerName() + " and " + res2.getPlayerName());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteFriendRequest(Resident res1, Resident res2, boolean response) {
        try {
            if (res2.hasFriendRequest(res1)) {
                PreparedStatement s = prepare("DELETE FROM " + prefix + "FriendRequests WHERE resident=? AND residentTarget=?", true);
                s.setString(1, res1.getUUID().toString());
                s.setString(2, res2.getUUID().toString());
                s.executeUpdate();

                if (response)
                    saveFriendLink(res1, res2);
                res2.verifyFriendRequest(res1, response);
            } else {
                return false;
            }
        } catch (SQLException e) {
            log.error("Failed to delete friend request from " + res1.getPlayerName() + " to " + res2.getPlayerName());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteTownInvite(Resident res, Town town, boolean response) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "TownInvites WHERE resident=? AND townName=?", true);
            s.setString(1, res.getUUID().toString());
            s.setString(2, town.getName());
            s.executeUpdate();
            if (response)
                linkResidentToTown(res, town, town.getDefaultRank());
            res.removeInvite(town);
        } catch (SQLException e) {
            log.error("Failed to delete town invite for " + res.getPlayerName() + " to town " + town.getName());
            return false;
        }
        return true;
    }


    @Override
    public boolean deleteFlag(Flag flag, Town town) {
        try {
            PreparedStatement deleteFlagStatement = prepare("DELETE FROM " + prefix + "TownFlags WHERE name=? AND townName=?", true);
            deleteFlagStatement.setString(1, flag.flagType.toString());
            deleteFlagStatement.setString(2, town.getName());
            deleteFlagStatement.execute();

            town.removeFlag(flag.flagType);
        } catch (SQLException e) {
            log.error("Failed to delete flag %s!", e, flag.flagType.toString());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteFlag(Flag flag, Plot plot) {
        try {
            PreparedStatement deleteFlagStatement = prepare("DELETE FROM " + prefix + "PlotFlags WHERE name=? AND plotID=?", true);
            deleteFlagStatement.setString(1, flag.flagType.toString());
            deleteFlagStatement.setInt(2, plot.getDb_ID());
            deleteFlagStatement.execute();

            plot.removeFlag(flag.flagType);
        } catch (SQLException e) {
            log.error("Failed to delete flag %s!", e, flag.flagType.toString());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteWorld(int dim) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "Worlds WHERE dim=?", true);
            s.setInt(1, dim);
            s.executeUpdate();

            MyTownUniverse.getInstance().removeWorld(dim);
        } catch (SQLException e) {
            log.error("Failed to delete world with dimension id " + dim);
            return false;
        }
        return true;
    }

    @Override
    public boolean removeRankPermission(Rank rank, String perm) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "RankPermissions WHERE node = ? AND rank = ?", true);
            s.setString(1, perm);
            s.setString(2, rank.getName());
            s.execute();
        } catch (SQLException e) {
            log.error("Failed to add permission (%s) to Rank (%s)", e, perm, rank.getName());
            return false;
        }
        return true;
    }

    /* ----- Checks ------ */

    @SuppressWarnings("unchecked")
    @Override
    protected boolean checkFlags() {


        // Checking if flag is supposed to exist and it doesn't or otherwise.
        for (Town town : MyTownUniverse.getInstance().getTownsMap().values()) {
            for (FlagType type : FlagType.values()) {
                if (!type.isUsableForTowns() && town.hasFlag(type)) {
                    deleteFlag(town.getFlag(type), town);
                    MyTown.instance.log.info("A flag in town " + town.getName() + " got deleted because of the settings.");
                } else if (type.isUsableForTowns() && !town.hasFlag(type)) {
                    saveFlag(new Flag(type, type.getDefaultValue()), town);
                    MyTown.instance.log.info("A flag in town " + town.getName() + " got created because of the settings.");
                }
            }
        }

        for (Plot plot : MyTownUniverse.getInstance().getPlotsMap().values()) {
            for (FlagType type : FlagType.values()) {
                if (!type.isTownOnly()) {
                    if (!type.isUsableForTowns() && plot.hasFlag(type)) {
                        deleteFlag(plot.getFlag(type), plot);
                        MyTown.instance.log.info("A flag in a plot in town " + plot.getTown().getName() + " got deleted because of the settings.");
                    } else if (type.isUsableForTowns() && !plot.hasFlag(type)) {
                        saveFlag(new Flag(type, type.getDefaultValue()), plot);
                        MyTown.instance.log.info("A flag in a plot in town " + plot.getTown().getName() + " got created because of the settings.");
                    }
                }
            }
        }

        return true;
    }

    /* ----- Helpers ----- */

    /**
     * Returns a PreparedStatement using the given sql
     *
     * @param sql                  The SQL Statement
     * @param returnGenerationKeys A flag indicating whether auto-generated keys
     *                             should be returned
     * @return
     * @throws Exception
     */
    protected PreparedStatement prepare(String sql, boolean returnGenerationKeys) throws SQLException {
        if (!createConnection())
            throw new SQLException("No SQL Connection");
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(sql, returnGenerationKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
        } catch (SQLException e) {
            log.fatal(sql);
            e.printStackTrace();
        }
        return statement;
    }

    /* ----- Update System ----- */

    /**
     * Holds an SQL statement to be run to update the tables in the DB
     *
     * @author Joe Goett
     */
    protected class DBUpdate {
        /**
         * Formatted mm.dd.yyyy.e where e increments by 1 for every update released on the same date
         */
        public String id;
        public String desc;
        public String sql;

        public DBUpdate(String id, String desc, String sql) {
            this.id = id;
            this.desc = desc;
            this.sql = sql;
        }
    }

    protected List<DBUpdate> updates = new ArrayList<DBUpdate>();

    /**
     * Setup all the updates
     */
    protected void setupUpdates() { // TODO Move these into a XML/JSON file?
        updates.add(new DBUpdate("07.25.2014.1", "Add Updates Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Updates (" +
                "id VARCHAR(20) NOT NULL," +
                "description VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(id)" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.2", "Add Residents Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Residents (" +
                "uuid CHAR(36) NOT NULL," +
                "name VARCHAR(240) NOT NULL," +
                "joined BIGINT NOT NULL," +
                "lastOnline BIGINT NOT NULL," +
                "PRIMARY KEY(uuid)" +
                ");"));
        updates.add(new DBUpdate("10.05.2014", "Add Worlds", "CREATE TABLE IF NOT EXISTS " + prefix + "Worlds(" +
                "dim INT," +
                "PRIMARY KEY(dim))"));
        updates.add(new DBUpdate("07.25.2014.3", "Add Towns Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Towns (" +
                "name VARCHAR(32) NOT NULL," + // TODO Allow larger town names?
                "isAdminTown BOOLEAN, " +
                "spawnDim INT NOT NULL, " +
                "spawnX FLOAT NOT NULL, " +
                "spawnY FLOAT NOT NULL, " +
                "spawnZ FLOAT NOT NULL, " +
                "cameraYaw FLOAT NOT NULL, " +
                "cameraPitch FLOAT NOT NULL, " +
                "PRIMARY KEY(name), " +
                "FOREIGN KEY(spawnDim) REFERENCES " + prefix + " Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.4", "Add Ranks Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Ranks (" +
                "name VARCHAR(50) NOT NULL," +  // TODO Allow larger rank names?
                "townName VARCHAR(32) NOT NULL," +
                "isDefault BOOLEAN, " +
                "PRIMARY KEY(name, townName)," +
                "FOREIGN KEY(townName) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.5", "Add RankPermissions Table", "CREATE TABLE IF NOT EXISTS " + prefix + "RankPermissions (" +
                "node VARCHAR(100) NOT NULL," +
                "rank VARCHAR(50) NOT NULL," +
                "townName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(node, rank, townName)," +
                "FOREIGN KEY(rank, townName) REFERENCES " + prefix + "Ranks(name, townName) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.6", "Add Blocks Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Blocks (" +
                "dim INT NOT NULL," +
                "x INT NOT NULL," +
                "z INT NOT NULL," +
                "townName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(dim, x, z)," +
                "FOREIGN KEY(townName) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + prefix + "Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.7", "Add Plots Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Plots (" +
                "ID INTEGER NOT NULL " + AUTO_INCREMENT + "," + // Just because it's a pain with this many primary keys
                "name VARCHAR(50) NOT NULL," + // TODO Allow larger Plot names?
                "dim INT NOT NULL," +
                "x1 INT NOT NULL," +
                "y1 INT NOT NULL," +
                "z1 INT NOT NULL," +
                "x2 INT NOT NULL," +
                "y2 INT NOT NULL," +
                "z2 INT NOT NULL," +
                "townName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(ID)," +
                "FOREIGN KEY(townName) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + prefix + "Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.8", "Add Nations Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Nations (" +
                "name VARCHAR(32) NOT NULL," + // TODO Allow larger nation names?
                "PRIMARY KEY(name)" +
                ");"));

        // Create "Join" Tables
        updates.add(new DBUpdate("08.07.2014.1", "Add ResidentsToTowns Table", "CREATE TABLE IF NOT EXISTS " + prefix + "ResidentsToTowns (" +
                "resident CHAR(36) NOT NULL," +
                "town VARCHAR(32) NOT NULL," +
                "rank VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(resident, town)," +
                "FOREIGN KEY(resident) REFERENCES " + prefix + "Residents(uuid) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(rank, town) REFERENCES " + prefix + "Ranks(name, townName) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.07.2014.2", "Add TownsToNations Table", "CREATE TABLE IF NOT EXISTS " + prefix + "TownsToNations (" +
                "town VARCHAR(50)," +
                "nation VARCHAR(50)," +
                "rank CHAR(1) DEFAULT 'T'," +
                "PRIMARY KEY(town, nation)," +
                "FOREIGN KEY(town) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(nation) REFERENCES " + prefix + "Nations(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.26.2014.1", "Add TownFlags Table", "CREATE TABLE IF NOT EXISTS " + prefix + "TownFlags (" +
                "name VARCHAR(50) NOT NULL," +
                "serializedValue VARCHAR(400), " +
                "townName VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(name, townName)," +
                "FOREIGN KEY(townName) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.30.2014.1", "Add PlotFlags Table", "CREATE TABLE IF NOT EXISTS " + prefix + "PlotFlags (" +
                "name VARCHAR(50) NOT NULL," +
                "serializedValue VARCHAR(400), " +
                "plotID INT NOT NULL," +
                "PRIMARY KEY(name, plotID)," +
                "FOREIGN KEY(plotID) REFERENCES " + prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.31.2014.1", "Add ResidentsToPlots", "CREATE TABLE IF NOT EXISTS " + prefix +
                "ResidentsToPlots(" +
                "resident varchar(36) NOT NULL, " +
                "plotID INT NOT NULL, " +
                "isOwner boolean, " + // false if it's ONLY whitelisted, if neither then shouldn't be in this list
                "PRIMARY KEY(resident, plotID), " +
                "FOREIGN KEY(resident) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE, " +
                "FOREIGN KEY(plotID) REFERENCES " + prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("09.04.2014.1", "Add BlockWhitelists", "CREATE TABLE IF NOT EXISTS " + prefix +
                "BlockWhitelists(" +
                "ID INTEGER NOT NULL " + AUTO_INCREMENT + ", " +
                "dim INT NOT NULL, " +
                "x INT NOT NULL, " +
                "y INT NOT NULL, " +
                "z INT NOT NULL, " +
                "townName VARCHAR(50), " +
                "flagName VARCHAR(50) NOT NULL, " +
                "PRIMARY KEY(ID), " +
                "FOREIGN KEY(flagName, townName) REFERENCES " + prefix + "TownFlags(name, townName) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + prefix + "Worlds(dim) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("09.11.2014.1", "Add SelectedTown", "CREATE TABLE IF NOT EXISTS " + prefix +
                "SelectedTown(" +
                "resident CHAR(36), " +
                "townName VARCHAR(50)," +
                "PRIMARY KEY(resident), " +
                "FOREIGN KEY(resident) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(townName) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("09.19.2014.1", "Add Friends", "CREATE TABLE IF NOT EXISTS " + prefix + "Friends(" +
                "resident1 CHAR(36)," +
                "resident2 CHAR(36)," +
                "PRIMARY KEY(resident1, resident2)," +
                "FOREIGN KEY(resident1) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(resident2) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("09.19.2014.2", "Add FriendRequests", "CREATE TABLE IF NOT EXISTS " + prefix + "FriendRequests(" +
                "resident CHAR(36)," +
                "residentTarget CHAR(36)," +
                "PRIMARY KEY(resident, residentTarget)," +
                "FOREIGN KEY(resident) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(residentTarget) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("10.02.2014", "Add TownInvites", "CREATE TABLE IF NOT EXISTS " + prefix + "TownInvites(" +
                "resident CHAR(36)," +
                "townName VARCHAR(50), " +
                "PRIMARY KEY(resident, townName)," +
                "FOREIGN KEY(resident) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE, " +
                "FOREIGN KEY(townName) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE)"));

        // Table Modifications
        updates.add(new DBUpdate("10.18.2014.1", "Add 'extraBlocks' to towns", "ALTER TABLE " + prefix +
                "Towns ADD extraBlocks INTEGER DEFAULT 0"));

        updates.add(new DBUpdate("10.23.2014.1", "Add 'maxPlots' to towns", "ALTER TABLE " + prefix +
                "Towns ADD maxPlots INTEGER DEFAULT " + Config.defaultMaxPlots + ""));

        updates.add(new DBUpdate("11.4.2014.1", "Add 'extraBlocks to residents", "ALTER TABLE " + prefix +
                "Residents ADD extraBlocks INTEGER DEFAULT 0;"));
    }

    /**
     * Does the actual updates on the DB
     *
     * @throws Exception
     */
    protected void doUpdates() throws SQLException {
        List<String> ids = Lists.newArrayList();
        PreparedStatement statement;
        try {
            statement = prepare("SELECT id FROM " + prefix + "Updates", false);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch (Exception e) {
        } // Ignore. Just missing the updates table for now

        for (DBUpdate update : updates) {
            if (ids.contains(update.id)) {
                continue; // Skip if update is already done
            }

            try {
                // Update!
                log.info("Running update %s - %s", update.id, update.desc);
                statement = prepare(update.sql, false);
                statement.execute();

                // Insert the update key so as to not run the update again
                statement = prepare("INSERT INTO " + prefix + "Updates (id,description) VALUES(?,?)", true);
                statement.setString(1, update.id);
                statement.setString(2, update.desc);
                statement.executeUpdate();
            } catch (SQLException e) {
                log.error("Update (%s - %s) failed to apply!", e, update.id, update.desc);
                throw e; // Throws back up to force safemode
            }
        }
    }
}
