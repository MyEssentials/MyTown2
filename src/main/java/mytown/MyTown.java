package mytown;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;
import forgeperms.api.ForgePermsAPI;
import mytown.commands.CommandsAdmin;
import mytown.commands.CommandsAssistant;
import mytown.commands.CommandsEveryone;
import mytown.core.utils.command.CommandManager;
import mytown.entities.flag.Flag;
import mytown.handlers.VisualsTickHandler;
import mytown.x_commands.admin.CmdTownAdmin;
import mytown.x_commands.town.CmdTown;
import mytown.x_commands.town.info.CmdListTown;
import mytown.config.Config;
import mytown.config.RanksConfig;
import mytown.core.Localization;
import mytown.core.utils.Log;
import mytown.core.utils.x_command.CommandUtils;
import mytown.core.utils.config.ConfigProcessor;
import mytown.crash.DatasourceCrashCallable;
import mytown.handlers.PlayerTracker;
import mytown.handlers.SafemodeHandler;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.ModProxies;
import mytown.util.Constants;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

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

            if (keyParts[0] == "datasource") {
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
        ForgePermsAPI.permManager = new PermissionManager(); // temporary for testing, returns true all the time
        // addDefaultPermissions();
        DatasourceProxy.setLog(log);
        SafemodeHandler.setSafemode(DatasourceProxy.start(config));

        CmdListTown.updateTownSortCache(); // Update cache after everything is loaded
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

        CommandManager.registerCommands(CommandsEveryone.class);
        CommandManager.registerCommands(CommandsAssistant.class);
        CommandManager.registerCommands(CommandsAdmin.class);


        /*
        // No longer registering old commands, still keeping them around tho

        CommandUtils.registerCommand(new CmdTown());
        CommandUtils.registerCommand(new CmdTownAdmin());
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
        // TODO Re-add the below two handlers after they are redone
        FMLCommonHandler.instance().bus().register(VisualsTickHandler.instance);
        //MinecraftForge.EVENT_BUS.register(new MyTownEventHandler());
    }

    // ////////////////////////////
    // Helpers
    // ////////////////////////////

    public static Localization getLocal() {
        return LocalizationProxy.getLocalization();
    }
}