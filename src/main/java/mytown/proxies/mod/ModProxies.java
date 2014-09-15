package mytown.proxies.mod;

import cpw.mods.fml.common.Loader;
import mytown.MyTown;

import java.util.ArrayList;
import java.util.List;

public class ModProxies {
    private static List<ModProxy> proxies = new ArrayList<ModProxy>();
    private static boolean loaded = false;

    private ModProxies() {

    }

    public static void load() {

        MyTown.instance.log.info("Starting proxies...");

        proxies.add(new IC2Proxy());
        proxies.add(new BuildCraftProxy());
        proxies.add(new ForgePermsProxy());

        if (!ModProxies.loaded) {
            ModProxies.loaded = true;
            MyTown.instance.config.getCategory("modproxies").setComment("Holds the enable state of the different ModProxies.\nModProxies handle interaction with other mods.\nIf a mod interaction causes issues, just set it to false.");
        }

        // Load ModProxies
        for (ModProxy p : ModProxies.proxies) {
            /*
            if (p.getModID() == null || !Loader.isModLoaded(p.getName())) {// || !MyTown.instance.config.get("ModProxies", p.getName(), true).getBoolean(true)) {
                continue;
            }
            */
            if(p.getModID() != null && Loader.isModLoaded(p.getModID())) {
                p.load();
                p.isLoaded = true;
            }
        }
    }

    /**
     * Adds all the {@link ModProxy}'s to the list
     */
    public static void addProxies() {
    }

    /**
     * Adds the given {@link ModProxy}
     *
     * @param proxy
     */
    public static void addProxy(ModProxy proxy) {
        ModProxies.proxies.add(proxy);
    }

    /**
     * Removes the given {@link ModProxy}
     *
     * @param proxy
     */
    public static void removeProxy(ModProxy proxy) {
        ModProxies.proxies.remove(proxy);
    }

    /**
     * Checks if the proxy with the give mod_id is loaded
     *
     * @param mod_id
     * @return
     */
    public static boolean isProxyLoaded(String mod_id) {
        for(ModProxy proxy : proxies) {
            if(proxy.getModID().equals(mod_id))
                return true;
        }
        return false;
    }
}