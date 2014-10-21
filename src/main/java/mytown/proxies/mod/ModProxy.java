package mytown.proxies.mod;

import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import mytown.protection.Protection;
import mytown.protection.Protections;

/**
 * @author Joe Goett
 */
public class ModProxy {

    private String name, modid;
    private Class<? extends Protection> protClass;
    private Protection prot;

    public ModProxy(String name, String modid, Class<? extends Protection> protClass) {
        this.name = name;
        this.modid = modid;
        this.protClass = protClass;
    }

    public boolean isLoaded = false;
    /**
     * Returns the name of the ModProxy
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the Mod ID of the mod this {@link ModProxy} interacts with.
     *
     * @return
     */
    public String getModID() {
        return this.modid;
    }

    /**
     * Loads this {@link ModProxy}, its run during {@link FMLPostInitializationEvent}.
     */
    public void load() {
        try {
            prot = protClass.newInstance();
            Protections.instance.addProtection(prot, modid);
        } catch (InstantiationException e) { // TODO Log Exception
            e.printStackTrace();
        } catch (IllegalAccessException e) { // TODO Log Exception
            e.printStackTrace();
        }
    }
}