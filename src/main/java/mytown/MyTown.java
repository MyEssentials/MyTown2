package mytown;

import java.io.File;
import java.util.ArrayList;

import mytown.commands.admin.CmdTownAdmin;
import mytown.commands.town.CmdTown;
import mytown.commands.town.info.CmdListTown;
import mytown.config.Config;
import mytown.core.Localization;
import mytown.core.utils.Log;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.config.ConfigProcessor;
import mytown.crash.DatasourceCrashCallable;
import mytown.handlers.SafemodeHandler;
import mytown.proxies.DatasourceProxy;
import mytown.handlers.PlayerTracker;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.ModProxies;
import mytown.util.Constants;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import forgeperms.api.ForgePermsAPI;

// TODO Add a way to safely reload
// TODO Make sure ALL DB drivers are included when built. Either as a separate mod, or packaged with this. Maybe even make MyTown just DL them at runtime and inject them

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES)
public class MyTown {
	@Instance
	public static MyTown instance;
	public Log log;
	public Configuration config;

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
		registerCommands();
		ForgePermsAPI.permManager = new PermissionManager(); // temporary for testing, returns true all the time
		addDefaultPermissions();
		SafemodeHandler.setSafemode(DatasourceProxy.start(config));
		
		CmdListTown.updateTownSortCache(); // Update cache after everything is loaded
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent ev) {
		config.save();
		DatasourceProxy.stop();
	}

	/**
	 * Adds all the default permissions
	 */
	private void addDefaultPermissions() {
		// TODO: Config files for all default ranks?
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
		CommandUtils.registerCommand(new CmdTown());
		CommandUtils.registerCommand(new CmdTownAdmin());
	}

	/**
	 * Registers all handlers (Event, etc)
	 */
	private void registerHandlers() {
		PlayerTracker playerTracker = new PlayerTracker();
        FMLCommonHandler.instance().bus().register(playerTracker);
        MinecraftForge.EVENT_BUS.register(playerTracker);
        // TODO Re-add the below two handlers after they are redone
		//FMLCommonHandler.instance().bus().register(VisualsTickHandler.instance);
		//MinecraftForge.EVENT_BUS.register(new MyTownEventHandler());
	}

	// ////////////////////////////
	// Helpers
	// ////////////////////////////

	public static Localization getLocal() {
		return LocalizationProxy.getLocalization();
	}
}