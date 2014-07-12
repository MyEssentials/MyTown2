package mytown.datasource.impl;

import java.sql.DriverManager;
import java.sql.PreparedStatement;

import mytown.Constants;
import mytown.datasource.types.MyTownDatasource_SQL;
import net.minecraftforge.common.config.Configuration;

// TODO Test
// TODO Add comments
// TODO Profit?

/**
 * SQLite Datasource connector
 * 
 * @author Joe Goett
 */
public class MyTownDatasource_sqlite extends MyTownDatasource_SQL {
	private String dbPath;

	@Override
	public boolean connect() throws Exception {
		if (conn != null)
			return true;

		Class.forName("org.sqlite.JDBC");

		conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		if (conn == null)
			return false;

		MyTownDatasource_SQL.autoIncrement = ""; // SQLite auto increment is a little different than MySQL

		// Update DB
		setUpdates();
		doUpdates();

		PreparedStatement statement = prepare("PRAGMA foreign_keys = ON");
		statement.executeUpdate();

		return true;
	}

	@Override
	protected void doConfig(Configuration config) {
		super.doConfig(config);
		dbPath = config.get(configCat, "Path", Constants.CONFIG_FOLDER + "data.db", "The database file path. Used by SQLite").getString();

		// TODO Finish Config
	}
}