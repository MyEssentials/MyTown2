package mytown.x_datasource.impl;

import java.sql.DriverManager;
import java.sql.PreparedStatement;

import mytown.core.utils.config.ConfigProperty;
import mytown.x_datasource.types.MyTownDatasource_SQL;
import mytown.util.Constants;

// TODO Test
// TODO Add comments
// TODO Profit?

/**
 * SQLite Datasource connector
 * 
 * @author Joe Goett
 */
public class MyTownDatasource_sqlite extends MyTownDatasource_SQL {
	@ConfigProperty(category="datasource", comment="The database file path. Used by SQLite")
	private String dbPath = Constants.CONFIG_FOLDER + "data.db";

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
}