package mytown.proxies;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.economy.Economy;

public class EconomyProxy {

    private static Economy economy = null;

    private EconomyProxy() {
    }

    public static void init() {
        economy = new Economy(Config.costItemName);
        MyTown.instance.LOG.info("Successfully initialized economy!");
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static boolean isItemEconomy() {
        if(Config.costItemName.equals(Economy.CURRENCY_FORGE_ESSENTIALS)) {
            return false;
        } else if(Config.costItemName.equals(Economy.CURRENCY_VAULT)) {
            return false;
        }
        return true;
    }

    public static String getCurrency(int amount) {
        String currency = economy.getCurrency(amount);
        if(Character.isDigit(currency.charAt(0)))
            return currency;
        else
            return amount + " " + currency;
    }
}
