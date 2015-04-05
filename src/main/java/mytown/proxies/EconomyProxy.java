package mytown.proxies;

import mytown.MyTown;
import mytown.economy.IEconManager;

import java.util.UUID;

/**
 * @author Joe Goett
 */
public class EconomyProxy {
    public static Class<IEconManager> econManagerClass;

    public static IEconManager economyManagerForUUID(UUID uuid) {
        if (econManagerClass == null) {
            return null;
        }

        try {
            IEconManager manager = econManagerClass.newInstance();
            manager.setUUID(uuid);
            return manager;
        } catch(Exception ex) {
            MyTown.instance.log.info("Failed to create IEconManager", ex);
        }

        return null; // Hopefully this doesn't break things...
    }
}
