package mytown.proxies.mod;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * 
 * @author Joe Goett
 */
public abstract class ModProxy {
	/**
	 * Returns the name of the ModProxy
	 * 
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Returns the Mod ID of the mod this {@link ModProxy} interacts with, or null if it doesn't interact with a mod
	 * @return
	 */
	public String getModID() {
		return null;
	}

	/**
	 * Runs during {@link FMLPreInitializationEvent}
	 */
	public abstract void preInit();

	/**
	 * Runs during {@link FMLInitializationEvent}
	 * 
	 * Usually used to send {@link IMCMessage}'s to other mods
	 */
	public abstract void init();

	/**
	 * Runs during {@link FMLPostInitializationEvent}
	 */
	public abstract void postInit();
}