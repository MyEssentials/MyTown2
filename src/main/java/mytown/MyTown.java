package mytown;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.GameRegistry;
import myessentials.Localization;
import mypermissions.api.command.CommandManager;
import myessentials.config.ConfigProcessor;
import myessentials.json.JSONConfig;
import myessentials.utils.ClassUtils;
import myessentials.utils.StringUtils;
import mytown.commands.*;
import mytown.config.Config;
import mytown.config.json.FlagsConfig;
import mytown.config.json.WildPermsConfig;
import mytown.crash.DatasourceCrashCallable;
import mytown.entities.Rank;
import mytown.handlers.SafemodeHandler;
import mytown.handlers.Ticker;
import mytown.handlers.VisualsHandler;
import mytown.protection.ProtectionUtils;
import mytown.protection.Protections;
import mytown.protection.eventhandlers.ExtraEventsHandler;
import mytown.protection.json.ProtectionParser;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.EconomyProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import mytown.util.exceptions.ConfigException;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// TODO Add a way to safely reload
/**/
@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES, acceptableRemoteVersions = "*")
public class MyTown {
    @Instance
    public static MyTown instance;
    public Logger LOG;
    // ---- Configuration files ----
    private Configuration config;

    private final List<JSONConfig> jsonConfigs =  new ArrayList<JSONConfig>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent ev) {
        // Setup Loggers
        LOG = ev.getModLog();

        Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory().getPath() + "/MyTown/";

        // Read Configs
        config = new Configuration(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));
        ConfigProcessor.load(config, Config.class);

        ProtectionParser.setFolderPath(ev.getModConfigurationDirectory() + "/MyTown/protections");

        registerHandlers();

        // Register ICrashCallable's
        FMLCommonHandler.instance().registerCrashCallable(new DatasourceCrashCallable());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent ev) {
        config.save();
    }

    @EventHandler
    public void imcEvent(FMLInterModComms.IMCEvent ev) {
        for (FMLInterModComms.IMCMessage msg : ev.getMessages()) {
            String[] keyParts = msg.key.split("|");

            if (keyParts[0].equals("datasource")) {
                DatasourceProxy.imc(msg);
            }
        }
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent ev) {
        checkConfig();
        EconomyProxy.init();
        registerCommands();
        Rank.initDefaultRanks();
        Commands.populateCompletionMap();

        jsonConfigs.add(new WildPermsConfig(Constants.CONFIG_FOLDER + "/WildPerms.json"));
        jsonConfigs.add(new FlagsConfig(Constants.CONFIG_FOLDER + "/DefaultFlags.json"));
        for (JSONConfig jsonConfig : jsonConfigs) {
            jsonConfig.init();
        }

        ProtectionParser.start();
        SafemodeHandler.setSafemode(!DatasourceProxy.start(config));
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent ev) {
        DatasourceProxy.getDatasource().deleteAllBlockOwners();
        ProtectionUtils.saveBlockOwnersToDB();
        DatasourceProxy.stop();
    }

    /**
     * Registers all commands
     */
    private void registerCommands() {
        Method m = null;
        try {
            m = Commands.class.getMethod("firstPermissionBreach", String.class, ICommandSender.class);
        } catch (Exception e) {
            LOG.info("Failed to get first permission breach method.");
            LOG.error(ExceptionUtils.getStackTrace(e));
        }

        CommandManager.registerCommands(CommandsEveryone.class, null, getLocal(), new RankPermissionManager());
        CommandManager.registerCommands(CommandsAssistant.class, "mytown.cmd", getLocal(), null);
        if (Config.modifiableRanks)
            CommandManager.registerCommands(CommandsAssistant.ModifyRanks.class, "mytown.cmd", getLocal(), null);
        CommandManager.registerCommands(CommandsAdmin.class, null, getLocal(), null);
        if(Config.enablePlots) {
            CommandManager.registerCommands(CommandsEveryone.Plots.class, "mytown.cmd", getLocal(), null);
            CommandManager.registerCommands(CommandsAssistant.Plots.class, "mytown.cmd", getLocal(), null);
            CommandManager.registerCommands(CommandsAdmin.Plots.class, "mytown.adm.cmd", getLocal(), null);
        }

        CommandManager.registerCommands(CommandsOutsider.class, "mytown.cmd", getLocal(), null);
    }

    public WildPermsConfig getWildConfig() {
        for(JSONConfig jsonConfig : jsonConfigs) {
            if(jsonConfig instanceof WildPermsConfig)
                return (WildPermsConfig)jsonConfig;
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

        FMLCommonHandler.instance().bus().register(Protections.instance);
        MinecraftForge.EVENT_BUS.register(Protections.instance);

        if(Config.useExtraEvents)
            MinecraftForge.EVENT_BUS.register(ExtraEventsHandler.getInstance());
    }

    public void loadConfigs() {
        config = new Configuration(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));
        ConfigProcessor.load(config, Config.class);

        checkConfig();
        EconomyProxy.init();

        for (JSONConfig jsonConfig : jsonConfigs) {
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
            String[] split = Config.costItemName.split(":");
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

        if(Config.useExtraEvents && !checkExtraEvents()) {
            throw new ConfigException("Extra events are enabled but you don't have the minimal forge version needed to load them.");
        }
    }

    /**
     * Returns whether or not ALL extra events are available.
     */
    private boolean checkExtraEvents() {
        return ClassUtils.isClassLoaded("net.minecraftforge.event.world.ExplosionEvent");
    }

    // ////////////////////////////
    // Helpers
    // ////////////////////////////

    public static Localization getLocal() {
        return LocalizationProxy.getLocalization();
    }
}