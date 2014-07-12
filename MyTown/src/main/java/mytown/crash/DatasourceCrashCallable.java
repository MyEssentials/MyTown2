package mytown.crash;

import java.sql.Connection;
import java.sql.SQLWarning;

import mytown.api.datasource.MyTownDatasource;
import mytown.datasource.types.MyTownDatasource_SQL;
import mytown.proxies.DatasourceProxy;
import cpw.mods.fml.common.ICrashCallable;

// TODO Add more info about the datasource?

/**
 * Adds {@link MyTownDatasource} and {@link Connection} info to the crash report
 * 
 * @author Joe Goett
 */
public class DatasourceCrashCallable implements ICrashCallable {
	@Override
	public String call() throws Exception {
		MyTownDatasource datasource = DatasourceProxy.getDatasource();
		if (datasource == null) {
			return "Datasource is null...";
		}
		String str = "";

		str += String.format("Class: %s\n", datasource.getClass().getName());
		str += String.format("Stats (Towns: %s, Residents: %s, Nations: %s, Blocks: %s, Ranks: %s, Plots: %s)\n", datasource.getTownsMap().size(), datasource.getRanksMap().size(), datasource.getNationsMap().size(), datasource.getTownBlocksMap().size(), datasource.getRanksMap().size(), datasource.getPlotsMap().size());

		// SQL Specific Info
		if (datasource instanceof MyTownDatasource_SQL) {
			MyTownDatasource_SQL sqlDatasource = (MyTownDatasource_SQL) datasource;
			Connection conn = sqlDatasource.getConnection();

			str += String.format("AutoCommit: %s\n", conn.getAutoCommit());

			str += String.format("----- SQL Warnings -----\n");
			str += String.format("%s8 | %s9 | %s\n", "SQLState", "ErrorCode", "Message");
			SQLWarning sqlWarning = conn.getWarnings();
			do {
				str += String.format("%s8 | %s9 | %s\n", sqlWarning.getSQLState(), sqlWarning.getErrorCode(), sqlWarning.getMessage());
			} while (sqlWarning.getNextWarning() != null);
		}

		return str;
	}

	@Override
	public String getLabel() {
		return "MyTown|Datasource";
	}
}