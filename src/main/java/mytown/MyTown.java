package mytown;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import myessentials.Localization;
import myessentials.json.JsonConfig;
import myessentials.utils.ClassUtils;
import myessentials.utils.StringUtils;
import mypermissions.api.command.CommandManager;
import mytown.commands.*;
import mytown.config.Config;
import mytown.config.json.FlagsConfig;
import mytown.config.json.RanksConfig;
import mytown.config.json.WildPermsConfig;
import mytown.crash.DatasourceCrashCallable;
import mytown.entities.signs.SellSign;
import mytown.handlers.SafemodeHandler;
import mytown.handlers.Ticker;
import mytown.handlers.VisualsHandler;
import mytown.new_datasource.MyTownDatasource;
import mytown.protection.ProtectionHandlers;
import mytown.protection.ProtectionManager;
import mytown.protection.eventhandlers.ExtraEventsHandler;
import mytown.protection.json.ProtectionParser;
import mytown.proxies.EconomyProxy;
import mytown.util.Constants;
import mytown.util.exceptions.ConfigException;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

// TODO Add a way to safely reload
/**/
@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES, acceptableRemoteVersions = "*")
public class MyTown {
    @Instance
    public static MyTown instance;
    public Logger LOG;
    public Localization LOCAL;
    public MyTownDatasource datasource;
    // ---- Configuration files ----

    private final List<JsonConfig> jsonConfigs =  new ArrayList<JsonConfig>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent ev) {
        // Setup Loggers
        LOG = ev.getModLog();

        Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory().getPath() + "/MyTown/";

        // Read Configs
        Config.instance.init(Constants.CONFIG_FOLDER + "/MyTown.cfg", Constants.MODID);
        LOCAL = new Localization(Constants.CONFIG_FOLDER, Config.instance.localization.get(), "/mytown/localization/", MyTown.class);

        registerHandlers();

        // Register ICrashCallable's
        FMLCommonHandler.instance().registerCrashCallable(new DatasourceCrashCallable());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent ev) {
        SellSign.SellSignType.instance.register();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent ev) {
        EconomyProxy.init();
        checkConfig();
        registerCommands();
        Commands.populateCompletionMap();

        jsonConfigs.add(new WildPermsConfig(Constants.CONFIG_FOLDER + "/WildPerms.json"));
        jsonConfigs.add(new FlagsConfig(Constants.CONFIG_FOLDER + "/DefaultFlags.json"));
        jsonConfigs.add(new RanksConfig(Constants.CONFIG_FOLDER + "/DefaultTownRanks.json"));
        for (JsonConfig jsonConfig : jsonConfigs) {
            jsonConfig.init();
        }

        ProtectionParser.start();
        //SafemodeHandler.setSafemode(!DatasourceProxy.start(config));
        datasource = new MyTownDatasource();
        LOG.info("Started");
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent ev) {
        datasource.deleteAllBlockOwners();
        ProtectionManager.saveBlockOwnersToDB();
        datasource.stop();
    }

    /**
     * Registers all commands
     */
    private void registerCommands() {
        CommandManager.registerCommands(CommandsEveryone.class, null, LOCAL, new RankPermissionManager());
        CommandManager.registerCommands(CommandsAssistant.class, "mytown.cmd", LOCAL, null);
        if (Config.instance.modifiableRanks.get())
            CommandManager.registerCommands(CommandsAssistant.ModifyRanks.class, "mytown.cmd", LOCAL, null);
        CommandManager.registerCommands(CommandsAdmin.class, null, LOCAL, null);
        if(Config.instance.enablePlots.get()) {
            CommandManager.registerCommands(CommandsEveryone.Plots.class, "mytown.cmd", LOCAL, null);
            CommandManager.registerCommands(CommandsAssistant.Plots.class, "mytown.cmd", LOCAL, null);
            CommandManager.registerCommands(CommandsAdmin.Plots.class, "mytown.adm.cmd", LOCAL, null);
        }

        CommandManager.registerCommands(CommandsOutsider.class, "mytown.cmd", LOCAL, null);
    }

    public WildPermsConfig getWildConfig() {
        for(JsonConfig jsonConfig : jsonConfigs) {
            if(jsonConfig instanceof WildPermsConfig)
                return (WildPermsConfig)jsonConfig;
        }
        return null;
    }

    public RanksConfig getRanksConfig() {
        for(JsonConfig jsonConfig : jsonConfigs) {
            if(jsonConfig instanceof RanksConfig)
                return (RanksConfig)jsonConfig;
        }
        return null;
    }

    public FlagsConfig getFlagsConfig() {
        for(JsonConfig jsonConfig : jsonConfigs) {
            if(jsonConfig instanceof FlagsConfig)
                return (FlagsConfig)jsonConfig;
        }
        return null;
    }

    /**
     * Registers all handlers (Event handlers)
     */
    private void registerHandlers() {

        FMLCommonHandler.instance().bus().register(new SafemodeHandler());
        Ticker playerTracker = new Ticker();

        FMLCommonHandler.instance().bus().register(playerTracker);
        MinecraftForge.EVENT_BUS.register(playerTracker);

        FMLCommonHandler.instance().bus().register(VisualsHandler.instance);

        FMLCommonHandler.instance().bus().register(ProtectionHandlers.instance);
        MinecraftForge.EVENT_BUS.register(ProtectionHandlers.instance);

        if(Config.instance.useExtraEvents.get())
            MinecraftForge.EVENT_BUS.register(ExtraEventsHandler.getInstance());
    }

    public void loadConfigs() {
        Config.instance.reload();

        EconomyProxy.init();
        checkConfig();

        for (JsonConfig jsonConfig : jsonConfigs) {
            jsonConfig.init();
        }

        ProtectionParser.start();
    }

    /**
     * Checks the config to see if there are any wrong values.
     * Throws an exception if there is a problem.
     */
    private void checkConfig() {
        // Checking cost item
        if(EconomyProxy.isItemEconomy()) {
            String[] split = Config.instance.costItemName.get().split(":");
            if (split.length < 2 || split.length > 3) {
                throw new ConfigException("Field costItem has an invalid value. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
            }

            if (GameRegistry.findItem(split[0], split[1]) == null) {
                throw new ConfigException("Field costItem has an invalid modid or unique name of the item. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
            }

            if (split.length > 2 && (!StringUtils.tryParseInt(split[2]) || Integer.parseInt(split[2]) < 0)) {
                throw new ConfigException("Field costItem has an invalid metadata. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
            }
        }

        if(Config.instance.useExtraEvents.get() && !checkExtraEvents()) {
            throw new ConfigException("Extra events are enabled but you don't have the minimal forge version needed to load them.");
        }
    }

    /**
     * Returns whether or not ALL extra events are available.
     */
    private boolean checkExtraEvents() {
        return ClassUtils.isClassLoaded("net.minecraftforge.event.world.ExplosionEvent");
    }
}