package mytown.datasource;

import com.google.common.collect.Lists;
import mytown.config.Config;
import mytown.core.utils.config.ConfigProperty;
import mytown.entities.*;

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
    @ConfigProperty(category="datasource", comment="The prefix of each of the tables. <prefix>tablename")
    protected String prefix = "";

    @ConfigProperty(category = "datasource", comment = "User defined properties to be passed to the connection.\nFormat: key=value;key=value...")
    protected String[] userProperties = {};

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
        } catch(Exception ex) {
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
            log.error("Failed to update database!", e);
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
                    } catch(SQLException ex) { } // Ignore since we are just closing an old connection
                    conn = null;
                }

                conn = DriverManager.getConnection(dsn, dbProperties);

                if (conn == null || conn.isClosed()) {
                    return false;
                }
            }
            return true;
        } catch(SQLException ex) {
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
     * @return
     */
    protected abstract String getDriver();

    /* ----- Read ----- */

    @Override
    public boolean loadAll() {
        return super.loadAll() && loadResidentsToTowns() && loadTownsToNations();
    }

    @Override
    protected boolean loadTowns() {
        try {
            PreparedStatement loadTownsStatement = prepare("SELECT * FROM " + prefix + "Towns", true);
            ResultSet rs = loadTownsStatement.executeQuery();

            while(rs.next()) {
                Town town = new Town(rs.getString("name"));
                towns.put(town.getName(), town);
            }
        } catch(SQLException e) {
            log.error("Failed to load Towns!", e);
            return false;
        }

        return true;
    }

    @Override
    protected boolean loadBlocks() {
        try {
            PreparedStatement loadBlocksStatement = prepare("", true);
            ResultSet rs = loadBlocksStatement.executeQuery();

            while(rs.next()) {
                Town town = towns.get(rs.getString("townName"));
                if (town == null) {
                    log.warn("Failed to load Block (%s, %s, %s) due to missing Town (%s)", rs.getInt("dim"), rs.getInt("x"), rs.getInt("z"), rs.getString("townName"));
                    continue; // TODO Should I just return out?
                }
                Block block = new Block(rs.getInt("dim"), rs.getInt("x"), rs.getInt("z"), town);
                blocks.put(block.getKey(), block);
            }
        } catch(SQLException e) {
            log.error("Failed to load blocks!", e);
            return false;
        }

        return true;
    }

    @Override
    protected boolean loadRanks() {
        try {
            try {
                getConnection().setAutoCommit(false);
                PreparedStatement loadRanksStatement = prepare("SELECT * FROM " + prefix + "Ranks", true);
                ResultSet rs = loadRanksStatement.executeQuery();
                while (rs.next()) {
                    Town town = towns.get(rs.getString("townName"));
                    if (town == null) {
                        log.warn("Failed to load Rank (%s) due to missing Town (%s)", rs.getString("name"), rs.getString("townName"));
                        continue; // TODO Should I just return out?
                    }
                    Rank rank = new Rank(rs.getString("name"), town);
                    PreparedStatement loadRankPermsStatement = prepare("SELECT * FROM " + prefix + "RankPermissions WHERE rank=?", true);
                    loadRankPermsStatement.setString(1, rank.getName());
                    ResultSet rs2 = loadRankPermsStatement.executeQuery();
                    while (rs2.next()) {
                        rank.addPermission(rs2.getString("node"));
                    }
                    ranks.put(rank.getKey(), rank);
                }
            } catch(SQLException e) {
                log.error("Failed to load a rank!", e);
                getConnection().rollback();
            } finally {
                getConnection().setAutoCommit(true);
            }
        } catch(SQLException e) {
            log.error("Failed to load Ranks!", e);
            return false;
        }
        return true;
    }

    @Override
    protected boolean loadResidents() {
        try {
            PreparedStatement loadResidentsStatement = prepare("SELECT * FROM " + prefix + "Residents", true);
            ResultSet rs = loadResidentsStatement.executeQuery();

            while(rs.next()) {
                Resident res = new Resident(rs.getString("uuid"), rs.getString("name"), rs.getTimestamp("joined"), rs.getTimestamp("lastOnline"));
                this.residents.put(res.getUUID().toString(), res);
            }
        } catch(SQLException e) {
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

            while(rs.next()) {
                Town town = towns.get(rs.getString("townName"));
                if (town == null) {
                    log.error("Failed to load Plot (%s) due to missing Town (%s)", rs.getString("name"), rs.getString("townName"));
                    continue; // TODO Should I just return out?
                }
                Plot plot = new Plot(rs.getString("name"), town, rs.getInt("dim"), rs.getInt("x1"), rs.getInt("y1"), rs.getInt("z1"), rs.getInt("x2"), rs.getInt("y2"), rs.getInt("z2"));
                plots.put(plot.getKey(), plot);
            }
        } catch(SQLException e) {
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
                nations.put(nation.getName(), nation);
            }
        } catch(SQLException e) {
            log.error("Failed to load Nations!", e);
            return false;
        }

        return true;
    }

    protected boolean loadResidentsToTowns() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "ResidentsToTowns", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Resident res = residents.get(rs.getString("resident"));
                Town town = towns.get(rs.getString("town"));
                if (res == null || town == null) {
                    log.warn("Failed to link Resident %s to Town %s. Skipping!", rs.getString("resident"), rs.getString("town"));
                    continue;
                }
                Rank rank = ranks.get(String.format("%s;%s", town.getName(), rs.getString("rank")));
                if (rank == null) {
                    log.warn("Failed to link Resident %s to Town %s because of unknown Rank %s. Skipping!", rs.getString("resident"), rs.getString("town"), rs.getString("rank"));
                    continue;
                }
                town.addResident(res, rank);
                res.addTown(town);
            }
        } catch(SQLException e) {
            log.error("Failed to link Residents to Towns!", e);
            return false;
        }

        return true;
    }

    protected boolean loadTownsToNations() {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "TownsToNations", true);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Town town = towns.get("");
                Nation nation = nations.get("");
                if (town == null || nation == null) {
                    log.warn("Failed to link Town %s to Nation %s. Skipping!", rs.getString("town"), rs.getString("nation"));
                    continue;
                }
                nation.addTown(town);
                nation.promoteTown(town, Nation.Rank.parse(rs.getString("rank")));
                town.setNation(nation);
            }
        } catch(SQLException e) {
            log.error("Failed to link Towns to Nations!", e);
            return false;
        }

        return true;
    }

    /* ----- Save ----- */

    @Override
    public boolean saveTown(Town town) {
        try {
            if (towns.containsValue(town)) { // Update
                if (town.getOldName() != null) { // Rename Town
                    PreparedStatement updateStatement = prepare("UPDATE " + prefix + "Towns SET name=? WHERE name=?", true);
                    updateStatement.setString(1, town.getName());
                    updateStatement.setString(2, town.getOldName());
                    updateStatement.executeUpdate();

                    // Need to move the Town in the map from the old name to the new
                    towns.remove(town.getOldName());
                    towns.put(town.getName(), town);

                    town.resetOldName();
                }
                // TODO Link any new Residents to the given Town
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Towns (name) VALUES(?)", true);
                insertStatement.setString(1, town.getName());
                insertStatement.executeUpdate();

                // Put the Town in the Map
                towns.put(town.getName(), town);
            }
        } catch(SQLException e) {
            log.error("Failed to save Town %s!", e, town.getName());
            return false;
        }

        return true;
    }

    @Override
    public boolean saveBlock(Block block) {
        try {
            if (blocks.containsValue(block)) { // Update
                // TODO Update Block (If needed?)
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Blocks (dim, x, z, towName) VALUES (?, ?, ?, ?)", true);
                insertStatement.setInt(1, block.getDim());
                insertStatement.setInt(2, block.getX());
                insertStatement.setInt(3, block.getZ());
                insertStatement.setString(4, block.getTown().getName());
                insertStatement.executeUpdate();

                // Put the Block in the Map
                blocks.put(block.getKey(), block);
            }
        } catch(SQLException e) {
            log.error("Failed to save Block %s!", e, block.getKey());
            return false;
        }

        return true;
    }

    @Override
    public boolean saveRank(Rank rank) { // TODO Insert any new permissions to the RankPermission table
        try {
            if (ranks.containsValue(rank)) { // Update
                // TODO Update
            } else { // Insert
                try {
                    getConnection().setAutoCommit(false);

                    PreparedStatement insertRankStatement = prepare("INSERT INTO " + prefix + "Ranks (name, townName) VALUES(?, ?)", true);
                    insertRankStatement.setString(1, rank.getName());
                    insertRankStatement.setString(2, rank.getTown().getName());
                    insertRankStatement.executeUpdate();

                    if (rank.getPermissions().size() > 0) {
                        PreparedStatement insertRankPermStatement = prepare("INSERT INTO " + prefix + "RankPermissions (node, rank) VALUES(?, ?)", true);
                        for (String perm : rank.getPermissions()) {
                            insertRankPermStatement.setString(1, perm);
                            insertRankPermStatement.setString(2, rank.getName());
                            insertRankPermStatement.addBatch();
                        }

                        insertRankPermStatement.executeBatch();
                    }

                    // Put the Rank in the Map
                    ranks.put(rank.getKey(), rank);
                } catch(SQLException e) {
                    log.error("Failed to insert Rank %s", rank.getKey());
                    getConnection().rollback();
                    return false;
                } finally {
                    getConnection().setAutoCommit(true);
                }
            }
        } catch(SQLException e) {
            log.error("Failed to save Rank %s!", e, rank.getKey());
            return false;
        }

        return true;
    }

    @Override
    public boolean saveResident(Resident resident) {
        try {
            if (residents.containsValue(resident)) { // Update
                PreparedStatement updateStatement = prepare("UPDATE " + prefix + "Residents SET name=?, lastOnline=? WHERE uuid=?", true);
                updateStatement.setString(1, resident.getPlayerName());
                updateStatement.setTimestamp(2, new Timestamp(resident.getLastOnline().getTime())); // Stupid hack...
                updateStatement.setString(3, resident.getUUID().toString());
                updateStatement.executeUpdate();
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Residents (uuid, name) VALUES(?, ?)", true);
                insertStatement.setString(1, resident.getUUID().toString());
                insertStatement.setString(2, resident.getPlayerName());
                insertStatement.executeUpdate();

                // Put the Resident in the Map
                residents.put(resident.getUUID().toString(), resident);
            }
        } catch(SQLException e) {
            log.error("Failed to save resident %s!", e, resident.getUUID());
            return false;
        }

        return true;
    }

    @Override
    public boolean savePlot(Plot plot) {
        try {
            if (plots.containsValue(plot)) { // Update
                // TODO Update Plot
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Plots (name, dim, x1, y1, z1, x2, y2, z2, townName) VALUES(?, ?, ?, ?, ?, ?, ?, ?)", true);
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

                // Put the Plot in the Map
                plots.put(plot.getKey(), plot);
            }
        } catch(SQLException e) {
            log.error("Failed to save Plot %s!", e, plot.getKey());
            return false;
        }

        return true;
    }

    @Override
    public boolean saveNation(Nation nation) { // TODO Link any new Towns to the given Nation
        try {
            if (nations.containsValue(nation)) { // Update
                // TODO Update Nation (If needed?)
            } else { // Insert
                PreparedStatement insertStatement = prepare("INSERT INTO " + prefix + "Nations (name) VALUES(?)", true);
                insertStatement.setString(1, nation.getName());
                insertStatement.executeUpdate();

                // Put the Nation in the Map
                nations.put(nation.getName(), nation);
            }
        } catch(SQLException e) {
            log.error("Failed to save Nation %s!", e, nation.getName());
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
            for (Block b : town.getBlocks()) {
                blocks.remove(b.getKey());
            }
            // Remove all Plots owned by the Town
            for (Plot p : town.getPlots()) {
                plots.remove(p.getKey());
            }
            // Remove all Ranks owned by this Town
            for (Rank r : town.getRanks()) {
                ranks.remove(r.getKey());
            }
            // Remove the Town from the Map
            towns.remove(town.getName());
        } catch(SQLException e) {
            log.error("Failed to delete Town %s", e, town.getName());
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteBlock(Block block) {
        try {
            // Delete Block from Datasource
            PreparedStatement deleteBlockStatement = prepare("DELETE FROM " + prefix + "Blocks WHERE dim=?, x=?, z=?", true);
            deleteBlockStatement.setInt(1, block.getDim());
            deleteBlockStatement.setInt(2, block.getX());
            deleteBlockStatement.setInt(3, block.getZ());
            deleteBlockStatement.execute();

            // Delete Plots contained in the Block
            for (Plot p : block.getPlots()) {
                deletePlot(p);
            }
            // Remove Block from Map
            blocks.remove(block.getKey());
        } catch(SQLException e) {
            log.error("Failed to delete Block %s!", e, block.getKey());
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteRank(Rank rank) {
        try {
            // Delete Rank from Datasource
            PreparedStatement deleteRankStatement = prepare("DELETE FROM " + prefix + "Ranks WHERE name=?, townName=?", true);
            deleteRankStatement.setString(1, rank.getName());
            deleteRankStatement.setString(2, rank.getTown().getName());
            deleteRankStatement.execute();

            // Remove Rank from Map
            ranks.remove(rank.getKey());
        } catch(SQLException e) {
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
            residents.remove(resident.getUUID().toString());
        } catch(SQLException e) {
            log.error("Failed to delete Resident %s!", e, resident.getUUID());
            return false;
        }

        return true;
    }

    @Override
    public boolean deletePlot(Plot plot) {
        try {
            // Delete Plot from Datasource
            PreparedStatement deletePlotStatement = prepare("DELETE FROM " + prefix + "Plots WHERE dim=?, x1=?, y1=?, z1=?, x2=?, y2=?, z2=?", true);
            deletePlotStatement.setInt(1, plot.getDim());
            deletePlotStatement.setInt(2, plot.getStartX());
            deletePlotStatement.setInt(3, plot.getStartY());
            deletePlotStatement.setInt(4, plot.getStartZ());
            deletePlotStatement.setInt(4, plot.getEndX());
            deletePlotStatement.setInt(5, plot.getEndY());
            deletePlotStatement.setInt(6, plot.getEndZ());
            deletePlotStatement.execute();

            // Remove Plot from Map
            plots.remove(plot.getKey());
        } catch(SQLException e) {
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
            nations.remove(nation.getName());
        } catch(SQLException e) {
            log.error("Failed to delete Nation $s!", e, nation.getName());
            return false;
        }

        return true;
    }

    /* ----- Helpers ----- */

    /**
     * Returns a PreparedStatement using the given sql
     *
     * @param sql
     * @param returnGenerationKeys
     * @return
     * @throws Exception
     */
    protected PreparedStatement prepare(String sql, boolean returnGenerationKeys) throws SQLException {
        if (!createConnection())
            throw new SQLException("No SQL Connection");
        PreparedStatement statement = conn.prepareStatement(sql, returnGenerationKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);

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

    protected void setupUpdates() { // TODO Move these into a XML/JSON file?
        updates.add(new DBUpdate("07.25.2014.1", "Add Updates Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Updates (" +
                "id VARCHAR(20) NOT NULL," +
                "desc VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(id)" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.2", "Add Residents Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Residents (" +
                "uuid CHAR(36) NOT NULL," +
                "name VARCHAR(240) NOT NULL," +
                "joined DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "lastOnline DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "PRIMARY KEY(uuid)" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.3", "Add Towns Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Towns (" +
                "name VARCHAR(32) NOT NULL," + // TODO Allow larger town names?
                "PRIMARY KEY(name)" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.4", "Add Ranks Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Ranks (" +
                "name VARCHAR(50) NOT NULL," +  // TODO Allow larger rank names?
                "townName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(name, townName)," +
                "FOREIGN KEY(townName) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.5", "Add RankPermissions Table", "CREATE TABLE IF NOT EXISTS " + prefix + "RankPermissions (" +
                "node VARCHAR(100) NOT NULL," +
                "rank VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(rank, node)," +
                "FOREIGN KEY(rank) REFERENCES " + prefix + "Rank(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.6", "Add Blocks Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Blocks (" +
                "dim INT NOT NULL," +
                "x INT NOT NULL," +
                "z INT NOT NULL," +
                "townName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(dim, x, z)," +
                "FOREIGN KEY(townName) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.7", "Add Plots Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Plots (" +
                "name VARCHAR(50) NOT NULL" + // TODO Allow larger Plot names?
                "dim INT NOT NULL," +
                "x1 INT NOT NULL," +
                "y1 INT NOT NULL," +
                "z1 INT NOT NULL," +
                "x2 INT NOT NULL," +
                "y2 INT NOT NULL," +
                "z2 INT NOT NULL," +
                "townName VARCHAR(32) NOT NULL," +
                "owner CHAR(36) DEFAULT NULL," + // TODO Allow multiple owners?
                "PRIMARY KEY(dim, x1, y1, z1, x2, y2, z2)," +
                "FOREIGN KEY(townName) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(owner) REFERENCES " + prefix + "Residents(uuid) ON DELETE SET NULL" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.8", "Add Nations Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Nations (" +
                "name VARCHAR(32) NOT NULL," + // TODO Allow larger nation names?
                "PRIMARY KEY(name)" +
                ");"));

        // Create "Join" Tables
        updates.add(new DBUpdate("08.07.2014.1", "Add ResidentsToTowns Table", "CREATE TABLE IF NOT EXISTS " + prefix + "ResidentsToTowns (" +
                "resident CHAR(36)," +
                "town VARCHAR(50)," +
                "rank VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(resident, town)," +
                "FOREIGN KEY(resident) REFERENCES " + prefix + "Residents(uuid) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(town) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(rank) REFERENCES " + prefix + "Ranks(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.07.2014.2", "Add TownsToNations Table", "CREATE TABLE IF NOT EXISTS " + prefix + "TownsToNations (" +
                "town VARCHAR(50)," +
                "nation VARCHAR(50)," +
                "rank CHAR(1) DEFAULT 'T'," +
                "PRIMARY KEY(town, nation)," +
                "FOREIGN KEY(town) REFERENCES " + prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(nation) REFERENCES " + prefix + "Nations(name) ON DELETE CASCADE ON UPDATE CASCADE," +
                ");"));
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
            statement = prepare("SELECT Id FROM " + prefix + "Updates", false);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                ids.add(rs.getString("Id"));
            }
        } catch (Exception e) { } // Ignore. Just missing the updates table for now

        try {
            conn.setAutoCommit(false); // Disable auto-commit to allow us to wrap updates in transactions

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
                    statement = prepare("INSERT INTO " + prefix + "Updates (Id,Desc) VALUES(?,?)", true);
                    statement.setString(1, update.id);
                    statement.setString(2, update.desc);
                    statement.executeUpdate();

                    conn.commit(); // Commit the transaction
                } catch(SQLException e) {
                    log.error("Update (%s - %s) failed to apply!", e, update.id, update.desc);
                    try {
                        conn.rollback();
                    } catch(SQLException e2) {
                        log.error("Rollback failed!", e);
                    }
                }
            }
        } catch(SQLException e) {
            log.error("Updates failed to apply!", e);
            // TODO Do I need to do conn.rollback() here? I don't think so since I am doing that for each update above
        } finally {
            conn.setAutoCommit(true); // Restore auto-commit
        }
    }
}
