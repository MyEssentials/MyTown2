package mytown.config;

import cpw.mods.fml.common.registry.GameRegistry;
import myessentials.utils.ClassUtils;
import myessentials.config.ConfigProperty;
import net.minecraft.init.Items;

@SuppressWarnings({"squid:S1444"}) // Suppresses SonarQube warnings to mark fields in Config as final.
public class Config {

    /* ----- General Config ----- */

    @ConfigProperty(category = "general", name = "Localization", comment = "Localization file without file extension.\\nLoaded from config/MyTown/localization/ first, then from the jar, then finally will fallback to en_US if needed.")
    public static String localization = "en_US";

    @ConfigProperty(category = "general", name = "SafeModeMessage", comment = "Message to display to users when MyTown is in safemode.")
    public static String safeModeMsg = "MyTown is in safe mode. Please tell a server admin!";

    /* ----- Datasource Config ----- */

    @ConfigProperty(category = "datasource", name = "Type", comment = "Datasource Type. Eg: MySQL, SQLite, etc.")
    public static String dbType = "SQLite";

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

    @ConfigProperty(category = "towns", name = "maxFarClaims", comment = "The maximum amount of chunks not attached to any other claimed chunk that are allowed per town. Set to 0 to disable far claims altogether.")
    public static int maxFarClaims = 0;

    @ConfigProperty(category = "residents", name = "teleportCooldown", comment = "The amount of time in server ticks for how long a player needs to wait between teleports.")
    public static int teleportCooldown = 200;

    @ConfigProperty(category = "cost", name = "costItem", comment = "The item which is used for paying for claims and making new towns. Use $ForgeEssentials if you want to use ForgeEssentials economy or $Vault if you want Vault economy.")
    public static String costItemName = GameRegistry.findUniqueIdentifierFor(Items.diamond).toString();

    @ConfigProperty(category = "cost", name = "costAmountMakeTown", comment = "The amount of the cost item you need to create a town. Making a town will cost this amount + amount to claim a chunk.")
    public static int costAmountMakeTown = 5;

    @ConfigProperty(category = "cost", name = "costAmountClaim", comment = "The amount of the cost item you need to claim a chunk.")
    public static int costAmountClaim = 3;

    @ConfigProperty(category = "cost", name = "costAmountClaimFar", comment = "The amount of the cost item you need to claim a chunk that is not adjacent to the town.")
    public static int costAmountClaimFar = 8;

    @ConfigProperty(category = "cost", name = "costAmountSpawn", comment = "The amount of the cost item you need to warp to the town's spawn point.")
    public static int costAmountSpawn = 0;

    @ConfigProperty(category = "cost", name = "costAmountOtherSpawn", comment = "The amount of the cost item you need to warp to the spawn point of a town that the player is not a resident of.")
    public static int costAmountOtherSpawn = 1;

    @ConfigProperty(category = "cost", name = "costAmountSetSpawn", comment = "The amount of the cost item you need to create a spawn point for the town.")
    public static int costAmountSetSpawn = 1;

    @ConfigProperty(category = "cost", name = "costTownUpkeep", comment = "The amount of the cost item towns have to pay everyday to maintain it.")
    public static int costTownUpkeep = 0;

    @ConfigProperty(category = "cost", name = "costAdditionalUpkeep", comment = "The amount of the cost item towns have to pay everyday per chunk owned to maintain it.")
    public static int costAdditionalUpkeep = 0;

    @ConfigProperty(category = "cost", name = "costAdditionClaim", comment = "The additional amount of the cost item people need to pay for each block already claimed [Ex: if you have 3 chunks in town claiming the next one will cost costAdditionClaim*3 + costAmountClaim]. This can be used with costMultiplicativeClaim.")
    public static int costAdditionClaim = 0;

    @ConfigProperty(category = "cost", name = "costMultiplicativeClaim", comment = "The multiplicative amount of the cost item people need to pay for each block already claimed [Ex: if you have 2 chunks the next one will cost costMultiplicativeClaim ^ 2 * costAmountClaim]. This can be used with costAdditionClaim.")
    public static float costMultiplicativeClaim = 1;

    @ConfigProperty(category = "cost", name = "defaultBankAmount", comment = "The amount of the cost item that the towns are gonna start with in their banks after created.")
    public static int defaultBankAmount = 5;

    @ConfigProperty(category = "towns", name = "defaultMaxPlotsPerPlayer", comment = "The maximum amount of plots a player can make in a town as a default.")
    public static int defaultMaxPlots = 1;

    @ConfigProperty(category = "towns", name = "upkeepTownDeletionDays", comment = "The amount of days a town can go on without paying upkeep.")
    public static int upkeepTownDeletionDays = 7;

    @ConfigProperty(category = "plots", name = "minPlotsArea", comment = "The minimum area required to create a plot. (X*Z)")
    public static int minPlotsArea = 9;

    @ConfigProperty(category = "plots", name = "minPlotsHeight", comment = "The minimum height required to create a plot. (Y)")
    public static int minPlotsHeight = 1;

    @ConfigProperty(category = "plots", name = "maxPlotsArea", comment = "The maximum area a plot can have. (X*Z)")
    public static int maxPlotsArea = 300;

    @ConfigProperty(category = "plots", name = "maxPlotsHeight", comment = "The maximum height a plot can have. (Y) [255 for unlimited height.]")
    public static int maxPlotsHeight = 256;

    @ConfigProperty(category = "plots", name = "enablePlots", comment = "Set this to false to disable all types of plot interaction.")
    public static boolean enablePlots = true;

    @ConfigProperty(category = "protection", name = "defaultProtectionSize", comment = "The range that it's going to check in if a protection's segment that has a tileentity does not provide getters for its area of influence.")
    public static int defaultProtectionSize = 32;

    @ConfigProperty(category = "protection", name = "useExtraEvents", comment = "If you have Forge 1254 or higher you can enable this feature. It provides more accurate protection.")
    public static boolean useExtraEvents = ClassUtils.isClassLoaded("net.minecraftforge.event.world.ExplosionEvent");

    //@ConfigProperty(category = "extra", name = "debug", comment = "Enables debugging output to console, use '/ta debug' to toggle ingame")
    //public static boolean debug;

    private Config() {
    }
}