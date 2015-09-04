package mytown.datasource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.config.Config;
import myessentials.config.ConfigProperty;
import myessentials.teleport.Teleport;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.protection.ProtectionUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.*;
import java.util.Properties;
import java.util.UUID;

// TODO Check connection for each command and error out if connection doesn't check out
// TODO Run DB writes (and maybe reads?) on a separate thread
// TODO Ensure thread safety!

public abstract class MyTownDatasourceSQL extends MyTownDatasource {
    @ConfigProperty(category = "datasource.sql", comment = "The prefix of each of the tables. <prefix>tablename")
    protected String prefix = "";

    @ConfigProperty(category = "datasource.sql", comment = "User defined properties to be passed to the connection.\nFormat: key=value;key=value...")
    protected String[] userProperties = {};

    protected String autoIncrement = ""; // Only because SQLite and MySQL are different >.>

    protected Properties dbProperties = new Properties();
    protected String dsn = "";
    protected Connection conn = null;

    public boolean initialize() {
        setup();

        // Add user-defined properties
        for (String prop : userProperties) {
            String[] pair = prop.split("=");
            if (pair.length < 2)
                continue;
            dbProperties.put(pair[0], pair[1]);
        }

        // Register driver if needed
        try {
            Driver driver = (Driver) Class.forName(getDriver()).newInstance();
            DriverManager.registerDriver(driver);
        } catch (Exception ex) {
            LOG.error("Driver error", ex);
        }

        // Attempt connection
        if (createConnection()) {
            LOG.info("Connected to database");
        } else {
            LOG.error("Failed to connect to the database!");
            return false;
        }

        // Run updates
        try {
            SQLSchema schema = new SQLSchema(this);
            schema.doUpdates();
        } catch (SQLException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        // Initialization was successful! Yay!
        return true;
    }

    // TODO Change this to checkConnection() and call on each command?
    protected boolean createConnection() {
        try {
            if (conn == null || conn.isClosed() || (!"sqlite".equalsIgnoreCase(Config.dbType) && conn.isValid(1))) {
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
            LOG.error("Failed to get SQL connection! {}", ex, dsn);
            LOG.error(ExceptionUtils.getStackTrace(ex));
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
     */
    protected abstract String getDriver();

    /* ----- Read ----- */

    @Override
    protected boolean loadWorlds() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "Worlds", true);
            ResultSet rs = s.executeQuery();

            while (rs.next()) {
                MyTownUniverse.instance.addWorld(rs.getInt("dim"));
            }

        } catch (SQLException e) {
            LOG.error("Failed to load worlds from the database!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        for (World world : MinecraftServer.getServer().worldServers) {
            if (!MyTownUniverse.instance.worlds.contains(world.provider.dimensionId)) {
                saveWorld(world.provider.dimensionId);
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
                town.setSpawn(new Teleport(rs.getInt("spawnDim"), rs.getFloat("spawnX"), rs.getFloat("spawnY"), rs.getFloat("spawnZ"), rs.getFloat("cameraYaw"), rs.getFloat("cameraPitch")));
                town.townBlocksContainer.setExtraBlocks(rs.getInt("extraBlocks"));
                town.townBlocksContainer.setMaxFarClaims(rs.getInt("maxFarClaims"));
                town.plotsContainer.setMaxPlots(rs.getInt("maxPlots"));

                MyTownUniverse.instance.addTown(town);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load Towns!");
            LOG.error(ExceptionUtils.getStackTrace(e));
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
                Town town = getUniverse().towns.get(rs.getString("townName"));
                TownBlock block = new TownBlock(rs.getInt("dim"), rs.getInt("x"), rs.getInt("z"), rs.getBoolean("isFarClaim"), rs.getInt("pricePaid"), town);

                town.townBlocksContainer.add(block);

                MyTownUniverse.instance.addTownBlock(block);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load blocks!");
            LOG.error(ExceptionUtils.getStackTrace(e));
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
                Town town = getUniverse().towns.get(rs.getString("townName"));
                Rank rank = new Rank(rs.getString("name"), town, Rank.Type.valueOf(rs.getString("type")));

                LOG.debug("Loading Rank %s for Town {}", rank.getName(), town.getName());

                town.ranksContainer.add(rank);
                MyTownUniverse.instance.addRank(rank);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load a rank!");
            LOG.error(ExceptionUtils.getStackTrace(e));
        }
        return true;
    }

    @Override
    protected boolean loadRankPermissions() {
        try {
            PreparedStatement loadRankPermsStatement = prepare("SELECT * FROM " + prefix + "RankPermissions", true);
            ResultSet rs = loadRankPermsStatement.executeQuery();
            while(rs.next()) {
                Town town = getUniverse().towns.get(rs.getString("townName"));
                Rank rank = town.ranksContainer.get(rs.getString("rank"));

                rank.permissionsContainer.add(rs.getString("node"));
            }
        } catch (SQLException e) {
            LOG.error("Failed to load RankPermissions!");
            LOG.error(ExceptionUtils.getStackTrace(e));
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
                Resident res = new Resident(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getLong("joined"), rs.getLong("lastOnline"));
                res.setExtraBlocks(rs.getInt("extraBlocks"));

                MyTownUniverse.instance.addResident(res);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load Residents!");
            LOG.error(ExceptionUtils.getStackTrace(e));
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
                Town town = getUniverse().towns.get(rs.getString("townName"));
                Plot plot = new Plot(rs.getString("name"), town, rs.getInt("dim"), rs.getInt("x1"), rs.getInt("y1"), rs.getInt("z1"), rs.getInt("x2"), rs.getInt("y2"), rs.getInt("z2"));
                plot.setDbID(rs.getInt("ID"));

                town.plotsContainer.add(plot);

                MyTownUniverse.instance.addPlot(plot);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load Plots!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    /*
    @Override
    protected boolean loadNations() {
        try {
            PreparedStatement loadNationsStatement = prepare("SELECT * FROM " + prefix + "Nations", true);
            ResultSet rs = loadNationsStatement.executeQuery();

            while (rs.next()) {
                Nation nation = new Nation(rs.getString("name"));
                MyTownUniverse.instance.addNation(nation);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load Nations!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }
    */

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

                    Town town = getUniverse().towns.get(townName);
                    town.flagsContainer.add(flag);
                } catch (IllegalArgumentException ex) {
                    LOG.error("Flag {} does no longer exist... will be deleted shortly from the database.", flagName);
                    LOG.error(ExceptionUtils.getStackTrace(ex));
                    PreparedStatement removeFlag = prepare("DELETE FROM " + prefix + "TownFlags WHERE townName=? AND name=?", true);
                    removeFlag.setString(1, townName);
                    removeFlag.setString(2, flagName);
                    removeFlag.executeUpdate();
                }
            }
        } catch (SQLException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
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

                    if(type.isTownOnly()) {
                        throw new IllegalArgumentException("FlagType " + type + " can only be used in towns.");
                    }

                    Plot plot = getUniverse().plots.get(plotID);
                    plot.flagsContainer.add(flag);

                } catch (IllegalArgumentException ex) {
                    LOG.error("Flag {} does no longer exist. Deleting from database.", flagName);
                    LOG.error(ExceptionUtils.getStackTrace(ex));
                    PreparedStatement removeFlag = prepare("DELETE FROM " + prefix + "PlotFlags WHERE plotID=? AND name=?", true);
                    removeFlag.setInt(1, plotID);
                    removeFlag.setString(2, flagName);
                    removeFlag.executeUpdate();
                }
            }
        } catch (SQLException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    @Override
    protected boolean loadResidentsToTowns() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "ResidentsToTowns", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Resident res = getUniverse().residents.get(UUID.fromString(rs.getString("resident")));
                Town town = getUniverse().towns.get(rs.getString("town"));
                Rank rank = town.ranksContainer.get(rs.getString("rank"));

                town.residentsMap.put(res, rank);
                res.townsContainer.add(town);
            }
        } catch (SQLException e) {
            LOG.error("Failed to link Residents to Towns!");
            LOG.error(ExceptionUtils.getStackTrace(e));
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
                bw.setDbID(rs.getInt("ID"));
                Town town = getUniverse().towns.get(rs.getString("townName"));
                /*
                town.addBlockWhitelist(bw);
                */
            }

        } catch (SQLException e) {
            LOG.error("Failed to load a Block whitelist");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }
    /*
    @Override
    protected boolean loadTownsToNations() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "TownsToNations", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Town town = getUniverse().towns.get("");
                Nation nation = MyTownUniverse.instance.getNation("");


                nation.addTown(town);
                nation.promoteTown(town, Nation.Rank.parse(rs.getString("rank")));
                town.setNation(nation);

            }
        } catch (SQLException e) {
            LOG.error("Failed to link Towns to Nations!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }
    */

    @Override
    protected boolean loadResidentsToPlots() {
        try {
            PreparedStatement loadStatement = prepare("SELECT * FROM " + prefix + "ResidentsToPlots", true);
            ResultSet rs = loadStatement.executeQuery();

            while (rs.next()) {
                Plot plot = getUniverse().plots.get(rs.getInt("plotID"));
                Resident res = getUniverse().residents.get(UUID.fromString(rs.getString("resident")));

                if (rs.getBoolean("isOwner")) {
                    plot.ownersContainer.add(res);
                } else {
                    plot.membersContainer.add(res);
                }
            }
        } catch (SQLException e) {
            LOG.error("Failed to link Residents to Plots");
            LOG.error(ExceptionUtils.getStackTrace(e));
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
                Resident res = getUniverse().residents.get(UUID.fromString(rs.getString("resident")));
                Town town = getUniverse().towns.get(rs.getString("townName"));

                res.townInvitesContainer.add(town);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load town invites.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected boolean loadBlockOwners() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "BlockOwners", true);
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                Resident res = getUniverse().residents.get(UUID.fromString(rs.getString("resident")));
                int dim = rs.getInt("dim");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");

                World world = MinecraftServer.getServer().worldServerForDimension(dim);
                if(world == null) {
                    LOG.error("Failed to find a TileEntity at position ({}, {}, {}| DIM: {})", x, y, z, dim);
                    continue;
                }

                TileEntity te = world.getTileEntity(x, y, z);
                if(te == null) {
                    LOG.error("Failed to find a TileEntity at position ({}, {}, {}| DIM: {})", x, y, z, dim);
                    continue;
                }
                ProtectionUtils.addTileEntity(te, res);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load block owners.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    protected boolean loadTownBanks() {
        try {
            PreparedStatement s = prepare("SELECT * FROM " + prefix + "TownBanks", true);
            ResultSet rs = s.executeQuery();
            while(rs.next()) {
                Town town = getUniverse().towns.get(rs.getString("townName"));

                town.bank.setAmount(rs.getInt("amount"));
                town.bank.setDaysNotPaid(rs.getInt("daysNotPaid"));

                getUniverse().addBank(town.bank);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load town banks.");
            LOG.error(ExceptionUtils.getStackTrace(e));
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
                Resident res = getUniverse().residents.get(UUID.fromString(rs.getString("resident")));
                Town town = getUniverse().towns.get(rs.getString("townName"));
                res.townsContainer.setMainTown(town);
            }
        } catch (SQLException e) {
            LOG.error("Failed to load a town selection.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    /* ----- Save ----- */

    @Override
    public boolean saveTown(Town town) {
        LOG.debug("Saving Town {}", town.getName());
        try {
            if (getUniverse().towns.contains(town)) { // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "Towns SET name=?, spawnDim=?, spawnX=?, spawnY=?, spawnZ=?, cameraYaw=?, cameraPitch=?, extraBlocks=?, maxPlots=?, maxFarClaims=? WHERE name=?", true);
                updateStatement.setString(1, town.getName());
                updateStatement.setInt(2, town.getSpawn().getDim());
                updateStatement.setFloat(3, town.getSpawn().getX());
                updateStatement.setFloat(4, town.getSpawn().getY());
                updateStatement.setFloat(5, town.getSpawn().getZ());
                updateStatement.setFloat(6, town.getSpawn().getYaw());
                updateStatement.setFloat(7, town.getSpawn().getPitch());
                updateStatement.setInt(8, town.townBlocksContainer.getExtraBlocks());
                updateStatement.setInt(9, town.plotsContainer.getMaxPlots());
                updateStatement.setInt(10, town.townBlocksContainer.getMaxFarClaims());

                if (town.getOldName() == null)
                    updateStatement.setString(11, town.getName());
                else
                    updateStatement.setString(11, town.getOldName());

                updateStatement.executeUpdate();

                // Need to move the Town in the map from the old name to the new
                if (town.getOldName() != null) {
                    MyTownUniverse.instance.removeTown(town);
                    // This updates the name
                    MyTownUniverse.instance.addTown(town);
                }
                town.resetOldName();

            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Towns (name, spawnDim, spawnX, spawnY, spawnZ, cameraYaw, cameraPitch, isAdminTown, extraBlocks, maxPlots, maxFarClaims) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", true);
                insertStatement.setString(1, town.getName());
                insertStatement.setInt(2, town.getSpawn().getDim());
                insertStatement.setFloat(3, town.getSpawn().getX());
                insertStatement.setFloat(4, town.getSpawn().getY());
                insertStatement.setFloat(5, town.getSpawn().getZ());
                insertStatement.setFloat(6, town.getSpawn().getYaw());
                insertStatement.setFloat(7, town.getSpawn().getPitch());
                insertStatement.setBoolean(8, town instanceof AdminTown);
                insertStatement.setInt(9, town.townBlocksContainer.getExtraBlocks());
                insertStatement.setInt(10, town.plotsContainer.getMaxPlots());
                insertStatement.setInt(11, town.townBlocksContainer.getMaxFarClaims());

                insertStatement.executeUpdate();

                // Put the Town in the Map
                MyTownUniverse.instance.addTown(town);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Town {}!", town.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    @Override
    public boolean saveBlock(TownBlock block) {
        LOG.debug("Saving TownBlock {}", block.getKey());
        try {
            if (getUniverse().blocks.contains(block)) { // Update
                // TODO Update Block (If needed?)
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Blocks (dim, x, z, isFarClaim, pricePaid, townName) VALUES (?, ?, ?, ?, ?, ?)", true);
                insertStatement.setInt(1, block.getDim());
                insertStatement.setInt(2, block.getX());
                insertStatement.setInt(3, block.getZ());
                insertStatement.setBoolean(4, block.isFarClaim());
                insertStatement.setInt(5, block.getPricePaid());
                insertStatement.setString(6, block.getTown().getName());
                insertStatement.executeUpdate();

                // Put the Block in the Map
                MyTownUniverse.instance.addTownBlock(block);

                block.getTown().townBlocksContainer.add(block);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Block {}!", block.getKey());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean saveRank(Rank rank) { // TODO Insert any new permissions to the RankPermission table
        LOG.debug("Saving Rank {} in town {}", rank.getName(), rank.getTown().getName());
        try {
            if (getUniverse().ranks.contains(rank)) { // Update
                try {
                    getConnection().setAutoCommit(false);

                    PreparedStatement s = prepare("UPDATE " + prefix + "Ranks SET type=?, name=? WHERE name=? AND townName=?", true);
                    s.setString(1, rank.getType().toString());
                    if (rank.getNewName() == null) {
                        s.setString(2, rank.getName());
                    } else {
                        s.setString(2, rank.getNewName());
                        rank.resetNewName();
                    }
                    s.setString(3, rank.getName());
                    s.setString(4, rank.getTown().getName());
                    s.executeUpdate();

                    s = prepare("DELETE FROM " + prefix + "RankPermissions WHERE rank=? AND townName=?", true);
                    s.setString(1, rank.getName());
                    s.setString(2, rank.getTown().getName());
                    s.executeUpdate();

                    if (!rank.permissionsContainer.isEmpty()) {
                        s = prepare("INSERT INTO " + prefix + "RankPermissions(node, rank, townName) VALUES(?, ?, ?)", true);
                        for (String perm : rank.permissionsContainer) {
                            s.setString(1, perm);
                            s.setString(2, rank.getName());
                            s.setString(3, rank.getTown().getName());
                            s.addBatch();
                        }
                        s.executeBatch();
                    }
                } catch (SQLException e) {
                    LOG.error("Failed to update Rank {} in town {}", rank.getName(), rank.getTown().getName());
                    LOG.error(ExceptionUtils.getStackTrace(e));
                    getConnection().rollback();

                    return false;
                } finally {
                    getConnection().setAutoCommit(true);
                }
            } else { // Insert
                try {
                    getConnection().setAutoCommit(false);

                    PreparedStatement insertRankStatement = prepare("INSERT INTO " + prefix + "Ranks (name, townName, type) VALUES(?, ?, ?)", true);
                    insertRankStatement.setString(1, rank.getName());
                    insertRankStatement.setString(2, rank.getTown().getName());
                    insertRankStatement.setString(3, rank.getType().toString());
                    insertRankStatement.executeUpdate();

                    if (!rank.permissionsContainer.isEmpty()) {
                        PreparedStatement insertRankPermStatement = prepare("INSERT INTO " + prefix + "RankPermissions(node, rank, townName) VALUES(?, ?, ?)", true);
                        for (String perm : rank.permissionsContainer) {
                            insertRankPermStatement.setString(1, perm);
                            insertRankPermStatement.setString(2, rank.getName());
                            insertRankPermStatement.setString(3, rank.getTown().getName());
                            insertRankPermStatement.addBatch();
                        }
                        insertRankPermStatement.executeBatch();
                    }

                    // Put the Rank in the Map
                    MyTownUniverse.instance.addRank(rank);
                    rank.getTown().ranksContainer.add(rank);
                } catch (SQLException e) {
                    LOG.error("Failed to insert Rank {} in town {}", rank.getName(), rank.getTown().getName());
                    LOG.error(ExceptionUtils.getStackTrace(e));
                    getConnection().rollback();

                    return false;
                } finally {
                    getConnection().setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Rank {} in Town {}", rank.getName(), rank.getTown().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean saveRankPermission(Rank rank, String perm) {
        LOG.debug("Saving RankPermission {} for Rank {} in Town {}", perm, rank.getName(), rank.getTown().getName());
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "RankPermissions (node, rank) VALUES(?, ?)", true);
            s.setString(1, perm);
            s.setString(2, rank.getName());
            s.execute();

            rank.permissionsContainer.add(perm);
        } catch (SQLException e) {
            LOG.error("Failed to add permission ({}) to Rank ({})", perm, rank.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean saveResident(Resident resident) {
        LOG.debug("Saving Resident {} ({})", resident.getUUID(), resident.getPlayerName());
        try {
            if (getUniverse().residents.contains(resident.getUUID())) { // Update
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
                MyTownUniverse.instance.addResident(resident);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save resident {}!", resident.getUUID());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean savePlot(Plot plot) {
        LOG.debug("Saving Plot {} for Town {}", plot.getKey(), plot.getTown().getName());
        try {
            if (getUniverse().plots.contains(plot)) { // Update
                PreparedStatement statement = prepare("UPDATE " + prefix + "Plots SET name=?, dim=?, x1=?, y1=?, z1=?, x2=?, y2=?, z2=? WHERE ID=?", true);
                statement.setString(1, plot.getName());
                statement.setInt(2, plot.getDim());
                statement.setInt(3, plot.getStartX());
                statement.setInt(4, plot.getStartY());
                statement.setInt(5, plot.getStartZ());
                statement.setInt(6, plot.getEndX());
                statement.setInt(7, plot.getEndY());
                statement.setInt(8, plot.getEndZ());
                statement.setInt(9, plot.getDbID());
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
                    plot.setDbID(generatedKeys.getInt(1));

                for (Flag flag : plot.getTown().flagsContainer) {
                    if (!flag.getFlagType().isTownOnly())
                        saveFlag(new Flag(flag.getFlagType(), flag.getValue()), plot);
                }

                // Put the Plot in the Map
                MyTownUniverse.instance.addPlot(plot);
                plot.getTown().plotsContainer.add(plot);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Plot {}!", plot.getKey());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    /*
    @Override
    public boolean saveNation(Nation nation) { // TODO Link any new Towns to the given Nation
        LOG.debug("Saving Nation {}", nation.getName());
        try {
            if (MyTownUniverse.instance.hasNation(nation)) { // Update
                // TODO Update Nation (If needed?)
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Nations (name) VALUES(?)", true);
                insertStatement.setString(1, nation.getName());
                insertStatement.executeUpdate();

                // Put the Nation in the Map
                MyTownUniverse.instance.addNation(nation);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Nation {}!", nation.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }

        return true;
    }
    */

    @Override
    public boolean saveFlag(Flag flag, Plot plot) {
        LOG.debug("Saving Flag {} for Plot {}", flag.getFlagType().name(), plot.getKey());
        try {
            if (plot.flagsContainer.contains(flag.getFlagType())) {
                // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "PlotFlags SET serializedValue=? WHERE plotID=? AND name=?", true);
                updateStatement.setString(1, flag.serializeValue());
                updateStatement.setInt(2, plot.getDbID());
                updateStatement.setString(3, flag.getFlagType().toString());
                updateStatement.executeUpdate();


            } else {
                // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "PlotFlags(name, serializedValue, plotID) VALUES(?, ?, ?)", true);
                insertStatement.setString(1, flag.getFlagType().toString());
                insertStatement.setString(2, flag.serializeValue());
                insertStatement.setInt(3, plot.getDbID());
                insertStatement.executeUpdate();

                plot.flagsContainer.add(flag);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Flag {}!", flag.getFlagType().toString());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean saveFlag(Flag flag, Town town) {
        LOG.debug("Saving Flag {} for Town {}", flag.getFlagType().name(), town.getName());
        try {
            if (town.flagsContainer.contains(flag.getFlagType())) {
                // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "TownFlags SET serializedValue=? WHERE townName=? AND name=?", true);
                updateStatement.setString(1, flag.serializeValue());
                updateStatement.setString(2, town.getName());
                updateStatement.setString(3, flag.getFlagType().toString());
                updateStatement.executeUpdate();

            } else {
                // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "TownFlags(name,  serializedValue, townName) VALUES(?, ?, ?)", true);
                insertStatement.setString(1, flag.getFlagType().toString());
                insertStatement.setString(2, flag.serializeValue());
                insertStatement.setString(3, town.getName());
                insertStatement.executeUpdate();

                town.flagsContainer.add(flag);
            }
        } catch (SQLException e) {
            LOG.error("Failed to save Flag {}!", flag.getFlagType().toString());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }


    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public boolean saveBlockWhitelist(BlockWhitelist bw, Town town) {
        try {
            if (!town.blockWhitelistsContainer.contains(bw)) {
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "BlockWhitelists(dim, x, y, z, flagName, townName) VALUES(?, ?, ?, ?, ?, ?)", true);
                insertStatement.setInt(1, bw.getDim());
                insertStatement.setInt(2, bw.getX());
                insertStatement.setInt(3, bw.getY());
                insertStatement.setInt(4, bw.getZ());
                insertStatement.setString(5, bw.getFlagType().toString());
                insertStatement.setString(6, town.getName());

                insertStatement.executeUpdate();

                ResultSet keys = insertStatement.getGeneratedKeys();
                if (keys.next())
                    bw.setDbID(keys.getInt(1));

                town.blockWhitelistsContainer.add(bw);
            }
            // NO update since ID can't change
        } catch (SQLException e) {
            LOG.error("Failed to save a Block Whitelist!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean saveSelectedTown(Resident res, Town town) {
        try {
            if (res.townsContainer.getMainTown() == null) {
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
            res.townsContainer.setMainTown(town);

        } catch (SQLException e) {
            LOG.error("Failed to save a town selection!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean saveTownInvite(Resident res, Town town) {
        try {
            if (!res.townInvitesContainer.contains(town)) {
                PreparedStatement s = prepare("INSERT INTO " + prefix + "TownInvites(resident, townName) VALUES(?, ?)", true);
                s.setString(1, res.getUUID().toString());
                s.setString(2, town.getName());
                s.executeUpdate();

                res.townInvitesContainer.add(town);
            } else {
                return false;
            }
        } catch (SQLException e) {
            LOG.error("Failed to save town invite: {} for town {}", res.getPlayerName(), town.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean saveWorld(int dim) {
        LOG.debug("Saving World {}", dim);
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "Worlds(dim) VALUES(?)", true);
            s.setInt(1, dim);
            s.executeUpdate();

            MyTownUniverse.instance.addWorld(dim);
        } catch (SQLException e) {
            LOG.error("Failed to save world with dimension id {}", dim);
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public boolean saveBlockOwner(Resident res, int dim, int x, int y, int z) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "BlockOwners(resident, dim, x, y, z) VALUES(?, ?, ?, ?, ?)", false);
            s.setString(1, res.getUUID().toString());
            s.setInt(2, dim);
            s.setInt(3, x);
            s.setInt(4, y);
            s.setInt(5, z);
            s.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Failed to save block owner.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean saveTownBank(Bank bank) {
        try {
            if(getUniverse().banks.contains(bank)) {
                PreparedStatement s = prepare("UPDATE " + prefix + "TownBanks SET amount=?, daysNotPaid=? WHERE townName=?", false);
                s.setInt(1, bank.getAmount());
                s.setInt(2, bank.getDaysNotPaid());
                s.setString(3, bank.getTown().getName());
                s.executeUpdate();
            } else {
                bank.setAmount(Config.defaultBankAmount);
                bank.setDaysNotPaid(0);

                PreparedStatement s = prepare("INSERT INTO " + prefix + "TownBanks VALUES(?, ?, ?)", false);
                s.setString(1, bank.getTown().getName());
                s.setInt(2, bank.getAmount());
                s.setInt(3, bank.getDaysNotPaid());
                s.executeUpdate();

                getUniverse().addBank(bank);
            }

        } catch (SQLException e) {
            LOG.error("Failed to save a town's bank.");
            LOG.error(ExceptionUtils.getStackTrace(e));
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

            res.townsContainer.add(town);
            town.residentsMap.put(res, rank);
        } catch (SQLException e) {
            LOG.error("Failed to link Resident {} ({}) with Town {}", res.getPlayerName(), res.getUUID(), town.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
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

            res.townsContainer.remove(town);
            town.residentsMap.remove(res);
        } catch (SQLException e) {
            LOG.error("Failed to unlink Resident {} ({}) with Town {}", e, res.getPlayerName(), res.getUUID(), town.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
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

            town.residentsMap.put(res, rank);
        } catch (SQLException e) {
            LOG.error("Failed to update link between Resident {} ({}) with Town {}", e, res.getPlayerName(), res.getUUID(), town.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    /*
    @Override
    public boolean linkTownToNation(Town town, Nation nation) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "TownsToNations (town, nation, rank) VALUES(?, ?, ?);", true);
            s.setString(1, town.getName());
            s.setString(2, nation.getName());
            s.setString(3, nation.getTownRank(town).toString());
            s.execute();
        } catch (SQLException e) {
            LOG.error("Failed to link a town to a nation.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
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
            LOG.error("Failed to remove link from a town to a nation.");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
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
            LOG.error("Failed to update link between a town and a nation");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }
    */

    @Override
    public boolean linkResidentToPlot(Resident res, Plot plot, boolean isOwner) {
        try {
            PreparedStatement s = prepare("INSERT INTO " + prefix + "ResidentsToPlots(resident, plotID, isOwner) VALUES(?, ?, ?)", true);
            s.setString(1, res.getUUID().toString());
            s.setInt(2, plot.getDbID());
            s.setBoolean(3, isOwner);
            s.executeUpdate();

            if (isOwner) {
                plot.ownersContainer.add(res);
            } else {
                plot.membersContainer.add(res);
            }

        } catch (SQLException e) {
            LOG.error("Failed to link {} to plot {} in town {}", res.getPlayerName(), plot.getName(), plot.getTown().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean unlinkResidentFromPlot(Resident res, Plot plot) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "ResidentsToPlots WHERE resident=? AND plotID=?", true);
            s.setString(1, res.getUUID().toString());
            s.setInt(2, plot.getDbID());
            s.executeUpdate();

            plot.ownersContainer.remove(res);
            plot.membersContainer.remove(res);

        } catch (SQLException e) {
            LOG.error("Failed to unlink {} to plot {} in town {}", res.getPlayerName(), plot.getName(), plot.getTown().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean updateResidentToPlotLink(Resident res, Plot plot, boolean isOwner) {
        try {
            PreparedStatement s = prepare("UPDATE " + prefix + "ResidentsToPlots SET isOwner=? WHERE resident=? AND plotID=?", true);
            s.setBoolean(1, isOwner);
            s.setString(2, res.getUUID().toString());
            s.setInt(3, plot.getDbID());
            s.executeUpdate();

        } catch (SQLException e) {
            LOG.error("Failed to update link {} to plot {} in town {}", res.getPlayerName(), plot.getName(), plot.getTown().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
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
            for (TownBlock b : town.townBlocksContainer) {
                MyTownUniverse.instance.removeTownBlock(b);
            }
            // Remove all Plots owned by the Town
            for (Plot p : town.plotsContainer) {
                MyTownUniverse.instance.removePlot(p);
            }
            // Remove all Ranks owned by this Town
            for (Rank r : town.ranksContainer) {
                MyTownUniverse.instance.removeRank(r);
            }
            for (Resident res : town.residentsMap.keySet()) {
                if (res.townsContainer.getMainTown() == town)
                    deleteSelectedTown(res);
                res.townsContainer.remove(town);
            }
            // Remove the Town from the Map
            MyTownUniverse.instance.removeTown(town);
        } catch (SQLException e) {
            LOG.error("Failed to delete Town {}", town.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
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
            block.getTown().townBlocksContainer.remove(block);

            // Delete Plots contained in the Block
            for (Plot p : block.plotsContainer) {
                deletePlot(p);
            }
            // Remove Block from Map
            MyTownUniverse.instance.removeTownBlock(block);
        } catch (SQLException e) {
            LOG.error("Failed to delete Block {}!", block.getKey());
            LOG.error(ExceptionUtils.getStackTrace(e));
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
            MyTownUniverse.instance.removeRank(rank);
            rank.getTown().ranksContainer.remove(rank);
        } catch (SQLException e) {
            LOG.error("Failed to delete Rank {} in Town {}", rank.getName(), rank.getTown().getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
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
            MyTownUniverse.instance.removeResident(resident);
        } catch (SQLException e) {
            LOG.error("Failed to delete Resident {}!", resident.getUUID());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean deletePlot(Plot plot) {
        try {
            // Delete Plot from Datasource
            PreparedStatement deletePlotStatement = prepare("DELETE FROM " + prefix + "Plots WHERE ID=?", true);
            deletePlotStatement.setInt(1, plot.getDbID());
            deletePlotStatement.execute();

            // Remove Plot from Map
            MyTownUniverse.instance.removePlot(plot);
            plot.getTown().plotsContainer.remove(plot);
        } catch (SQLException e) {
            LOG.error("Failed to delete Plot {}!", plot.getKey());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    /*
    @Override
    public boolean deleteNation(Nation nation) {
        try {
            // Delete Nation from Datsource
            PreparedStatement deleteNationStatement = prepare("DELETE FROM " + prefix + "Nations WHERE name=?", true);
            deleteNationStatement.setString(1, nation.getName());
            deleteNationStatement.execute();

            // Remove Nation from Map
            MyTownUniverse.instance.removeNation(nation);
        } catch (SQLException e) {
            LOG.error("Failed to delete Nation {}!", nation.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }
    */

    @Override
    public boolean deleteBlockWhitelist(BlockWhitelist bw, Town town) {
        try {
            PreparedStatement deleteStatement = prepare("DELETE FROM " + prefix + "BlockWhitelists WHERE ID=?", false);
            deleteStatement.setInt(1, bw.getDbID());
            deleteStatement.executeUpdate();

            town.blockWhitelistsContainer.remove(bw);
        } catch (SQLException e) {
            LOG.error("Failed to delete BlockWhitelist!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteSelectedTown(Resident res) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "SelectedTown WHERE resident=?", true);
            statement.setString(1, res.getUUID().toString());
            statement.executeUpdate();
        } catch (Exception e) {
            LOG.error("Failed to delete a town selection!");
            LOG.error(ExceptionUtils.getStackTrace(e));
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
            if (response) {
                linkResidentToTown(res, town, town.ranksContainer.getDefaultRank());
            }
            res.townInvitesContainer.remove(town);
        } catch (SQLException e) {
            LOG.error("Failed to delete town invite for {} to town {}", res.getPlayerName(), town.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }


    @Override
    public boolean deleteFlag(Flag flag, Town town) {
        try {
            PreparedStatement deleteFlagStatement = prepare("DELETE FROM " + prefix + "TownFlags WHERE name=? AND townName=?", true);
            deleteFlagStatement.setString(1, flag.getFlagType().toString());
            deleteFlagStatement.setString(2, town.getName());
            deleteFlagStatement.execute();

            town.flagsContainer.remove(flag.getFlagType());
        } catch (SQLException e) {
            LOG.error("Failed to delete flag {}!", flag.getFlagType().toString());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteFlag(Flag flag, Plot plot) {
        try {
            PreparedStatement deleteFlagStatement = prepare("DELETE FROM " + prefix + "PlotFlags WHERE name=? AND plotID=?", true);
            deleteFlagStatement.setString(1, flag.getFlagType().toString());
            deleteFlagStatement.setInt(2, plot.getDbID());
            deleteFlagStatement.execute();

            plot.flagsContainer.remove(flag.getFlagType());
        } catch (SQLException e) {
            LOG.error("Failed to delete flag {}!", flag.getFlagType().toString());
            LOG.error(ExceptionUtils.getStackTrace(e));
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

            MyTownUniverse.instance.removeWorld(dim);
        } catch (SQLException e) {
            LOG.error("Failed to delete world with dimension id {}", dim);
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteRankPermission(Rank rank, String perm) {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "RankPermissions WHERE node = ? AND rank = ?", true);
            s.setString(1, perm);
            s.setString(2, rank.getName());
            s.execute();

            rank.permissionsContainer.remove(perm);
        } catch (SQLException e) {
            LOG.error("Failed to add permission ({}) to Rank ({})", perm, rank.getName());
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteAllBlockOwners() {
        try {
            PreparedStatement s = prepare("DELETE FROM " + prefix + "BlockOwners", false);
            s.execute();
        } catch (SQLException e) {
            LOG.error("Failed to delete BlockOwners table!");
            LOG.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    /* ----- Checks ------ */

    @SuppressWarnings("unchecked")
    @Override
    protected boolean checkFlags() {
        // Checking if flag is supposed to exist and it doesn't or otherwise.
        for (Town town : getUniverse().towns) {
            for (FlagType type : FlagType.values()) {
                if (!type.canTownsModify() && town.flagsContainer.contains(type)) {
                    deleteFlag(town.flagsContainer.get(type), town);
                    LOG.info("Flag {} in town {} got deleted because of the settings.", type.toString().toLowerCase(), town.getName());
                } else if (type.canTownsModify() && !town.flagsContainer.contains(type)) {
                    saveFlag(new Flag(type, type.getDefaultValue()), town);
                    LOG.info("Flag {} in town {} got created because of the settings.", type.toString().toLowerCase(), town.getName());
                } else {
                    if(!type.isValueAllowed(town.flagsContainer.getValue(type))) {
                        town.flagsContainer.get(type).setValueFromString(type.getDefaultValue().toString());
                        saveFlag(town.flagsContainer.get(type), town);
                        LOG.info("Flag {} in town {} had invalid value and got changed to its default state.", type.toString().toLowerCase(), town.getName());
                    }
                }
            }
        }

        for (Plot plot : getUniverse().plots) {
            for (FlagType type : FlagType.values()) {
                if (!type.isTownOnly()) {
                    if (!type.canTownsModify() && plot.flagsContainer.contains(type)) {
                        deleteFlag(plot.flagsContainer.get(type), plot);
                        LOG.info("Flag {} in a plot in town {} got deleted because of the settings.", type.toString().toLowerCase(), plot.getTown().getName());
                    } else if (type.canTownsModify() && !plot.flagsContainer.contains(type)) {
                        saveFlag(new Flag(type, type.getDefaultValue()), plot);
                        LOG.info("Flag {} in a plot in town {} got created because of the settings.", type.toString().toLowerCase(), plot.getTown().getName());
                    } else {
                        if(!type.isValueAllowed(plot.flagsContainer.getValue(type))) {
                            plot.flagsContainer.get(type).setValueFromString(type.getDefaultValue().toString());
                            saveFlag(plot.flagsContainer.get(type), plot);
                            LOG.info("Flag {} in a plot in town {} had invalid value and got changed to its default state.", type.toString().toLowerCase(), plot.getTown().getName());
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected boolean checkTowns() {
        for(Town town : getUniverse().towns) {
            if(town.ranksContainer.getDefaultRank() == null) {
                LOG.error("Town {} does not have a default rank set.", town.getName());
                Rank rank = town.ranksContainer.get("Resident");
                if(rank == null) {
                    rank = new Rank("Resident", town, Rank.Type.DEFAULT);
                    rank.permissionsContainer.addAll(Rank.defaultRanks.get(Rank.Type.DEFAULT).permissionsContainer);
                    LOG.info("Adding default rank for town.");
                } else {
                    rank.setType(Rank.Type.DEFAULT);
                    LOG.info("Set 'Resident' as current default rank.");
                }
                saveRank(rank);
            }

            if(town.ranksContainer.getMayorRank() == null) {
                LOG.error("Town {} does not have a mayor rank set.", town.getName());
                Rank rank = town.ranksContainer.get("Mayor");
                if(rank == null) {
                    rank = new Rank("Mayor", town, Rank.Type.MAYOR);
                    rank.permissionsContainer.addAll(Rank.defaultRanks.get(Rank.Type.MAYOR).permissionsContainer);
                    LOG.info("Adding mayor rank for town.");
                } else {
                    rank.setType(Rank.Type.MAYOR);
                    LOG.info("Set 'Mayor' as current default rank.");
                }
                saveRank(rank);
            }

            if(!(town instanceof AdminTown)) {
                if(!getUniverse().banks.contains(town)) {
                    saveTownBank(town.bank);
                    LOG.info("Added bank entry for {}", town.getName());
                }
            }
        }
        return true;
    }

    /* ----- Reset ----- */

    public boolean resetRanks(Town town) {

        for(Rank defaultRank : Rank.defaultRanks) {
            Rank rank = town.ranksContainer.get(defaultRank.getName());
            if(rank == null) {
                LOG.info("Adding rank {} to town {}", defaultRank.getName(), town.getName());
                rank = new Rank(defaultRank.getName(), town, defaultRank.getType());
            } else  {
                rank.permissionsContainer.clear();
                if(rank.getType() != defaultRank.getType()) {
                    LOG.info("Changing type of rank {} to {}", rank.getName(), defaultRank.getType());
                    rank.setType(defaultRank.getType());
                }
            }
            rank.permissionsContainer.addAll(defaultRank.permissionsContainer);

            saveRank(rank);
        }

        for(int i = 0; i < town.ranksContainer.size(); i++) {
            Rank rank = town.ranksContainer.get(i);
            if(!Rank.defaultRanks.contains(rank.getName())) {
                LOG.info("Deleting rank {} from town {}", rank.getName(), town.getName());
                deleteRank(rank);
                i--;
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
            LOG.fatal(sql);
            LOG.error(ExceptionUtils.getStackTrace(e));
        }
        return statement;
    }
}
