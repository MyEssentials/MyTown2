package mytown.proxies;

import cpw.mods.fml.common.event.FMLInterModComms;
import mytown.MyTown;
import mytown.config.Config;
import myessentials.config.ConfigProcessor;
import mytown.datasource.InMemoryDatasource;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.impl.MySQLDatasource;
import mytown.datasource.impl.SQLiteDatasource;
import net.minecraftforge.common.config.Configuration;

import java.util.HashMap;
import java.util.Map;

public class DatasourceProxy {
    private static Map<String, Class<?>> dbTypes = new HashMap<String, Class<?>>();
    private static MyTownDatasource datasource = null;

    static {
        registerType("in-memory", InMemoryDatasource.class);
        registerType("mysql", MySQLDatasource.class);
        registerType("sqlite", SQLiteDatasource.class);
    }

    private DatasourceProxy() {
    }

    /**
     * Initializes, configures, and loads the Datasource returning if successful
     */
    public static boolean start(Configuration config) {
        if (!dbTypes.containsKey(Config.dbType.toLowerCase())) {
            MyTown.instance.LOG.error("Unknown Datasource type {}!", Config.dbType.toLowerCase());
            return false;
        }

        try {
            // Create MyTownDatasource instance
            datasource = (MyTownDatasource) dbTypes.get(Config.dbType.toLowerCase()).newInstance();
        } catch (Exception e) {
            MyTown.instance.LOG.error("Failed to instantiate the Datasource ({})!", e, Config.dbType.toLowerCase());
            return false;
        }

        // Load config options
        ConfigProcessor.load(config, datasource.getClass(), datasource);

        // Do actual initialization
        if (!datasource.initialize()) {
            MyTown.instance.LOG.error("Failed to initialize the Datasource!");
            return false;
        }

        // Load everything
        if (!datasource.loadAll()) {
            MyTown.instance.LOG.error("Failed to load the Datasource!");
            return false;
        }

        if (!datasource.checkAllOnStart()) {
            MyTown.instance.LOG.error("Failed to check the Datasource!");
            return false;
        }

        // Yay, initialized/loaded!
        return true;
    }

    /**
     * Stops the Datasource
     */
    public static boolean stop() {
        // TODO Implement stop! xD
        if(!datasource.checkAllOnStop()) {
            MyTown.instance.LOG.error("Failed to check the Datasource!");
            return false;
        }

        return true;
    }

    /**
     * Returns the MyTownDatasource instance
     */
    public static MyTownDatasource getDatasource() {
        return datasource;
    }

    /**
     * Registers the type with the given name
     */
    public static void registerType(String name, Class<?> type) {
        if (dbTypes.containsKey(name)) {
            MyTown.instance.LOG.warn("Type {} already registered!", name);
            return;
        }
        dbTypes.put(name, type);
    }

    /**
     * Handles registering types over IMC
     */
    public static void imc(FMLInterModComms.IMCMessage msg) {
        String[] keyParts = msg.key.split("|");
        if (keyParts.length < 2)
            return;

        if ("registerType".equals(keyParts[1])) {
            String[] msgSplit = msg.getStringValue().split(",");
            String datasourceName = msgSplit[0].toLowerCase();
            String datasourceClassName = msgSplit[1];

            try {
                registerType(datasourceName, Class.forName(datasourceClassName));
            } catch (ClassNotFoundException e) {
                MyTown.instance.LOG.warn("Failed to register datasource type {} from mod {}", e, datasourceName, msg.getSender());
            }
        }
    }
}
