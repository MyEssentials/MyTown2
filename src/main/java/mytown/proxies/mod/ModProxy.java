package mytown.proxies.mod;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;

/**
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
     *
     * @return
     */
    public String getModID() {
        return null;
    }

    /**
     * Loads this {@link ModProxy}, its run during {@link FMLPostInitializationEvent}.
     */
    public abstract void load();
}