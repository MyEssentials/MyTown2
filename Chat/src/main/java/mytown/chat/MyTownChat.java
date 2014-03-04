package mytown.chat;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "MyTownChat", name="MyTownChat", version="2.0", dependencies="required-after:Forge")
public class MyTownChat {
	@Mod.Instance
	public static MyTownChat INSTANCE;
	
	public ChatHandler chatHandler;
	public Config config;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev){
		config = new Config(new File(ev.getModConfigurationDirectory(), "/MyTown/Chat.cfg"));
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent ev){
		chatHandler = new ChatHandler();
		MinecraftForge.EVENT_BUS.register(chatHandler);
	}
}