package mytown;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import mytown.commands.admin.CmdTownAdmin;
import mytown.commands.town.CmdTown;
import mytown.core.Localization;
import mytown.core.Log;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.config.ConfigProcessor;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.impl.MyTownDatasource_mysql;
import mytown.datasource.impl.MyTownDatasource_sqlite;
import net.minecraft.command.ICommand;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import forgeperms.api.ForgePermsAPI;

// TODO Add a way to safely reload

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION, dependencies = Constants.DEPENDENCIES)
public class MyTown {
	@Mod.Instance
	public static MyTown instance;

	// Permission Manager
	public PermissionManager permManager;

	// Loggers
	public Log coreLog;
	public Log bypassLog;
	public Log datasourceLog;

	// Configs
	public Configuration config;

	// MyTown Localization instance
	public Localization local;

	// Datasource
	public Map<String, Class<?>> datasourceTypes;
	public MyTownDatasource datasource;

	// Set to true to kick all non-admin users out with a custom kick message
	public boolean safemode = false;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		// Register Datasource Types
		datasourceTypes = new Hashtable<String, Class<?>>();
		datasourceTypes.put("mysql", MyTownDatasource_mysql.class);
		datasourceTypes.put("sqlite", MyTownDatasource_sqlite.class);

		// Setup Loggers
		coreLog = new Log("MyTown2", FMLLog.getLogger());
		datasourceLog = new Log("Datasource", coreLog.getLogger());
		bypassLog = new Log("Bypass", coreLog.getLogger());

		Constants.CONFIG_FOLDER = ev.getModConfigurationDirectory().getPath() + "/MyTown/";

		// Read Configs
		config = new Configuration(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));
		ConfigProcessor.processConfig(config, Config.class);

		// Localization
		File localFile = new File(Constants.CONFIG_FOLDER, "localization/" + Config.localization + ".lang");
		if (!localFile.getParentFile().exists()) {
			localFile.getParentFile().mkdir();
		}
		if (!localFile.exists()) {
			InputStream is = MyTown.class.getResourceAsStream("/localization/" + Config.localization + ".lang");
			if (is != null) {
				OutputStream resStreamOut = null;
				int readBytes;
				byte[] buffer = new byte[4096];
				try {
					resStreamOut = new FileOutputStream(localFile);
					while ((readBytes = is.read(buffer)) > 0) {
						resStreamOut.write(buffer, 0, readBytes);
					}
				} catch (IOException e1) {
					// TODO Handle this
				} finally {
					try {
						is.close();
						resStreamOut.close();
					} catch (Exception ignored) {}
				}
			}
		}
		try {
			local = new Localization(new File(Constants.CONFIG_FOLDER, "localization/" + Config.localization + ".lang"));
			local.load();
		} catch (Exception e) {
			coreLog.warning("Localization file %s missing!", Config.localization);
		}
		registerHandlers();
	}

	@Mod.EventHandler
	public void imcEvent(FMLInterModComms.IMCEvent ev) {
		for (FMLInterModComms.IMCMessage msg : ev.getMessages()) {
			if (msg.key == "registerDatasourceType") {
				String[] msgSplit = msg.getStringValue().split(",");
				String datasourceName = msgSplit[0].toLowerCase();
				String datasourceClassName = msgSplit[1];

				try {
					datasourceTypes.put(datasourceName, Class.forName(datasourceClassName));
				} catch (ClassNotFoundException e) {
					coreLog.warning("Failed to register datasource type %s from mod %s. %s", datasourceName, msg.getSender(), e.getLocalizedMessage());
				}
			}
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent ev) {
		registerCommands();
		ForgePermsAPI.permManager = new PermissionManager(); // temporary for testing, returns true all the time
		addDefaultPermissions();
		startDatasource();
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
		if (!datasourceTypes.containsKey(Config.dbType.toLowerCase())) {
			datasourceLog.severe("Failed to find datasource type: %s", Config.dbType.toLowerCase());
			return;
		}

		try {
			datasource = (MyTownDatasource) datasourceTypes.get(Config.dbType.toLowerCase()).newInstance();
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
			datasource.loadRanks();

			// Load links
			datasource.loadResidentToTownLinks();
			datasource.loadTownToNationLinks();
		} catch (Exception e) {
			datasourceLog.severe("Failed to create datasource!", e);
		}
	}

	/*
	 * Adds all the default permissions
	 */
	private void addDefaultPermissions()
	{
		ArrayList<String> pOutsider = new ArrayList<String>();
		ArrayList<String> pResident = new ArrayList<String>();
		ArrayList<String> pAssistant = new ArrayList<String>();
		ArrayList<String> pMayor = new ArrayList<String>();
		
		for(String s : CommandUtils.permissionList.values())
		{
			if(s.startsWith("mytown.cmd"))
			{
				pMayor.add(s);
				if(s.startsWith("mytown.cmd.assistant") || s.startsWith("mytown.cmd.resident") || s.startsWith("mytown.cmd.outsider"))
					pAssistant.add(s);
				if(s.startsWith("mytown.cmd.resident") || s.startsWith("mytown.cmd.outsider"))
					pResident.add(s);
				if(s.startsWith("mytown.cmd.outsider"))
					pOutsider.add(s);
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

		CommandUtils.registerCommand((ICommand)(new CmdTown("town")));
		CommandUtils.registerCommand((ICommand)(new CmdTownAdmin("townadmin")));
	}

	/**
	 * Registers IPlayerTrackers and EventHandlers
	 */
	private void registerHandlers() {
		PlayerTracker handler = new PlayerTracker();
		GameRegistry.registerPlayerTracker(handler);
		MinecraftForge.EVENT_BUS.register(new PlayerEventHandlers());
	}
}