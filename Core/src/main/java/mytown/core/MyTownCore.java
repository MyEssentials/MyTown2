package mytown.core;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid = "MyTownCore", name="MyTownCore", version="2.0", dependencies="required-after:Forge")
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class MyTownCore {
	Log log;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev){
		log = new Log("MyTownCore");
	}
}