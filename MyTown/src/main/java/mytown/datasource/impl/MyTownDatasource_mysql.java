package mytown.datasource.impl;

import java.sql.DriverManager;
import java.util.Properties;

import mytown.datasource.types.MyTownDatasource_SQL;
import net.minecraftforge.common.Configuration;

//TODO Test
//TODO Add comments
//TODO Profit?

/**
 * MySQL Datasource connector
 * 
 * @author Joe Goett
 */
public class MyTownDatasource_mysql extends MyTownDatasource_SQL {
	// Config
	private String username;
	private String password;
	private String host;
	private String database;

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
		if (conn != null)
			return true;

		Class.forName("com.mysql.jdbc.Driver");

		Properties properties = new Properties();
		properties.put("autoReconnect", "true");
		properties.put("user", username);
		properties.put("password", password);

		conn = DriverManager.getConnection("jdbc:mysql:" + getDatabasePath(), properties);
		if (conn == null)
			return false; // TODO Log error?

		// Update DB
		setUpdates();
		doUpdates();

		return true;
	}

	@Override
	protected void doConfig(Configuration config) {
		super.doConfig(config);
		username = config.get(configCat, "Username", "", "Username to use when connecting").getString();
		password = config.get(configCat, "Password", "", "Password to use when connecting").getString();
		host = config.get(configCat, "Host", "localhost", "Hostname:Port of the db server").getString();
		database = config.get(configCat, "Database", "mytown", "The database name").getString();
	}
}