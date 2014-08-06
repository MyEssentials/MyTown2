package mytown.x_datasource.impl;

import java.sql.DriverManager;
import java.util.Properties;

import mytown.core.utils.config.ConfigProperty;
import mytown.x_datasource.types.MyTownDatasource_SQL;

/**
 * Experimental PostgreSQL Datasource. Use with care!
 * 
 * @author Joe Goett
 */
public class MyTownDatasource_postgresql extends MyTownDatasource_SQL {
	// Config
	@ConfigProperty(category="datasource", comment="Username to use when connecting")
	private String username = "";

	@ConfigProperty(category="datasource", comment="Password to use when connecting")
	private String password = "";

	@ConfigProperty(category="datasource", comment="Hostname:Port of the db server")
	private String host = "localhost";

	@ConfigProperty(category="datasource", comment="The database name")
	private String database = "mytown";

	// //////////////////////////////////////
	// Helpers
	// //////////////////////////////////////

	public String getDatabasePath() {
		return "//" + host + "/" + database;
	}

	// //////////////////////////////////////
	// Implementation
	// //////////////////////////////////////

	@Override
	public boolean connect() throws Exception {
		log.warn("PostgreSQL support is experimental! Please backup your database regularly!");

		if (conn != null)
			return true;

		Class.forName("org.postgresql.Driver");

		Properties properties = new Properties();
		properties.put("user", username);
		properties.put("password", password);

		conn = DriverManager.getConnection("jdbc:postgresql:" + getDatabasePath(), properties);
		if (conn == null)
			return false; // TODO Log error?

		return true;
	}

	@Override
	protected void setUpdates() {
		updates.add(new DBUpdate("03.08.2014.1", "Add Updates Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Updates (Id varchar(50) NOT NULL, Code varchar(50) NOT NULL, PRIMARY KEY(Id));"));
		updates.add(new DBUpdate("03.08.2014.2", "Add Towns Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Towns (Id SERIAL NOT NULL, Name varchar(50) NOT NULL, ExtraBlocks int NOT NULL, PRIMARY KEY (Id));"));
		updates.add(new DBUpdate("03.08.2014.3", "Add Residents Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Residents (UUID varchar(255) NOT NULL, IsNPC boolean DEFAULT false, Joined int NOT NULL, LastLogin int NOT NULL, PRIMARY KEY (UUID));")); // MC
																																																															// Version
																																																															// <
																																																															// 1.7
																																																															// UUID
																																																															// is
																																																															// Player
																																																															// name.
																																																															// 1.7
																																																															// >=
																																																															// UUID
																																																															// is
																																																															// Player's
																																																															// UUID
		updates.add(new DBUpdate("03.08.2014.4", "Add Nations Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Nations (Id SERIAL NOT NULL, Name varchar(50) NOT NULL, ExtraBlocks int NOT NULL DEFAULT 0, PRIMARY KEY(Id));"));
		updates.add(new DBUpdate("03.08.2014.5", "Add TownBlocks Table", "CREATE TABLE IF NOT EXISTS " + prefix + "TownBlocks (Id SERIAL NOT NULL, X int NOT NULL, Z int NOT NULL, Dim int NOT NULL, TownId int NOT NULL, PRIMARY KEY(Id), FOREIGN KEY (TownId) REFERENCES " + prefix + "Towns(Id) ON DELETE CASCADE);"));
		updates.add(new DBUpdate("03.15.2014.1", "Add TownPlots Table", "CREATE TABLE IF NOT EXISTS " + prefix + "TownPlots (Id SERIAL NOT NULL, X1 int NOT NULL, Y1 int NOT NULL, Z1 int NOT NULL, X2 int NOT NULL, Y2 int NOT NULL, Z2 int NOT NULL, Dim int NOT NULL, TownId int NOT NULL, Owner varchar(255) DEFAULT NULL, Rank varchar(1) DEFAULT 'R', PRIMARY KEY(Id), FOREIGN KEY (TownId) REFERENCES " + prefix + "Towns(Id) ON DELETE CASCADE, FOREIGN KEY (Owner) REFERENCES " + prefix + "Residents(UUID) ON DELETE SET NULL);"));
		updates.add(new DBUpdate("03.22.2014.1", "Add ResidentsToTowns Table", "CREATE TABLE IF NOT EXISTS " + prefix + "ResidentsToTowns (Id SERIAL NOT NULL, TownId int NOT NULL, Owner varchar(255) NOT NULL, PRIMARY KEY (Id), FOREIGN KEY (TownId) REFERENCES " + prefix + "Towns(Id) ON DELETE CASCADE, FOREIGN KEY (Owner) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE);"));
		updates.add(new DBUpdate("03.22.2014.2", "Add TownsToNations", "CREATE TABLE IF NOT EXISTS " + prefix + "TownsToNations (Id SERIAL NOT NULL, TownId int NOT NULL, NationId int NOT NULL, Rank varchar(1) DEFAULT 'T', PRIMARY KEY (Id), FOREIGN KEY (TownId) REFERENCES " + prefix + "Towns(Id) ON DELETE CASCADE, FOREIGN KEY (NationId) REFERENCES " + prefix + "Nations(Id) ON DELETE CASCADE);"));
	}
}