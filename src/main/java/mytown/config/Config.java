package mytown.config;

import mytown.core.utils.config.ConfigProperty;

// TODO More config!
public class Config {
    /* ----- General Config ----- */

    @ConfigProperty(category = "general", name = "Localization", comment = "Localization file without file extension.\\nLoaded from config/MyTown/localization/ first, then from the jar, then finally will fallback to en_US if needed.")
    public static String localization = "en_US";

    @ConfigProperty(category = "general", name = "SafeModeMessage", comment = "Message to display to users when MyTown is in safemode.")
    public static String safeModeMsg = "MyTown is in safe mode. Please tell a server admin!";

	/* ----- Datasource Config ----- */

    @ConfigProperty(category = "datasource", name = "Type", comment = "Datasource Type. Eg: MySQL, SQLite, etc.")
    public static String dbType = "SQLite";

    /* ----- Formatter Config ----- */
    // TODO Finish default formats (Colors, more fields, etc)

    /* ----- Others ----- */
    @ConfigProperty(category = "towns", name = "distance", comment = "Minimum distance (in chunks) between 2 towns. Checked when creating a town")
    public static int distanceBetweenTowns = 5;


}