package mytown.proxies.mod;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public abstract class ModProxy {
	/**
	 * Returns the name of the ModProxy
	 * 
	 * @return
	 */
	public abstract String getName();

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