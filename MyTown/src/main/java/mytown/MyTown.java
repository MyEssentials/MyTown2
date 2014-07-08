package mytown;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.Loader;
import ic2.api.info.Info;
import mytown.api.datasource.MyTownDatasource;
import mytown.commands.admin.CmdTownAdmin;
import mytown.commands.town.CmdTown;
import mytown.commands.town.info.CmdListTown;
import mytown.config.Config;
import mytown.config.RanksConfig;
import mytown.core.Localization;
import mytown.core.utils.Log;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.config.ConfigProcessor;
import mytown.crash.DatasourceCrashCallable;
import mytown.handler.IC2EventHandler;
import mytown.handler.MyTownEventHandler;
import mytown.handler.VanillaEventHandler;
import mytown.interfaces.IModule;
import mytown.modules.IC2Module;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.ModProxies;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import forgeperms.api.ForgePermsAPI;

// TODO Add a way to safely reload
// TODO Make sure ALL DB drivers are included when built. Either as a separate mod, or packaged with this. Maybe even make MyTown just DL them at runtime and inject them

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES)
public class MyTown {
	@Mod.Instance(Constants.MODID)
	public static MyTown instance;
	public Log log;
	public Configuration config;
    public RanksConfig rankConfig; // atm very useless TODO: add reload functions to it
    public List<IModule> enabledModules;

    // Set to true to kick all non-admin users out with a custom kick message
	public boolean safemode = false;


	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		// Setup Loggers
		log = new Log(ev.getModLog());

		Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory().getPath() + "/MyTown/";

		// Read Configs
		config = new Configuration(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));
		ConfigProcessor.load(config, Config.class);
		LocalizationProxy.load();

		registerHandlers();


		// ModProxy PreInit
		ModProxies.addProxies();
		ModProxies.preInit();

		// Register ICrashCallable's
		FMLCommonHandler.instance().registerCrashCallable(new DatasourceCrashCallable());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent ev) {

        ModProxies.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent ev) {
        enableModules();
        ModProxies.postInit();
        config.save();
	}

	@Mod.EventHandler
	public void imcEvent(FMLInterModComms.IMCEvent ev) {
		for (FMLInterModComms.IMCMessage msg : ev.getMessages()) {
			DatasourceProxy.imc(msg);
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent ev) {
		registerCommands();
		ForgePermsAPI.permManager = new PermissionManager(); // temporary for testing, returns true all the time

        rankConfig = new RanksConfig(Constants.CONFIG_FOLDER + "/ranks.json");
		safemode = DatasourceProxy.start(config);
		
		CmdListTown.updateTownSortCache(); // Update cache after everything is loaded
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent ev) {
		config.save();
		DatasourceProxy.stop();
	}

	/**
	 * Registers all commands
	 */
	private void registerCommands() {
		CommandUtils.registerCommand(new CmdTown());
		CommandUtils.registerCommand(new CmdTownAdmin());
	}

	/**
	 * Registers IPlayerTrackers and EventHandlers
	 */
	private void registerHandlers() {
		PlayerTracker playerTracker = new PlayerTracker();
		GameRegistry.registerPlayerTracker(playerTracker);
		MinecraftForge.EVENT_BUS.register(playerTracker);

		TickRegistry.registerTickHandler(VisualsTickHandler.instance, Side.SERVER);
		
		MinecraftForge.EVENT_BUS.register(new MyTownEventHandler());
        MinecraftForge.EVENT_BUS.register(new VanillaEventHandler());
	}

    /**
     * Checks and registers the mods that are enabled
     */
    private void enableModules() {
        enabledModules = new ArrayList<IModule>();

        if(Info.isIc2Available()) {
            enabledModules.add(new IC2Module());
            log.info("Loaded IC2 module");
        }
        // Rest of implementations go here

        for(IModule module : enabledModules)
            module.load();
    }

    public boolean isModuleEnabled(String modid) {
        for(IModule module : enabledModules) {
            if (module.getModID().equals(modid))
                return true;
        }


        return false;
    }

	// ////////////////////////////
	// Helpers
	// ////////////////////////////

	public static Localization getLocal() {
		return LocalizationProxy.getLocalization();
	}

	public static MyTownDatasource getDatasource() {
		return DatasourceProxy.getDatasource();
	}
}