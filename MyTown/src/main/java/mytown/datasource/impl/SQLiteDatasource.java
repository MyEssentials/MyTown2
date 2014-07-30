package mytown.datasource.impl;

import mytown.core.utils.config.ConfigProperty;
import mytown.datasource.MyTownDatasource_SQL;
import mytown.util.Constants;

/**
 * @author Joe Goett
 */
public class SQLiteDatasource extends MyTownDatasource_SQL {
    @ConfigProperty(category="datasource", comment="The database file path. Used by SQLite")
    private String dbPath = Constants.CONFIG_FOLDER + "data.db";

    @Override
    protected void setup() {
        this.dsn = "jdbc:sqlite:" + dbPath;

        // Setup Properties
        dbProperties.put("foreign_keys", "ON");
    }

    @Override
    protected String getDriver() {
        return "org.sqlite.JDBC";
    }
}
