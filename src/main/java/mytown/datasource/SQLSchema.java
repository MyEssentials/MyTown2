package mytown.datasource;

import com.google.common.collect.Lists;
import mytown.MyTown;
import mytown.config.Config;
import mytown.entities.Rank;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLSchema {
    protected static Logger LOG = MyTown.instance.LOG;

    protected MyTownDatasourceSQL sqlDatasource;

    protected List<DBUpdate> updates = new ArrayList<DBUpdate>();

    public SQLSchema(MyTownDatasourceSQL sqlDatasource) {
        this.sqlDatasource = sqlDatasource;
        setupUpdates();
    }

    /**
     * Holds an SQL statement to be run to update the tables in the DB
     *
     * @author Joe Goett
     */
    protected class DBUpdate {
        /**
         * Formatted mm.dd.yyyy.e where e increments by 1 for every update released on the same date
         */
        private final String id;
        private final String desc;
        private final String sql;

        public DBUpdate(String id, String desc, String sql) {
            this.id = id;
            this.desc = desc;
            this.sql = sql;
        }
    }

    /**
     * Setup all the updates
     */
    protected void setupUpdates() {
        updates.add(new DBUpdate("07.25.2014.1", "Add Updates Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "Updates (" +
                "id VARCHAR(20) NOT NULL," +
                "description VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(id)" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.2", "Add Residents Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "Residents (" +
                "uuid CHAR(36) NOT NULL," +
                "name VARCHAR(240) NOT NULL," +
                "joined BIGINT NOT NULL," +
                "lastOnline BIGINT NOT NULL," +
                "PRIMARY KEY(uuid)" +
                ");"));
        updates.add(new DBUpdate("10.05.2014", "Add Worlds", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "Worlds(" +
                "dim INT," +
                "PRIMARY KEY(dim))"));
        updates.add(new DBUpdate("07.25.2014.3", "Add Towns Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "Towns (" +
                "name VARCHAR(32) NOT NULL," + // TODO Allow larger town names?
                "isAdminTown BOOLEAN, " +
                "spawnDim INT NOT NULL, " +
                "spawnX FLOAT NOT NULL, " +
                "spawnY FLOAT NOT NULL, " +
                "spawnZ FLOAT NOT NULL, " +
                "cameraYaw FLOAT NOT NULL, " +
                "cameraPitch FLOAT NOT NULL, " +
                "PRIMARY KEY(name), " +
                "FOREIGN KEY(spawnDim) REFERENCES " + this.sqlDatasource.prefix + " Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.4", "Add Ranks Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "Ranks (" +
                "name VARCHAR(50) NOT NULL," +  // TODO Allow larger rank names?
                "townName VARCHAR(32) NOT NULL," +
                "isDefault BOOLEAN, " +
                "PRIMARY KEY(name, townName)," +
                "FOREIGN KEY(townName) REFERENCES " + this.sqlDatasource.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.5", "Add RankPermissions Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "RankPermissions (" +
                "node VARCHAR(100) NOT NULL," +
                "rank VARCHAR(50) NOT NULL," +
                "townName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(node, rank, townName)," +
                "FOREIGN KEY(rank, townName) REFERENCES " + this.sqlDatasource.prefix + "Ranks(name, townName) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.6", "Add Blocks Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "Blocks (" +
                "dim INT NOT NULL," +
                "x INT NOT NULL," +
                "z INT NOT NULL," +
                "townName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(dim, x, z)," +
                "FOREIGN KEY(townName) REFERENCES " + this.sqlDatasource.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + this.sqlDatasource.prefix + "Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.7", "Add Plots Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "Plots (" +
                "ID INTEGER NOT NULL " + this.sqlDatasource.autoIncrement + "," + // Just because it's a pain with this many primary keys
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
                "FOREIGN KEY(townName) REFERENCES " + this.sqlDatasource.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + this.sqlDatasource.prefix + "Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.8", "Add Nations Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "Nations (" +
                "name VARCHAR(32) NOT NULL," + // TODO Allow larger nation names?
                "PRIMARY KEY(name)" +
                ");"));

        // Create "Join" Tables
        updates.add(new DBUpdate("08.07.2014.1", "Add ResidentsToTowns Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "ResidentsToTowns (" +
                "resident CHAR(36) NOT NULL," +
                "town VARCHAR(32) NOT NULL," +
                "rank VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(resident, town)," +
                "FOREIGN KEY(resident) REFERENCES " + this.sqlDatasource.prefix + "Residents(uuid) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(rank, town) REFERENCES " + this.sqlDatasource.prefix + "Ranks(name, townName) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.07.2014.2", "Add TownsToNations Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "TownsToNations (" +
                "town VARCHAR(50)," +
                "nation VARCHAR(50)," +
                "rank CHAR(1) DEFAULT 'T'," +
                "PRIMARY KEY(town, nation)," +
                "FOREIGN KEY(town) REFERENCES " + this.sqlDatasource.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(nation) REFERENCES " + this.sqlDatasource.prefix + "Nations(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.26.2014.1", "Add TownFlags Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "TownFlags (" +
                "name VARCHAR(50) NOT NULL," +
                "serializedValue VARCHAR(400), " +
                "townName VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(name, townName)," +
                "FOREIGN KEY(townName) REFERENCES " + this.sqlDatasource.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.30.2014.1", "Add PlotFlags Table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "PlotFlags (" +
                "name VARCHAR(50) NOT NULL," +
                "serializedValue VARCHAR(400), " +
                "plotID INT NOT NULL," +
                "PRIMARY KEY(name, plotID)," +
                "FOREIGN KEY(plotID) REFERENCES " + this.sqlDatasource.prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.31.2014.1", "Add ResidentsToPlots", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix +
                "ResidentsToPlots(" +
                "resident varchar(36) NOT NULL, " +
                "plotID INT NOT NULL, " +
                "isOwner boolean, " + // false if it's ONLY whitelisted, if neither then shouldn't be in this list
                "PRIMARY KEY(resident, plotID), " +
                "FOREIGN KEY(resident) REFERENCES " + this.sqlDatasource.prefix + "Residents(UUID) ON DELETE CASCADE, " +
                "FOREIGN KEY(plotID) REFERENCES " + this.sqlDatasource.prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("09.04.2014.1", "Add BlockWhitelists", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix +
                "BlockWhitelists(" +
                "ID INTEGER NOT NULL " + this.sqlDatasource.autoIncrement + ", " +
                "dim INT NOT NULL, " +
                "x INT NOT NULL, " +
                "y INT NOT NULL, " +
                "z INT NOT NULL, " +
                "townName VARCHAR(50), " +
                "flagName VARCHAR(50) NOT NULL, " +
                "PRIMARY KEY(ID), " +
                "FOREIGN KEY(flagName, townName) REFERENCES " + this.sqlDatasource.prefix + "TownFlags(name, townName) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + this.sqlDatasource.prefix + "Worlds(dim) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("09.11.2014.1", "Add SelectedTown", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix +
                "SelectedTown(" +
                "resident CHAR(36), " +
                "townName VARCHAR(50)," +
                "PRIMARY KEY(resident), " +
                "FOREIGN KEY(resident) REFERENCES " + this.sqlDatasource.prefix + "Residents(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(townName) REFERENCES " + this.sqlDatasource.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("09.19.2014.1", "Add Friends", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "Friends(" +
                "resident1 CHAR(36)," +
                "resident2 CHAR(36)," +
                "PRIMARY KEY(resident1, resident2)," +
                "FOREIGN KEY(resident1) REFERENCES " + this.sqlDatasource.prefix + "Residents(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(resident2) REFERENCES " + this.sqlDatasource.prefix + "Residents(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("09.19.2014.2", "Add FriendRequests", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "FriendRequests(" +
                "resident CHAR(36)," +
                "residentTarget CHAR(36)," +
                "PRIMARY KEY(resident, residentTarget)," +
                "FOREIGN KEY(resident) REFERENCES " + this.sqlDatasource.prefix + "Residents(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(residentTarget) REFERENCES " + this.sqlDatasource.prefix + "Residents(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("10.02.2014", "Add TownInvites", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "TownInvites(" +
                "resident CHAR(36)," +
                "townName VARCHAR(50), " +
                "PRIMARY KEY(resident, townName)," +
                "FOREIGN KEY(resident) REFERENCES " + this.sqlDatasource.prefix + "Residents(UUID) ON DELETE CASCADE, " +
                "FOREIGN KEY(townName) REFERENCES " + this.sqlDatasource.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE)"));

        // Table Modifications
        updates.add(new DBUpdate("10.18.2014.1", "Add 'extraBlocks' to towns", "ALTER TABLE " + this.sqlDatasource.prefix +
                "Towns ADD extraBlocks INTEGER DEFAULT 0"));

        updates.add(new DBUpdate("10.23.2014.1", "Add 'maxPlots' to towns", "ALTER TABLE " + this.sqlDatasource.prefix +
                "Towns ADD maxPlots INTEGER DEFAULT " + Config.defaultMaxPlots + ""));

        updates.add(new DBUpdate("11.4.2014.1", "Add 'extraBlocks to residents", "ALTER TABLE " + this.sqlDatasource.prefix +
                "Residents ADD extraBlocks INTEGER DEFAULT 0;"));
        updates.add(new DBUpdate("3.22.2014.1", "Add 'BlockOwners' table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "BlockOwners(" +
                "resident CHAR(36), " +
                "dim INT NOT NULL, " +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL, " +
                "FOREIGN KEY(resident) REFERENCES " + this.sqlDatasource.prefix + "Residents(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("3.27.2014.1", "Add 'TownBanks' table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "TownBanks(" +
                "townName VARCHAR(50), " +
                "amount INT NOT NULL, " +
                "PRIMARY KEY(townName), " +
                "FOREIGN KEY(townName) REFERENCES " + this.sqlDatasource.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("3.27.2014.2", "Add 'PlotBanks' table", "CREATE TABLE IF NOT EXISTS " + this.sqlDatasource.prefix + "PlotBanks(" +
                "plotID INT NOT NULL, " +
                "amount INT NOT NULL, " +
                "PRIMARY KEY(plotID), " +
                "FOREIGN KEY(plotID) REFERENCES " + this.sqlDatasource.prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("4.1.2015.1", "Add 'daysNotPaid' to TownBanks", "ALTER TABLE " + this.sqlDatasource.prefix +
                "TownBanks ADD daysNotPaid INTEGER DEFAULT 0"));
        updates.add(new DBUpdate("4.12.2015.1", "Add 'isFarClaim' to Blocks", "ALTER TABLE " + this.sqlDatasource.prefix +
                "Blocks ADD isFarClaim boolean DEFAULT false"));
        updates.add(new DBUpdate("4.12.2015.2", "Add 'maxFarClaims' to Towns", "ALTER TABLE " + this.sqlDatasource.prefix +
                "Towns ADD maxFarClaims INTEGER DEFAULT " + Config.maxFarClaims));
        updates.add(new DBUpdate("4.12.2015.3", "Add 'pricePaid' to Blocks", "ALTER TABLE " + this.sqlDatasource.prefix +
                "Blocks ADD pricePaid INTEGER DEFAULT " + Config.costAmountClaim));
        updates.add(new DBUpdate("8.21.2015.1", "Add 'type' to Ranks", "ALTER TABLE " + this.sqlDatasource.prefix +
                "Ranks ADD type VARCHAR(50) DEFAULT '" + Rank.Type.REGULAR + "'"));
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
            statement = this.sqlDatasource.prepare("SELECT id FROM " + this.sqlDatasource.prefix + "Updates", false);
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
                LOG.info("Running update {} - {}", update.id, update.desc);
                statement = this.sqlDatasource.prepare(update.sql, false);
                statement.execute();

                // Insert the update key so as to not run the update again
                statement = this.sqlDatasource.prepare("INSERT INTO " + this.sqlDatasource.prefix + "Updates (id,description) VALUES(?,?)", true);
                statement.setString(1, update.id);
                statement.setString(2, update.desc);
                statement.executeUpdate();
            } catch (SQLException e) {
                LOG.error("Update ({} - {}) failed to apply!", update.id, update.desc);
                LOG.error(ExceptionUtils.getStackTrace(e));
                throw e; // Throws back up to force safemode
            }
        }
    }
}
