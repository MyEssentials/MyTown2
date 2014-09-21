package mytown;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;
import forgeperms.api.ForgePermsAPI;
import mytown.commands.*;
import mytown.config.Config;
import mytown.config.RanksConfig;
import mytown.core.Localization;
import mytown.core.utils.DependencyManager;
import mytown.core.utils.Log;
import mytown.core.utils.command.CommandManager;
import mytown.core.utils.config.ConfigProcessor;
import mytown.crash.DatasourceCrashCallable;
import mytown.entities.flag.Flag;
import mytown.handlers.PlayerTracker;
import mytown.handlers.SafemodeHandler;
import mytown.handlers.VisualsTickHandler;
import mytown.protection.Protections;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.ModProxies;
import mytown.util.Constants;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.lang.reflect.Method;

// TODO Add a way to safely reload
// TODO Make sure ALL DB drivers are included when built. Either as a separate mod, or packaged with this. Maybe even make MyTown just DL them at runtime and inject them
/**/
@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES, acceptableRemoteVersions = "*")
public class MyTown {
    @Instance
    public static MyTown instance;
    public Log log;
    public Configuration config;
    public RanksConfig ranksConfig;

    @EventHandler
    public void preInit(FMLPreInitializationEvent ev) {

        // Setup Loggers
        log = new Log(ev.getModLog());

        Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory().getPath() + "/MyTown/";

        downloadDependencies(ev);

        // Read Configs
        config = new Configuration(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));


        ConfigProcessor.load(config, Config.class);
        LocalizationProxy.load();
        registerHandlers();

        // Add all the ModProxys
        ModProxies.addProxies();

        // Register ICrashCallable's
        FMLCommonHandler.instance().registerCrashCallable(new DatasourceCrashCallable());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent ev) {
        ModProxies.load();
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
        Flag.initFlags();
        registerCommands();
        // This needs to be after registerCommands... might want to move both methods...
        ranksConfig = new RanksConfig(new File(Constants.CONFIG_FOLDER, "DefaultRanks.json"));
        registerPermissionHandler();
        // addDefaultPermissions();
        DatasourceProxy.setLog(log);
        SafemodeHandler.setSafemode(!DatasourceProxy.start(config));

        Commands.populateCompletionMap();
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent ev) {
        config.save();
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
        CommandManager.registerCommands(CommandsAdmin.class);
        CommandManager.registerCommands(CommandsOutsider.class, m);


        /*
        // No longer registering old commands, still keeping them around tho

        CommandUtils.registerCommand(new CmdTown());
        CommandUtils.registerCommand(new CmdTownAdmin());
        */
    }

    private void registerPermissionHandler() {
        ForgePermsAPI.permManager = new PermissionManager();
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
     * Registers all handlers (Event, etc)
     */
    private void registerHandlers() {
        FMLCommonHandler.instance().bus().register(new SafemodeHandler());
        PlayerTracker playerTracker = new PlayerTracker();

        FMLCommonHandler.instance().bus().register(playerTracker);
        MinecraftForge.EVENT_BUS.register(playerTracker);

        FMLCommonHandler.instance().bus().register(VisualsTickHandler.instance);
        MinecraftForge.EVENT_BUS.register(Protections.instance);
        FMLCommonHandler.instance().bus().register(Protections.instance);
        //MinecraftForge.EVENT_BUS.register(new MyTownEventHandler());
    }

    public void downloadDependencies(FMLPreInitializationEvent ev) {
        String libsFolder = Constants.CONFIG_FOLDER + "libs/";

        DependencyManager.Dep[] deps = {
                new DependencyManager.Dep(libsFolder + "/sqlite-jdbc-3.7.2.jar", "https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.7.2.jar"),
                new DependencyManager.Dep(libsFolder + "/reflectasm-1.09.jar", "http://central.maven.org/maven2/com/esotericsoftware/reflectasm/reflectasm/1.09/reflectasm-1.09.jar")
        };

        DependencyManager.downloadDependencies(deps);
    }

    // ////////////////////////////
    // Helpers
    // ////////////////////////////

    public static Localization getLocal() {
        return LocalizationProxy.getLocalization();
    }
}