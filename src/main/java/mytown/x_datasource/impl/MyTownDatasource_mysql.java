package mytown.x_datasource.impl;

import mytown.core.utils.config.ConfigProperty;
import mytown.x_datasource.types.MyTownDatasource_SQL;

import java.sql.DriverManager;
import java.util.Properties;

//TODO Test
//TODO Add comments
//TODO Profit?

/**
 * MySQL Datasource connector
 *
 * @author Joe Goett
 */
public class MyTownDatasource_mysql extends MyTownDatasource_SQL {
    // Config
    @ConfigProperty(category = "datasource", comment = "Username to use when connecting")
    private String username = "";

    @ConfigProperty(category = "datasource", comment = "Password to use when connecting")
    private String password = "";

    @ConfigProperty(category = "datasource", comment = "Hostname:Port of the db server")
    private String host = "localhost";

    @ConfigProperty(category = "datasource", comment = "The database name")
    private String database = "mytown";

    // //////////////////////////////////////
    // Helpers
    // //////////////////////////////////////

    public String getDatabasePath() {
        return "//" + host + "/" + database;
    }

    // //////////////////////////////////////
    // Implementation
    // //////////////////////////////////////

    @Override
    public boolean connect() throws Exception {
        if (conn != null)
            return true;

        Class.forName("com.mysql.jdbc.Driver");

        Properties properties = new Properties();
        properties.put("autoReconnect", "true");
        properties.put("user", username);
        properties.put("password", password);

        conn = DriverManager.getConnection("jdbc:mysql:" + getDatabasePath(), properties);
        if (conn == null)
            return false; // TODO Log error?

        // Update DB
        setUpdates();
        doUpdates();

        return true;
    }
}