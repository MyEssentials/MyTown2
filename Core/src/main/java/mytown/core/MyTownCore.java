package mytown.core;

import java.io.File;

import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.config.ConfigProcessor;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "MyTownCore", name="MyTownCore", version="2.0", dependencies="required-after:Forge")
public class MyTownCore {
	@Mod.Instance
	public static MyTownCore Instance;
	
	public Log log;
	
	Configuration config;
	
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent ev) {
		log = new Log("MyTownCore", FMLLog.getLogger());
		
		// Load Configs
		config = new Configuration(new File(ev.getModConfigurationDirectory(), "/MyTown/Core.cfg"));
		ConfigProcessor.processConfig(config, Config.class);
		config.save();
		
		// Register handlers/trackers
		GameRegistry.registerPlayerTracker(new PlayerTracker());
	}
	
	@Mod.EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent ev) {
		try {
			CommandUtils.init();
		} catch (Exception e) {
			log.severe("Failed to initialize CommandUtils!", e);
		}
	}
}