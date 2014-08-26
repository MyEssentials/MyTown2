package mytown.proxies;

import cpw.mods.fml.common.event.FMLInterModComms;
import mytown.MyTown;
import mytown.api.x_datasource.MyTownDatasource;
import mytown.config.Config;
import mytown.core.utils.Log;
import mytown.core.utils.config.ConfigProcessor;
import mytown.x_datasource.impl.MyTownDatasource_mysql;
import mytown.x_datasource.impl.MyTownDatasource_sqlite;
import net.minecraftforge.common.config.Configuration;

import java.util.Hashtable;
import java.util.Map;

/**
 * Handles all of the Datasource stuffs
 *
 * @author Joe Goett
 */
public class X_DatasourceProxy {
    private static MyTownDatasource datasource;
    private static Map<String, Class<?>> types = new Hashtable<String, Class<?>>();
    private static Log log = MyTown.instance.log.createChild("Datasource");

    private X_DatasourceProxy() {
    }

    /**
     * Adds the default datasource types Should be added when this class is first accessed
     */
    static {
        X_DatasourceProxy.types.put("mysql", MyTownDatasource_mysql.class);
        X_DatasourceProxy.types.put("sqlite", MyTownDatasource_sqlite.class);
    }

    /**
     * Starts the datasource
     *
     * @param config
     * @return
     * @throws Exception
     */
    public static boolean start(Configuration config) {
        if (!X_DatasourceProxy.types.containsKey(Config.dbType.toLowerCase())) {
            X_DatasourceProxy.log.fatal("Failed to find datasource type: %s", Config.dbType.toLowerCase());
            return false;
        }
        try {
            X_DatasourceProxy.datasource = (MyTownDatasource) X_DatasourceProxy.types.get(Config.dbType.toLowerCase()).newInstance();
            X_DatasourceProxy.datasource.configure(X_DatasourceProxy.log);
            ConfigProcessor.load(config, datasource.getClass(), datasource);
            config.save();
            if (!X_DatasourceProxy.datasource.connect()) {
                X_DatasourceProxy.log.fatal("Failed to connect to datasource!");
                return false;
            }

            // Load everything
            X_DatasourceProxy.datasource.loadTowns();
            X_DatasourceProxy.datasource.loadResidents();
            X_DatasourceProxy.datasource.loadTownFlags();
            X_DatasourceProxy.datasource.loadNations();
            X_DatasourceProxy.datasource.loadRanks();
            X_DatasourceProxy.datasource.loadPlots();
            X_DatasourceProxy.datasource.loadPlotFlags();

            // Load links
            X_DatasourceProxy.datasource.loadResidentToTownLinks();
            X_DatasourceProxy.datasource.loadTownToNationLinks();
        } catch (Exception ex) {
            X_DatasourceProxy.log.fatal("Failed to start the datasource.", ex);
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
            X_DatasourceProxy.datasource.save();
            X_DatasourceProxy.datasource.disconnect();
        } catch (Exception ex) {
            X_DatasourceProxy.log.fatal("Failed to stop the datasource.", ex);
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
                X_DatasourceProxy.registerType(datasourceName, Class.forName(datasourceClassName));
            } catch (ClassNotFoundException e) {
                X_DatasourceProxy.log.warn("Failed to register datasource type %s from mod %s.", e, datasourceName, msg.getSender());
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
        X_DatasourceProxy.types.put(name, clazz);
    }

    public static MyTownDatasource getDatasource() {
        return X_DatasourceProxy.datasource;
    }
}