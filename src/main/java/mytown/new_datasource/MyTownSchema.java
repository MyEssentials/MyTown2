package mytown.new_datasource;

import myessentials.datasource.Schema;
import myessentials.datasource.bridge.BridgeSQL;
import mytown.config.Config;
import mytown.entities.Rank;

public class MyTownSchema extends Schema {
    @Override
    public void initializeUpdates(BridgeSQL bridge) {
        updates.add(new DBUpdate("07.25.2014.1", "Add Updates Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Updates (" +
                "id VARCHAR(20) NOT NULL," +
                "description VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(id)" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.2", "Add Residents Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Residents (" +
                "uuid CHAR(36) NOT NULL," +
                "name VARCHAR(240) NOT NULL," +
                "joined BIGINT NOT NULL," +
                "lastOnline BIGINT NOT NULL," +
                "PRIMARY KEY(uuid)" +
                ");"));
        updates.add(new DBUpdate("10.05.2014", "Add Worlds", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Worlds(" +
                "dim INT," +
                "PRIMARY KEY(dim))"));
        updates.add(new DBUpdate("07.25.2014.3", "Add Towns Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Towns (" +
                "name VARCHAR(32) NOT NULL," + // TODO Allow larger town names?
                "isAdminTown BOOLEAN, " +
                "spawnDim INT NOT NULL, " +
                "spawnX FLOAT NOT NULL, " +
                "spawnY FLOAT NOT NULL, " +
                "spawnZ FLOAT NOT NULL, " +
                "cameraYaw FLOAT NOT NULL, " +
                "cameraPitch FLOAT NOT NULL, " +
                "PRIMARY KEY(name), " +
                "FOREIGN KEY(spawnDim) REFERENCES " + bridge.prefix + " Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.4", "Add Ranks Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Ranks (" +
                "name VARCHAR(50) NOT NULL," +  // TODO Allow larger rank names?
                "townName VARCHAR(32) NOT NULL," +
                "isDefault BOOLEAN, " +
                "PRIMARY KEY(name, townName)," +
                "FOREIGN KEY(townName) REFERENCES " + bridge.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.5", "Add RankPermissions Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "RankPermissions (" +
                "node VARCHAR(100) NOT NULL," +
                "rank VARCHAR(50) NOT NULL," +
                "townName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(node, rank, townName)," +
                "FOREIGN KEY(rank, townName) REFERENCES " + bridge.prefix + "Ranks(name, townName) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.6", "Add Blocks Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Blocks (" +
                "dim INT NOT NULL," +
                "x INT NOT NULL," +
                "z INT NOT NULL," +
                "townName VARCHAR(32) NOT NULL," +
                "PRIMARY KEY(dim, x, z)," +
                "FOREIGN KEY(townName) REFERENCES " + bridge.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + bridge.prefix + "Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.7", "Add Plots Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Plots (" +
                "ID INTEGER NOT NULL " + bridge.getAutoIncrement() + "," + // Just because it's a pain with this many primary keys
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
                "FOREIGN KEY(townName) REFERENCES " + bridge.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + bridge.prefix + "Worlds(dim) ON DELETE CASCADE" +
                ");"));
        updates.add(new DBUpdate("07.25.2014.8", "Add Nations Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Nations (" +
                "name VARCHAR(32) NOT NULL," + // TODO Allow larger nation names?
                "PRIMARY KEY(name)" +
                ");"));

        // Create "Join" Tables
        updates.add(new DBUpdate("08.07.2014.1", "Add ResidentsToTowns Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "ResidentsToTowns (" +
                "resident CHAR(36) NOT NULL," +
                "town VARCHAR(32) NOT NULL," +
                "rank VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(resident, town)," +
                "FOREIGN KEY(resident) REFERENCES " + bridge.prefix + "Residents(uuid) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(rank, town) REFERENCES " + bridge.prefix + "Ranks(name, townName) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.07.2014.2", "Add TownsToNations Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "TownsToNations (" +
                "town VARCHAR(50)," +
                "nation VARCHAR(50)," +
                "rank CHAR(1) DEFAULT 'T'," +
                "PRIMARY KEY(town, nation)," +
                "FOREIGN KEY(town) REFERENCES " + bridge.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE," +
                "FOREIGN KEY(nation) REFERENCES " + bridge.prefix + "Nations(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.26.2014.1", "Add TownFlags Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "TownFlags (" +
                "name VARCHAR(50) NOT NULL," +
                "serializedValue VARCHAR(400), " +
                "townName VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(name, townName)," +
                "FOREIGN KEY(townName) REFERENCES " + bridge.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.30.2014.1", "Add PlotFlags Table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "PlotFlags (" +
                "name VARCHAR(50) NOT NULL," +
                "serializedValue VARCHAR(400), " +
                "plotID INT NOT NULL," +
                "PRIMARY KEY(name, plotID)," +
                "FOREIGN KEY(plotID) REFERENCES " + bridge.prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE" +
                ");"));
        updates.add(new DBUpdate("08.31.2014.1", "Add ResidentsToPlots", "CREATE TABLE IF NOT EXISTS " + bridge.prefix +
                "ResidentsToPlots(" +
                "resident varchar(36) NOT NULL, " +
                "plotID INT NOT NULL, " +
                "isOwner boolean, " + // false if it's ONLY whitelisted, if neither then shouldn't be in this list
                "PRIMARY KEY(resident, plotID), " +
                "FOREIGN KEY(resident) REFERENCES " + bridge.prefix + "Residents(UUID) ON DELETE CASCADE, " +
                "FOREIGN KEY(plotID) REFERENCES " + bridge.prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("09.04.2014.1", "Add BlockWhitelists", "CREATE TABLE IF NOT EXISTS " + bridge.prefix +
                "BlockWhitelists(" +
                "ID INTEGER NOT NULL " + bridge.getAutoIncrement() + ", " +
                "dim INT NOT NULL, " +
                "x INT NOT NULL, " +
                "y INT NOT NULL, " +
                "z INT NOT NULL, " +
                "townName VARCHAR(50), " +
                "flagName VARCHAR(50) NOT NULL, " +
                "PRIMARY KEY(ID), " +
                "FOREIGN KEY(flagName, townName) REFERENCES " + bridge.prefix + "TownFlags(name, townName) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY(dim) REFERENCES " + bridge.prefix + "Worlds(dim) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("09.11.2014.1", "Add SelectedTown", "CREATE TABLE IF NOT EXISTS " + bridge.prefix +
                "SelectedTown(" +
                "resident CHAR(36), " +
                "townName VARCHAR(50)," +
                "PRIMARY KEY(resident), " +
                "FOREIGN KEY(resident) REFERENCES " + bridge.prefix + "Residents(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(townName) REFERENCES " + bridge.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("09.19.2014.1", "Add Friends", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "Friends(" +
                "resident1 CHAR(36)," +
                "resident2 CHAR(36)," +
                "PRIMARY KEY(resident1, resident2)," +
                "FOREIGN KEY(resident1) REFERENCES " + bridge.prefix + "Residents(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(resident2) REFERENCES " + bridge.prefix + "Residents(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("09.19.2014.2", "Add FriendRequests", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "FriendRequests(" +
                "resident CHAR(36)," +
                "residentTarget CHAR(36)," +
                "PRIMARY KEY(resident, residentTarget)," +
                "FOREIGN KEY(resident) REFERENCES " + bridge.prefix + "Residents(UUID) ON DELETE CASCADE," +
                "FOREIGN KEY(residentTarget) REFERENCES " + bridge.prefix + "Residents(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("10.02.2014", "Add TownInvites", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "TownInvites(" +
                "resident CHAR(36)," +
                "townName VARCHAR(50), " +
                "PRIMARY KEY(resident, townName)," +
                "FOREIGN KEY(resident) REFERENCES " + bridge.prefix + "Residents(UUID) ON DELETE CASCADE, " +
                "FOREIGN KEY(townName) REFERENCES " + bridge.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE)"));

        // Table Modifications
        updates.add(new DBUpdate("10.18.2014.1", "Add 'extraBlocks' to towns", "ALTER TABLE " + bridge.prefix +
                "Towns ADD extraBlocks INTEGER DEFAULT 0"));

        updates.add(new DBUpdate("10.23.2014.1", "Add 'maxPlots' to towns", "ALTER TABLE " + bridge.prefix +
                "Towns ADD maxPlots INTEGER DEFAULT " + Config.instance.defaultMaxPlots.get() + ""));

        updates.add(new DBUpdate("11.4.2014.1", "Add 'extraBlocks to residents", "ALTER TABLE " + bridge.prefix +
                "Residents ADD extraBlocks INTEGER DEFAULT 0;"));
        updates.add(new DBUpdate("3.22.2014.1", "Add 'BlockOwners' table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "BlockOwners(" +
                "resident CHAR(36), " +
                "dim INT NOT NULL, " +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL, " +
                "FOREIGN KEY(resident) REFERENCES " + bridge.prefix + "Residents(UUID) ON DELETE CASCADE)"));
        updates.add(new DBUpdate("3.27.2014.1", "Add 'TownBanks' table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "TownBanks(" +
                "townName VARCHAR(50), " +
                "amount INT NOT NULL, " +
                "PRIMARY KEY(townName), " +
                "FOREIGN KEY(townName) REFERENCES " + bridge.prefix + "Towns(name) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("3.27.2014.2", "Add 'PlotBanks' table", "CREATE TABLE IF NOT EXISTS " + bridge.prefix + "PlotBanks(" +
                "plotID INT NOT NULL, " +
                "amount INT NOT NULL, " +
                "PRIMARY KEY(plotID), " +
                "FOREIGN KEY(plotID) REFERENCES " + bridge.prefix + "Plots(ID) ON DELETE CASCADE ON UPDATE CASCADE)"));
        updates.add(new DBUpdate("4.1.2015.1", "Add 'daysNotPaid' to TownBanks", "ALTER TABLE " + bridge.prefix +
                "TownBanks ADD daysNotPaid INTEGER DEFAULT 0"));
        updates.add(new DBUpdate("4.12.2015.1", "Add 'isFarClaim' to Blocks", "ALTER TABLE " + bridge.prefix +
                "Blocks ADD isFarClaim boolean DEFAULT false"));
        updates.add(new DBUpdate("4.12.2015.2", "Add 'maxFarClaims' to Towns", "ALTER TABLE " + bridge.prefix +
                "Towns ADD maxFarClaims INTEGER DEFAULT " + Config.instance.maxFarClaims.get()));
        updates.add(new DBUpdate("4.12.2015.3", "Add 'pricePaid' to Blocks", "ALTER TABLE " + bridge.prefix +
                "Blocks ADD pricePaid INTEGER DEFAULT " + Config.instance.costAmountClaim.get()));
        updates.add(new DBUpdate("8.21.2015.1", "Add 'type' to Ranks", "ALTER TABLE " + bridge.prefix +
                "Ranks ADD type VARCHAR(50) DEFAULT '" + Rank.Type.REGULAR + "'"));
        updates.add(new DBUpdate("11.11.2015.1", "Add 'extraFarClaims' to Towns", "ALTER TABLE " + bridge.prefix +
                "Towns ADD extraFarClaims INTEGER DEFAULT 0"));
        updates.add(new DBUpdate("12.16.2015.1", "Add 'fakePlayer to residents", "ALTER TABLE " + bridge.prefix +
                "Residents ADD fakePlayer BOOLEAN DEFAULT false"));
    }
}
