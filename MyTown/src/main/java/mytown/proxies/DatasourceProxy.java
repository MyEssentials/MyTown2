package mytown.proxies;

import java.util.Hashtable;
import java.util.Map;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.config.Config;
import mytown.core.utils.Log;
import mytown.core.utils.config.ConfigProcessor;
import mytown.datasource.impl.MyTownDatasource_mysql;
import mytown.datasource.impl.MyTownDatasource_sqlite;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.event.FMLInterModComms;

/**
 * Handles all of the Datasource stuffs
 * 
 * @author Joe Goett
 */
public class DatasourceProxy {
	private static MyTownDatasource datasource;
	private static Map<String, Class<?>> types = new Hashtable<String, Class<?>>();
	private static Log log = MyTown.instance.log.createChild("Datasource");
	
	private DatasourceProxy() {}

	/**
	 * Adds the default datasource types Should be added when this class is first accessed
	 */
	static {
		DatasourceProxy.types.put("mysql", MyTownDatasource_mysql.class);
		DatasourceProxy.types.put("sqlite", MyTownDatasource_sqlite.class);
	}

	/**
	 * Starts the datasource
	 * 
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public static boolean start(Configuration config) {
		if (!DatasourceProxy.types.containsKey(Config.dbType.toLowerCase())) {
			DatasourceProxy.log.fatal("Failed to find datasource type: %s", Config.dbType.toLowerCase());
			return false;
		}
		try {
			DatasourceProxy.datasource = (MyTownDatasource) DatasourceProxy.types.get(Config.dbType.toLowerCase()).newInstance();
			DatasourceProxy.datasource.configure(config, DatasourceProxy.log);
			ConfigProcessor.load(config, datasource.getClass(), datasource);
			config.save();
			if (!DatasourceProxy.datasource.connect()) {
				DatasourceProxy.log.fatal("Failed to connect to datasource!");
				return false;
			}

			// Load everything
			DatasourceProxy.datasource.loadTowns();
			DatasourceProxy.datasource.loadResidents();
			DatasourceProxy.datasource.loadTownFlags();
			DatasourceProxy.datasource.loadNations();
			DatasourceProxy.datasource.loadRanks();
			DatasourceProxy.datasource.loadPlots();
			DatasourceProxy.datasource.loadPlotFlags();

			// Load links
			DatasourceProxy.datasource.loadResidentToTownLinks();
			DatasourceProxy.datasource.loadTownToNationLinks();
		} catch (Exception ex) {
			DatasourceProxy.log.fatal("Failed to start the datasource.", ex);
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
			DatasourceProxy.datasource.save();
			DatasourceProxy.datasource.disconnect();
		} catch (Exception ex) {
			DatasourceProxy.log.fatal("Failed to stop the datasource.", ex);
		}
	}

	/**
	 * Parses the given IMCMessage on behalf of the datasource
	 * 
	 * @param msg
	 */
	public static void imc(FMLInterModComms.IMCMessage msg) {
		String[] keyParts = msg.key.split("|");
		if (keyParts.length < 2) return;
		
		if (keyParts[1] == "registerType") {
			String[] msgSplit = msg.getStringValue().split(",");
			String datasourceName = msgSplit[0].toLowerCase();
			String datasourceClassName = msgSplit[1];
			
			try {
				DatasourceProxy.registerType(datasourceName, Class.forName(datasourceClassName));
			} catch (ClassNotFoundException e) {
				DatasourceProxy.log.warn("Failed to register datasource type %s from mod %s.", e, datasourceName, msg.getSender());
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
		DatasourceProxy.types.put(name, clazz);
	}

	public static MyTownDatasource getDatasource() {
		return DatasourceProxy.datasource;
	}
}