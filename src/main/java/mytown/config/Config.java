package mytown.config;

import cpw.mods.fml.common.registry.GameRegistry;
import myessentials.config.ConfigProperty;
import myessentials.config.ConfigTemplate;
import myessentials.utils.ClassUtils;
import net.minecraft.init.Items;

@SuppressWarnings({"squid:S1444"}) // Suppresses SonarQube warnings to mark fields in Config as final.
public class Config extends ConfigTemplate {

    public static final Config instance = new Config();

    /* ----- General Config ----- */

    public ConfigProperty<String> localization = new ConfigProperty<String>(
            "localization", "general",
            "Localization file without file extension.\\nLoaded from config/MyTown/localization/ first, then from the jar, then finally will fallback to en_US if needed.",
            "en_US");
    public ConfigProperty<String> safeModeMsg = new ConfigProperty<String>(
            "safeModeMessage", "general",
            "Message to display to users when MyTown is in safemode.",
            "MyTown is in safe mode. Please tell a server admin!");

    /* ----- Datasource Config ----- */

    public ConfigProperty<String> dbType = new ConfigProperty<String>(
            "type", "datasource", "Datasource Type. Eg: MySQL, SQLite, etc.",
            "SQLite");

    /* ----- Others ----- */

    public ConfigProperty<Integer> distanceBetweenTowns = new ConfigProperty<Integer>(
            "distance", "towns",
            "Minimum distance (in chunks) between 2 towns. Checked when creating a town",
            5);
    public ConfigProperty<Integer> blocksMayor = new ConfigProperty<Integer>(
            "blocksMayor", "towns",
            "The amount of maximum blocks a town gets from the mayor.",
            5);
    public ConfigProperty<Integer> blocksResident = new ConfigProperty<Integer>(
            "blocksResidents", "towns",
            "The amount of maximum blocks a town gets from each player.",
            3);
    public ConfigProperty<Integer> maxTowns = new ConfigProperty<Integer>(
            "maxTowns", "residents",
            "The amount of towns a resident can be in.",
            3);
    public ConfigProperty<Integer> placeProtectionRange = new ConfigProperty<Integer>(
            "placeProtectionRange", "towns",
            "The distance in blocks from a protected town where you can't place a block in the Wild.",
            1);
    public ConfigProperty<Boolean> modifiableRanks = new ConfigProperty<Boolean>(
            "modifiableRanks", "towns",
            "If true residents with permission can modify the ranks of their towns. This feature hasn't been fully tested yet and it might cause problems!",
            false);
    public ConfigProperty<Integer> maxFarClaims = new ConfigProperty<Integer>(
            "maxFarClaims", "towns",
            "The maximum amount of chunks not attached to any other claimed chunk that are allowed per town. Set to 0 to disable far claims altogether.",
            0);
    public ConfigProperty<Integer> teleportCooldown = new ConfigProperty<Integer>(
            "teleportCooldown", "residents",
            "The amount of time in server ticks for how long a player needs to wait between teleports.",
            200);
    public ConfigProperty<String> costItemName = new ConfigProperty<String>(
            "costItem", "cost",
            "The item which is used for paying for claims and making new towns. Use $ForgeEssentials if you want to use ForgeEssentials economy or $Vault if you want Vault economy.",
            GameRegistry.findUniqueIdentifierFor(Items.diamond).toString());
    public ConfigProperty<Integer> costAmountMakeTown = new ConfigProperty<Integer>(
            "costAmountMakeTown", "cost",
            "The amount of the cost item you need to create a town. Making a town will cost this amount + amount to claim a chunk.",
            5);
    public ConfigProperty<Integer> costAmountClaim = new ConfigProperty<Integer>(
            "costAmountClaim", "cost",
            "The amount of the cost item you need to claim a chunk.",
            3);
    public ConfigProperty<Integer> costAmountClaimFar = new ConfigProperty<Integer>(
            "costAmountClaimFar", "cost",
            "The amount of the cost item you need to claim a chunk that is not adjacent to the town.",
            8);
    public ConfigProperty<Integer> costAmountSpawn = new ConfigProperty<Integer>(
            "costAmountSpawn", "cost",
            "The amount of the cost item you need to warp to the town's spawn point.",
            0);
    public ConfigProperty<Integer> costAmountOtherSpawn = new ConfigProperty<Integer>(
            "costAmountOtherSpawn", "cost",
            "The amount of the cost item you need to warp to the spawn point of a town that the player is not a resident of.",
            1);
    public ConfigProperty<Integer> costAmountSetSpawn = new ConfigProperty<Integer>(
            "costAmountSetSpawn", "cost",
            "The amount of the cost item you need to create a spawn point for the town.",
            1);
    public ConfigProperty<Integer> costTownUpkeep = new ConfigProperty<Integer>(
            "costTownUpkeep", "cost",
            "The amount of the cost item towns have to pay everyday to maintain it.",
            0);
    public ConfigProperty<Integer> costAdditionalUpkeep = new ConfigProperty<Integer>(
            "costAdditionalUpkeep", "cost", "The amount of the cost item towns have to pay everyday per chunk owned to maintain it.",
            0);
    public ConfigProperty<Integer> costAdditionClaim = new ConfigProperty<Integer>(
            "costAdditionClaim", "cost", "The additional amount of the cost item people need to pay for each block already claimed [Ex: if you have 3 chunks in town claiming the next one will cost costAdditionClaim*3 + costAmountClaim]. This can be used with costMultiplicativeClaim.",
            0);
    public ConfigProperty<Double> costMultiplicativeClaim = new ConfigProperty<Double>(
            "costMultiplicativeClaim", "cost",
            "The multiplicative amount of the cost item people need to pay for each block already claimed [Ex: if you have 2 chunks the next one will cost costMultiplicativeClaim ^ 2 * costAmountClaim]. This can be used with costAdditionClaim.",
            1.0D);
    public ConfigProperty<Integer> defaultBankAmount = new ConfigProperty<Integer>(
            "defaultBankAmount", "cost",
            "The amount of the cost item that the towns are gonna start with in their banks after created.",
            5);
    public ConfigProperty<Integer> defaultMaxPlots = new ConfigProperty<Integer>(
            "defaultMaxPlotsPerPlayer", "towns",
            "The maximum amount of plots a player can make in a town as a default.",
            1);
    public ConfigProperty<Integer> upkeepTownDeletionDays = new ConfigProperty<Integer>(
            "upkeepTownDeletionDays", "towns",
            "The amount of days a town can go on without paying upkeep.",
            7);
    public ConfigProperty<Integer> minPlotsArea = new ConfigProperty<Integer>(
            "minPlotsArea", "plots",
            "The minimum area required to create a plot. (X*Z)",
            9);
    public ConfigProperty<Integer> minPlotsHeight = new ConfigProperty<Integer>(
            "minPlotsHeight", "plots",
            "The minimum height required to create a plot. (Y)",
            1);
    public ConfigProperty<Integer> maxPlotsArea = new ConfigProperty<Integer>(
            "maxPlotsArea", "plots",
            "The maximum area a plot can have. (X*Z)",
            300);
    public ConfigProperty<Integer> maxPlotsHeight = new ConfigProperty<Integer>(
            "maxPlotsHeight", "plots",
            "The maximum height a plot can have. (Y) [255 for unlimited height.]",
            256);
    public ConfigProperty<Boolean> enablePlots = new ConfigProperty<Boolean>(
            "enablePlots", "plots",
            "Set this to false to disable all types of plot interaction.",
            true);
    public ConfigProperty<Integer> defaultProtectionSize = new ConfigProperty<Integer>(
            "defaultProtectionSize", "protection",
            "The range that it's going to check in if a protection's segment that has a tileentity does not provide getters for its area of influence.",
            32);
    public ConfigProperty<Boolean> fireSpreadInTowns = new ConfigProperty<Boolean>(
            "fireSpreadInTowns", "protection",
            "Allow fire to spread and burn up blocks in all towns and plots on the server.",
            false);
    public ConfigProperty<Boolean> taintSpreadInTowns = new ConfigProperty<Boolean>(
            "taintSpreadInTowns", "protection",
            "Allow Thaumcraft Taint biomes to spread in all towns and plots on the server.",
            false);
    public ConfigProperty<Boolean> mobTravelInTowns = new ConfigProperty<Boolean>(
            "mobTravelInTowns", "protection",
            "Allow mobs to travel into, but not spawn in a mob protected towns and plots on the server.",
            false);

    //@ConfigProperty(category = "extra", name = "debug", comment = "Enables debugging output to console, use '/ta debug' to toggle ingame")
    //public static boolean debug;
}