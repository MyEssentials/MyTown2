package mytown.economy;

import cpw.mods.fml.common.Loader;
import mytown.MyTown;
import mytown.config.Config;
import mytown.proxies.EconomyProxy;
import mytown.util.MyTownUtils;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by AfterWind on 4/5/2015.
 * Utilities for economy such as payments, refunds, etc.
 */
public class EconomyUtils {

    /**
     * Takes the amount of money specified.
     * Returns false if player doesn't have the money necessary
     */
    public static boolean takeMoneyFromPlayer(EntityPlayer player, int amount) {
        // UtilEconomy eco = new UtilEconomy(player.getUniqueID());
        if(Config.costItemName.startsWith("$")) {
            IEconManager eco = EconomyProxy.economyManagerForUUID(player.getUniqueID());
            if (eco == null) return false;
            int wallet = eco.getWallet();
            if (wallet >= amount) {
                eco.removeFromWallet(amount);
                return true;
            }
            return false;
        } else {
            return MyTownUtils.takeItemFromPlayer(player, Config.costItemName, amount);
        }
    }

    /**
     * Takes the amount of money specified.
     * Returns false if player doesn't have the money necessary
     */
    public static void giveMoneyToPlayer(EntityPlayer player, int amount) {
        if (Config.costItemName.startsWith("$")) {
            //UtilEconomy eco = new UtilEconomy(player.getUniqueID());
            IEconManager eco = EconomyProxy.economyManagerForUUID(player.getUniqueID());
            if (eco == null) return;
            eco.addToWallet(amount);
        } else {
            MyTownUtils.giveItemToPlayer(player, Config.costItemName, amount);
        }
    }

    /**
     * Gets the currency string currently used.
     */
    public static String getCurrency(int amount) {
        if(Config.costItemName.equals("$ForgeEssentials") || Config.costItemName.equals("$Vault")) {
            if (EconomyProxy.econManagerClass == null) {
                return null;
            }
            try {
                IEconManager manager = EconomyProxy.econManagerClass.newInstance();
                return manager.currency(amount);
            } catch(Exception ex) {
                MyTown.instance.log.info("Failed to create IEconManager", ex);
            }
            return "$";

        } else {
            return MyTownUtils.itemStackFromName(Config.costItemName).getDisplayName() + (amount == 1 ? "" : "s");
        }
    }


    /**
     * Returns true if the string matches one of the implemented Economy systems.
     * Returns false if the item based Economy is used.
     */
    public static boolean checkCurrencyString(String currencyString) {
        if(currencyString.equals("$ForgeEssentials")) {
            if(!Loader.isModLoaded("ForgeEssentials") || EconomyProxy.econManagerClass == null)
                throw new RuntimeException("ForgeEssentials economy failed to initialize.");
            return true;
        } else if(currencyString.equals("$Vault")) {
            if(EconomyProxy.econManagerClass == null)
                throw new RuntimeException("Vault economy failed to initialize");
            return true;
        }
        return false;
    }
}
