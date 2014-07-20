package mytown.proxies.mod;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.Loader;
import mytown.MyTown;

public class ModProxies {
	private static List<ModProxy> proxies = new ArrayList<ModProxy>();
	private static boolean loaded = false;

	private ModProxies() {
	}

	public static void load() {
		if (ModProxies.loaded == false) {
			ModProxies.loaded = true;
			MyTown.instance.config.getCategory("modproxies").setComment("Holds the enable state of the different ModProxies.\nModProxies handle interaction with other mods.\nIf a mod interaction causes issues, just set it to false.");
		}

		// Load ModProxies
		for (ModProxy p : ModProxies.proxies) {
			if ((p.getModID() != null && !Loader.isModLoaded(p.getName())) || !MyTown.instance.config.get("ModProxies", p.getName(), true).getBoolean(true)) {
				continue;
			}
			p.load();
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
}