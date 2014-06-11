package mytown.chat;

import mytown.core.utils.config.ConfigProperty;

public class Config {
	@ConfigProperty(category = "Chat")
	public static String[] channels = {
		"Global;G;[$chAbbreviation$] $username$: $msg;0;Global" // name, abbreviation, format, radius, type
	}; // TODO Add default channels
	
	@ConfigProperty(category = "Chat")
	public static String defaultChannel = "Global";
}