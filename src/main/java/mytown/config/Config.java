package mytown.config;

import cpw.mods.fml.common.registry.GameRegistry;
import mytown.core.utils.config.ConfigProperty;
import net.minecraft.init.Items;

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

    @ConfigProperty(category = "towns", name = "blocksMayor", comment = "The amount of maximum blocks a town gets from the mayor.")
    public static int blocksMayor = 5;

    @ConfigProperty(category = "towns", name = "blocksResidents", comment = "The amount of maximum blocks a town gets from each player.")
    public static int blocksResident = 3;

    @ConfigProperty(category = "residents", name = "maxTowns", comment = "The amount of towns a resident can be in.")
    public static int maxTowns = 3;

    @ConfigProperty(category = "towns", name = "placeProtectionRange", comment = "The amount of blocks from the town in which the place protection is applied.")
    public static int placeProtectionRange = 1;

    @ConfigProperty(category = "towns", name = "modifiableRanks", comment = "If true residents with permission can modify the ranks of their towns. This feature hasn't been fully tested yet and it might cause problems!")
    public static boolean modifiableRanks = false;

    @ConfigProperty(category = "towns", name = "costItem", comment = "The item which is used for paying for claims and making new towns.")
    public static String costItemName = GameRegistry.findUniqueIdentifierFor(Items.diamond).name;

    @ConfigProperty(category = "towns", name = "costAmountMakeTown", comment = "The amount of the cost item you need to create a town.")
    public static int costAmountMakeTown = 5;

    @ConfigProperty(category = "towns", name = "costAmountClaim", comment = "The amount of the cost item you need to create a town.")
    public static int costAmountClaim = 3;

    @ConfigProperty(category = "towns", name = "defaultMaxPlotsPerPlayer", comment = "The maximum amount of plots a player can make in a town as a default.")
    public static int defaultMaxPlots = 1;

    @ConfigProperty(category = "plots", name = "minPlotsArea", comment = "The minimum area required to create a plot (X*Z)")
    public static int minPlotsArea = 9;

    @ConfigProperty(category = "plots", name = "minPlotsHeight", comment = "The minimum height required to create a plot(Y)")
    public static int minPlotsHeight = 1;
}