package mytown;

import mytown.core.utils.config.ConfigProperty;

// TODO More config!
public class Config {
	//////////////////////////////
	// General Config
	//////////////////////////////
	
	@ConfigProperty(category = "general", name = "Localization", comment = "Localization file without file extension. Loaded from config/MyTown/localization/")
	public static String localization = "en_US";
	
	@ConfigProperty(category = "general", name = "SafeModeMessage", comment = "Message to display to users when MyTown is in safemode")
	public static String safeModeMsg = "MyTown is in safe mode. Please tell a server admin!";
	
	//////////////////////////////
	// Datasource Config
	//////////////////////////////
	
	@ConfigProperty(category = "datasource", name = "Type", comment = "Datasource Type. Eg: MySQL, SQLite, etc")
	public static String dbType = "SQLite";
}