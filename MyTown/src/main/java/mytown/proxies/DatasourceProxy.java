package mytown.proxies;

import java.util.Hashtable;
import java.util.Map;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.Log;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.impl.MyTownDatasource_mysql;
import mytown.datasource.impl.MyTownDatasource_sqlite;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.event.FMLInterModComms;

/**
 * Handles all of the Datasource stuffs
 * 
 * @author Joe Goett
 */
public class DatasourceProxy {
	private static MyTownDatasource datasource;
	private static Map<String, Class<?>> types = new Hashtable<String, Class<?>>();
	private static Log log = new Log("Datasource", MyTown.instance.coreLog.getLogger());

	/**
	 * Adds the default datasource types
	 */
	private static void loadTypes() {
		types.put("mysql", MyTownDatasource_mysql.class);
		types.put("sqlite", MyTownDatasource_sqlite.class);
	}

	/**
	 * Starts the datasource
	 * 
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public static boolean start(Configuration config) {
		loadTypes(); // Assuming this is the right way
		if (!types.containsKey(Config.dbType.toLowerCase())) {
			log.severe("Failed to find datasource type: %s", Config.dbType.toLowerCase());
			return false;
		}
		try {
			datasource = (MyTownDatasource) types.get(Config.dbType.toLowerCase()).newInstance();
			datasource.configure(config, log);
			config.save();
			if (!datasource.connect()) {
				log.severe("Failed to connect to datasource!");
				return false;
			}

			// Load everything
			datasource.loadResidents();
			datasource.loadTowns();
			datasource.loadNations();
			datasource.loadRanks();

			// Load links
			datasource.loadResidentToTownLinks();
			datasource.loadTownToNationLinks();
		} catch (Exception ex) {
			log.severe("Failed to start the datasource.", ex);
			return false;
		}
		return true;
	}

	/**
	 * Saves and disconnects the datasource
	 * 
	 * @throws Exception
	 */
	public static void stop() {
		try {
			datasource.save();
			datasource.disconnect();
		} catch (Exception ex) {
			log.severe("Failed to stop the datasource.", ex);
		}
	}

	/**
	 * Parses the given IMCMessage on behalf of the datasource
	 * 
	 * @param msg
	 */
	public static void imc(FMLInterModComms.IMCMessage msg) {
		if (msg.key == "registerDatasourceType") {
			String[] msgSplit = msg.getStringValue().split(",");
			String datasourceName = msgSplit[0].toLowerCase();
			String datasourceClassName = msgSplit[1];

			try {
				registerType(datasourceName, Class.forName(datasourceClassName));
			} catch (ClassNotFoundException e) {
				log.warning("Failed to register datasource type %s from mod %s. %s", datasourceName, msg.getSender(), e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Registers a Datasource type to the proxy
	 * 
	 * @param name
	 * @param clazz
	 */
	public static void registerType(String name, Class<?> clazz) {
		types.put(name, clazz);
	}

	public static MyTownDatasource getDatasource() {
		return datasource;
	}
}