package mytown.datasource.impl;

import mytown.datasource.types.MyTownDatasource_SQL;

public class MyTownDatasource_mongodb extends MyTownDatasource_SQL {
	@Override
	public boolean connect() throws Exception {
		log.severe("MongoDB Support not finished!");
		return false;
	}
}