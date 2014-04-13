package mytown.core;

import mytown.core.utils.config.ConfigProperty;

public class Config {
	@ConfigProperty(category = "general", comment = "Allows toggling maintenance mode")
	public static boolean maintenanceMode = false;
	
	@ConfigProperty(category = "general", comment = "Custom message to display when in maintenance mode")
	public static String  maintenanceModeMessage = "Server is in maintenance mode currently. Please come back later.";
}