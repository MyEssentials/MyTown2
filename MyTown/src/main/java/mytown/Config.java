package mytown;

import java.io.File;

import net.minecraftforge.common.Configuration;

// TODO Do config
public class Config extends Configuration {
	public String dbType = "";
	
	public Config(File file){
		super(file);
		load();
		loadDatasource();
		save();
	}
	
	private void loadDatasource() {
		dbType = get("datasource", "Type", "SQLite", "Datasource Type. Eg: MySQL, SQLite, etc").getString();
	}
	
	@Override
	public void save() {
		if (hasChanged()) {
			super.save();
		}
	}
}