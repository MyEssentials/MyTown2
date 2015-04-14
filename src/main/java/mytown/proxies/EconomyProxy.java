package mytown.proxies;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.utils.economy.Economy;

/**
 * @author Joe Goett
 */
public class EconomyProxy {
    private static Economy economy = null;

    public static void init() {
        economy = new Economy(Config.costItemName);
        MyTown.instance.log.info("Successfully initialized economy!");
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static boolean isItemEconomy() {
        if(Config.costItemName.equals("$ForgeEssentials")) {
            return false;
        } else if(Config.costItemName.equals("$Vault")) {
            return false;
        }
        return true;
    }
}
