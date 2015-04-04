package mytown.proxies;

import cpw.mods.fml.common.event.FMLInterModComms;
import mytown.config.Config;
import mytown.core.utils.Log;
import mytown.core.utils.config.ConfigProcessor;
import mytown.datasource.InMemoryDatasource;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.impl.MySQLDatasource;
import mytown.datasource.impl.SQLiteDatasource;
import net.minecraftforge.common.config.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joe Goett
 */
public class DatasourceProxy {
    private static Map<String, Class<?>> dbTypes = new HashMap<String, Class<?>>();
    private static MyTownDatasource datasource = null;
    private static Log log = null;

    static {
        registerType("in-memory", InMemoryDatasource.class);
        registerType("mysql", MySQLDatasource.class);
        registerType("sqlite", SQLiteDatasource.class);
    }

    /**
     * Initializes, configures, and loads the Datasource returning if successful
     *
     * @return True if started successful
     */
    public static boolean start(Configuration config) {
        if (!dbTypes.containsKey(Config.dbType.toLowerCase())) {
            log.error("Unknown Datasource type %s!", Config.dbType.toLowerCase());
            return false;
        }

        try {
            // Create MyTownDatasource instance
            datasource = (MyTownDatasource) dbTypes.get(Config.dbType.toLowerCase()).newInstance();
            datasource.setLog(log);
        } catch (Exception e) {
            log.error("Failed to instantiate the Datasource (%s)!", e, Config.dbType.toLowerCase());
            return false;
        }

        // Load config options
        ConfigProcessor.load(config, datasource.getClass(), datasource);

        // Do actual initialization
        if (!datasource.initialize()) {
            log.error("Failed to initialize the Datasource!");
            return false;
        }

        // Load everything
        if (!datasource.loadAll()) {
            log.error("Failed to load the Datasource!");
            return false;
        }

        if (!datasource.checkAllOnStart()) {
            log.error("Failed to check the Datasource!");
            return false;
        }

        // Yay, initialized/loaded!
        return true;
    }

    /**
     * Stops the Datasource
     *
     * @return
     */
    public static boolean stop() {
        // TODO Implement stop! xD
        if(!datasource.checkAllOnStop()) {
            log.error("Failed to check the Datasource!");
            return false;
        }

        return true;
    }

    /**
     * Returns the MyTownDatasource instance
     *
     * @return
     */
    public static MyTownDatasource getDatasource() {
        return datasource;
    }

    /**
     * Sets the Log for this Proxy. Useful when writing JUnit tests that doesn't have access to the MyTown instance
     *
     * @param log
     */
    public static void setLog(Log log) {
        DatasourceProxy.log = log;
    }

    /**
     * Registers the type with the given name
     *
     * @param name Name of the type
     * @param type The Class of the the type
     */
    public static void registerType(String name, Class<?> type) {
        if (dbTypes.containsKey(name)) {
            log.warn("Type %s already registered!", name);
            return;
        }
        dbTypes.put(name, type);
    }

    /**
     * Handles registering types over IMC
     *
     * @param msg
     */
    public static void imc(FMLInterModComms.IMCMessage msg) {
        String[] keyParts = msg.key.split("|");
        if (keyParts.length < 2) return;

        if (keyParts[1].equals("registerType")) {
            String[] msgSplit = msg.getStringValue().split(",");
            String datasourceName = msgSplit[0].toLowerCase();
            String datasourceClassName = msgSplit[1];

            try {
                registerType(datasourceName, Class.forName(datasourceClassName));
            } catch (ClassNotFoundException e) {
                log.warn("Failed to register datasource type %s from mod %s", e, datasourceName, msg.getSender());
            }
        }
    }

    private DatasourceProxy() {
    }
}
