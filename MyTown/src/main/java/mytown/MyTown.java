package mytown;

import java.io.File;
import java.util.ArrayList;

import mytown.commands.admin.CmdTownAdmin;
import mytown.commands.town.CmdTown;
import mytown.config.Config;
import mytown.core.Localization;
import mytown.core.utils.Log;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.config.ConfigProcessor;
import mytown.datasource.MyTownDatasource;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.ModProxies;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import forgeperms.api.ForgePermsAPI;

// TODO Add a way to safely reload

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES)
public class MyTown {
	@Mod.Instance("MyTown")
	public static MyTown instance;

	// Permission Manager
	public PermissionManager permManager;

	// Loggers
	public Log log;

	// Configs
	public Configuration config;

	// Set to true to kick all non-admin users out with a custom kick message
	public boolean safemode = false;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		// Setup Loggers
		log = new Log(ev.getModLog());

		Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory().getPath() + "/MyTown/";

		// Read Configs
		config = new Configuration(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));
		ConfigProcessor.processConfig(config, Config.class);
		LocalizationProxy.load();
		registerHandlers();

		// ModProxy PreInit
		ModProxies.addProxies();
		ModProxies.postInit();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent ev) {
		ModProxies.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent ev) {
		ModProxies.postInit();
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
		addDefaultPermissions();
		safemode = DatasourceProxy.start(config);
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent ev) {
		config.save();
		DatasourceProxy.stop();
	}

	/*
	 * Adds all the default permissions
	 */
	private void addDefaultPermissions() {
		ArrayList<String> pOutsider = new ArrayList<String>();
		ArrayList<String> pResident = new ArrayList<String>();
		ArrayList<String> pAssistant = new ArrayList<String>();
		ArrayList<String> pMayor = new ArrayList<String>();

		for (String s : CommandUtils.permissionList.values()) {
			if (s.startsWith("mytown.cmd")) {
				pMayor.add(s);
				if (s.startsWith("mytown.cmd.assistant") || s.startsWith("mytown.cmd.resident") || s.startsWith("mytown.cmd.outsider")) {
					pAssistant.add(s);
				}
				if (s.startsWith("mytown.cmd.resident") || s.startsWith("mytown.cmd.outsider")) {
					pResident.add(s);
				}
				if (s.startsWith("mytown.cmd.outsider")) {
					pOutsider.add(s);
				}
			}
		}
		Constants.DEFAULT_RANK_VALUES.put("Outsider", pOutsider);
		Constants.DEFAULT_RANK_VALUES.put("Resident", pResident);
		Constants.DEFAULT_RANK_VALUES.put("Assistant", pAssistant);
		Constants.DEFAULT_RANK_VALUES.put("Mayor", pMayor);
	}

	/**
	 * Registers all commands
	 */
	private void registerCommands() {
		CommandUtils.registerCommand(new CmdTown("town"));
		CommandUtils.registerCommand(new CmdTownAdmin("townadmin"));
	}

	/**
	 * Registers IPlayerTrackers and EventHandlers
	 */
	private void registerHandlers() {
		PlayerTracker playerTracker = new PlayerTracker();
		GameRegistry.registerPlayerTracker(playerTracker);
		MinecraftForge.EVENT_BUS.register(playerTracker);
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