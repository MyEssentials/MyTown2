package mytown;

import java.io.File;

import mytown.core.Log;
import mytown.datasource.MyTownDatasource;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

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

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		// Setup Loggers
		coreLog = new Log("MyTown2", FMLLog.getLogger());
		datasourceLog = new Log("Datasource", coreLog.getLogger());
		bypassLog = new Log("Bypass", coreLog.getLogger());

		Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory() + "/MyTown/";

		// Read Configs
		config = new Config(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));
		config.load();
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent ev) {
		try { // Try to load the Datasource
			datasource = (MyTownDatasource) Class.forName("mytown.datasource.impl.MyTownDatasource_" + config.dbType.toLowerCase()).newInstance();
			datasource.configure(config, datasourceLog);
			if (config.hasChanged())
				config.save();
			if (!datasource.connect()) {
				datasourceLog.severe("Failed to connect to datasource!");
			}
		} catch (ClassNotFoundException ex) {
			datasourceLog.severe("Failed to find datasource type: %s", config.dbType);
		} catch (InstantiationException e) {
			datasourceLog.severe("Failed to create datasource. %s", e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace(); // TODO Maybe change?
		}
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
}