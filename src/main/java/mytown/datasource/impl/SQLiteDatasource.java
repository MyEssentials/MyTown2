package mytown.datasource.impl;

import myessentials.config.ConfigProperty;
import mytown.datasource.MyTownDatasourceSQL;
import mytown.util.Constants;

public class SQLiteDatasource extends MyTownDatasourceSQL {
    @ConfigProperty(category = "datasource.sql", comment = "The database file path. Used by SQLite")
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
