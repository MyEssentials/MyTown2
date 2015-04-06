package mytown.proxies;

import mytown.config.Config;
import mytown.core.utils.economy.Economy;

/**
 * @author Joe Goett
 */
public class EconomyProxy {
    private static Economy economy = null;

    public static Economy economy() {
        if (economy == null) {
            economy = new Economy(Config.costItemName);
        }
        return economy;
    }
}
