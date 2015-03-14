package mytown;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.GameRegistry;
import mytown.commands.*;
import mytown.config.Config;
import mytown.config.FlagsConfig;
import mytown.config.RanksConfig;
import mytown.config.WildPermsConfig;
import mytown.core.Localization;
import mytown.core.utils.Log;
import mytown.core.utils.command.CommandManager;
import mytown.core.utils.config.ConfigProcessor;
import mytown.crash.DatasourceCrashCallable;
import mytown.handlers.Ticker;
import mytown.handlers.SafemodeHandler;
import mytown.handlers.VisualsTickHandler;
import mytown.new_protection.Protections;
import mytown.new_protection.eventhandlers.ExtraForgeHandlers;
import mytown.new_protection.json.JSONParser;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.ModProxies;
import mytown.util.Constants;
import mytown.util.MyTownUtils;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.lang.reflect.Method;

// TODO Add a way to safely reload
/**/
@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES, acceptableRemoteVersions = "*")
public class MyTown {
    @Instance
    public static MyTown instance;
    public Log log;
    // ---- Configuration files ----
    public Configuration config;
    public RanksConfig ranksConfig;
    public WildPermsConfig wildConfig;
    public FlagsConfig flagsConfig;

    @EventHandler
    public void preInit(FMLPreInitializationEvent ev) {
        // Setup Loggers
        log = new Log(ev.getModLog());

        Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory().getPath() + "/MyTown/";

        // Read Configs
        config = new Configuration(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));
        ConfigProcessor.load(config, Config.class);

        LocalizationProxy.load();

        JSONParser.folderPath = ev.getModConfigurationDirectory() + "/MyTown/protections";
        registerHandlers();

        // Add all the ModProxies
        ModProxies.addProxies();

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
        registerCommands();
        Commands.populateCompletionMap();
        // This needs to be after registerCommands... might want to move both methods...
        ranksConfig = new RanksConfig(new File(Constants.CONFIG_FOLDER, "DefaultRanks.json"));
        wildConfig = new WildPermsConfig(new File(Constants.CONFIG_FOLDER, "WildPerms.json"));
        flagsConfig = new FlagsConfig(new File(Constants.CONFIG_FOLDER, "DefaultFlags.json"));
        JSONParser.start();
        registerPermissionHandler();
        DatasourceProxy.setLog(log);
        SafemodeHandler.setSafemode(!DatasourceProxy.start(config));
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent ev) {
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
            log.info("Failed to get first permission breach method.");
            e.printStackTrace();
        }

        CommandManager.registerCommands(CommandsEveryone.class, m);
        CommandManager.registerCommands(CommandsAssistant.class, m);
        if (Config.modifiableRanks)
            CommandManager.registerCommands(CommandsAssistant.ModifyRanks.class, m);
        if(Config.enablePlots)
            CommandManager.registerCommands(CommandsEveryone.Plots.class, m);
        CommandManager.registerCommands(CommandsAdmin.class);
        CommandManager.registerCommands(CommandsOutsider.class, m);

        /*
        if(isCauldron) {
            BukkitCompat.getInstance();
        }
        */
    }

    private void registerPermissionHandler() {
        /*
        try {
            Class<?> c = Class.forName("forgeperms.ForgePerms");
            Method m = c.getMethod("getPermissionManager");
            ForgePermsAPI.permManager = (IPermissionManager)m.invoke(null);
        } catch (Exception e) {
            MyTown.instance.log.error("Failed to load ForgePerms. Currently not using ANY protection for commands usage!");
            e.printStackTrace();

        }
        */
    }

    /**
     * Registers all handlers (Event handlers)
     */
    private void registerHandlers() {

        FMLCommonHandler.instance().bus().register(new SafemodeHandler());
        Ticker playerTracker = new Ticker();

        FMLCommonHandler.instance().bus().register(playerTracker);
        MinecraftForge.EVENT_BUS.register(playerTracker);

        FMLCommonHandler.instance().bus().register(VisualsTickHandler.getInstance());

        FMLCommonHandler.instance().bus().register(Protections.getInstance());
        MinecraftForge.EVENT_BUS.register(Protections.getInstance());

        if(Config.useExtraEvents)
            MinecraftForge.EVENT_BUS.register(ExtraForgeHandlers.getInstance());
    }

    /**
     * Checks the config to see if there are any wrong values.
     * Throws an exception if there is a problem.
     */
    public void checkConfig() {
        // Checking cost item
        if(Config.costItemName.equals("$")) {
            if (!Loader.isModLoaded("ForgeEssentials")) {
                throw new RuntimeException("Failed to find ForgeEssentials for economy implementation. Reverting to default.");
            }
        } else {
            String[] split = Config.costItemName.split(":");
            if (split.length < 2 || split.length > 3) {
                throw new RuntimeException("Field costItem has an invalid value. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
            }

            if (GameRegistry.findItem(split[0], split[1]) == null) {
                throw new RuntimeException("Field costItem has an invalid modid or unique name of the item. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
            }

            if (split.length > 2 && (!MyTownUtils.tryParseInt(split[2]) || Integer.parseInt(split[2]) < 0)) {
                throw new RuntimeException("Field costItem has an invalid metadata. Template: (modid):(unique_name)[:meta]. Use \"minecraft\" as modid for vanilla items/blocks.");
            }
        }

        if(Config.useExtraEvents && !checkExtraEvents()) {
            throw new RuntimeException("Extra events are enabled but you don't have the minimal forge version needed to load them.");
        }
    }

    /**
     * Returns whether or not ALL extra events are available.
     */
    public boolean checkExtraEvents() {
        return MyTownUtils.isClassLoaded("net.minecraftforge.event.world.ExplosionEvent");
    }

    // ////////////////////////////
    // Helpers
    // ////////////////////////////

    public static Localization getLocal() {
        return LocalizationProxy.getLocalization();
    }
}