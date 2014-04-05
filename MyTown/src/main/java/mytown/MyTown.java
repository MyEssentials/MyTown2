package mytown;

import java.io.File;

import mytown.commands.CmdNewTown;
import mytown.core.Log;
import mytown.core.utils.command.CommandUtils;
import mytown.datasource.MyTownDatasource;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES)
public class MyTown {
	@Mod.Instance
	public static MyTown INSTANCE;

	// Loggers
	public Log coreLog;
	public Log bypassLog;
	public Log datasourceLog;

	// Configs
	public Config config;

	// Datasource
	public MyTownDatasource datasource;
	
	// Set to true to kick all non-admin users out with a custom kick message
	public boolean safemode = false;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		// Setup Loggers
		coreLog = new Log("MyTown2", FMLLog.getLogger());
		datasourceLog = new Log("Datasource", coreLog.getLogger());
		bypassLog = new Log("Bypass", coreLog.getLogger());

		Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory() + "/MyTown/";

		// Read Configs
		config = new Config(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent ev) {
		startDatasource();
		registerCommands();
		registerHandlers();
	}

	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent ev) {
		try {
			datasource.save();
			datasource.disconnect();
		} catch (Exception e) {
			e.printStackTrace(); // TODO Maybe change?
		}
		config.save();
	}

	/**
	 * Configures and Loads the Datasource
	 */
	private void startDatasource() {
		try {
			// TODO Change how datasources are loaded
			datasource = (MyTownDatasource) Class.forName("mytown.datasource.impl.MyTownDatasource_" + config.dbType.toLowerCase()).newInstance();
			datasource.configure(config, datasourceLog);
			config.save();
			if (!datasource.connect()) {
				datasourceLog.severe("Failed to connect to datasource!");
				safemode = true;
			}
			
			// Load everything
			datasource.loadResidents();
			datasource.loadTowns();
			datasource.loadNations();
			
			// Load links
			datasource.loadResidentToTownLinks();
			datasource.loadTownToNationLinks();
		} catch (ClassNotFoundException ex) {
			datasourceLog.severe("Failed to find datasource type: %s", config.dbType);
		} catch (InstantiationException e) {
			datasourceLog.severe("Failed to create datasource. %s", e.getLocalizedMessage());
		} catch (Exception e) {
			safemode = true;
			e.printStackTrace(); // TODO Maybe change?
		}
	}
	
	/**
	 * Registers all commands
	 */
	private void registerCommands() {
		CommandUtils.registerCommand(new CmdNewTown());
	}
	
	/**
	 * Registers IPlayerTrackers and EventHandlers
	 */
	private void registerHandlers() {
		EventHandler handler = new EventHandler();
		
		MinecraftForge.EVENT_BUS.register(handler);
		GameRegistry.registerPlayerTracker(handler);
	}
}