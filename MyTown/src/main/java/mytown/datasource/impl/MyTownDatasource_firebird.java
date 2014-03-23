package mytown.datasource.impl;

import java.sql.PreparedStatement;

import mytown.datasource.types.MyTownDatasource_SQL;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

// TODO Finish!
/**
 * Extremely Experimental and non-finished Firebird
 * @author Joe Goett
 */
public class MyTownDatasource_firebird extends MyTownDatasource_SQL {
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
		log.warning("Firebird support not finished!");
		return false;
		
//		if (conn != null)
//			return true;
//		
//		Class.forName("org.firebirdsql.jdbc.FBDriver");
//
//		Properties properties = new Properties();
//		properties.put("user", username);
//		properties.put("password", password);
//		
//		conn = DriverManager.getConnection("jdbc:firebirdsql:" + getDatabasePath(), properties);
//		if (conn == null) {
//			return false; // TODO Log error?
//		}
//		
//		createGenerators();
//		
//		return true;
	}
	
	private void createGenerators() throws Exception {
		PreparedStatement statement;
		statement = prepare("CREATE GENERATOR gen_towns_id; SET GENERATOR gen_towns_id TO 0;"
		+ "CREATE GENERATOR gen_nations_id; SET GENERATOR gen_nations_id TO 0;"
		+ "CREATE GENERATOR gen_townblocks_id; SET GENERATOR gen_townblocks_id TO 0;"
		+ "CREATE GENERATOR gen_townplots_id; SET GENERATOR gen_townplots_id TO 0;"
		+ "CREATE GENERATOR gen_restotown_id; SET GENERATOR gen_restotown_id TO 0;"
		+ "CREATE GENERATOR gen_towntonation_id; SET GENERATOR gen_towntonation_id TO 0;");
	}

	@Override
	protected void doConfig(Configuration config) {
		super.doConfig(config);

		Property prop;

		prop = config.get("database", "Username", "");
		prop.comment = "Username to use when connecting";
		username = prop.getString();

		prop = config.get("database", "Password", "");
		prop.comment = "Password to use when connecting";
		password = prop.getString();

		prop = config.get("database", "Host", "localhost");
		prop.comment = "Hostname:Port of the db server";
		host = prop.getString();

		prop = config.get("database", "Database", "mytown");
		prop.comment = "The database name";
		database = prop.getString();
		
		autoIncrement = ""; // Disable autoIncrement. Will use generators and triggers
	}
}