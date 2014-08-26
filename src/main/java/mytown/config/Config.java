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

    @ConfigProperty(category = "formatter", name = "BlockInfoFormat", comment = "The format to display a Block's info.")
    public static String blockInfoFormat = " ---------- Block ----------\\nTown: %1$s\\nDimension: %2$s\\nLocation: %3$s";

    @ConfigProperty(category = "formatter", name = "NationInfoFormat", comment = "The format to display a Nation's info.")
    public static String nationInfoFormat = " ---------- %1$s  ----------\\nCapital: TODO";

    @ConfigProperty(category = "formatter", name = "PlotInfoFormat", comment = "The format to display a Plots's info.")
    public static String plotInfoFormat = " ---------- %1$s  ----------\\nTown: %2$s\\nDimension: %3$s\\nStart: %4$s\\nEnd: %5$s";

    @ConfigProperty(category = "formatter", name = "RankInfoFormat", comment = "The format to display a Rank's info.")
    public static String rankInfoFormat = " ---------- %1$s  ----------\\nPermissions: %2$s";

    @ConfigProperty(category = "formatter", name = "ResidentInfoFormat", comment = "The format to display a Resident's info.")
    public static String residentInfoFormat = " ---------- %1$s  ----------";

    @ConfigProperty(category = "formatter", name = "TownInfoFormat", comment = "The format to display a Town's info.")
    public static String townInfoFormat = " ---------- %1$s (R: %2$s | B: %3$s | P: %4$s) ----------\\nResidents: %5$s\\nRanks %6$s";
}