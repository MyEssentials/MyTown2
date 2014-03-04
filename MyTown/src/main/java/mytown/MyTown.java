package mytown;

import java.io.File;

import mytown.core.Log;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Constants.MODID, name=Constants.MODNAME, version=Constants.VERSION, dependencies=Constants.DEPENDENCIES)
public class MyTown {
	@Mod.Instance
	public static MyTown INSTANCE;
	
	// Loggers
	Log coreLog;
	Log dbLog;
	Log bypassLog;
	
	// Configs
	Config config;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev){
		// Setup Loggers
		coreLog = new Log("MyTown", FMLLog.getLogger());
		dbLog = new Log("MyTownDB", coreLog.getLogger());
		bypassLog = new Log("MyTownBypass", coreLog.getLogger());
		
		// Read Configs
		config = new Config(new File(ev.getModConfigurationDirectory(), "MyTown/MyTown.cfg"));
	}
}