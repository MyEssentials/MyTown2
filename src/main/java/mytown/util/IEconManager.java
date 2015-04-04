package mytown.util;

import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

public interface IEconManager {

    public void setUUID(UUID uuid);

    /**
     * Add a set amount to a target's Wallet
     */
    public void addToWallet(int amountToAdd);

    /**
     * Get the amount of money the player has
     */
    public int getWallet();

    /**
     * Remove a set amount from a target's Wallet
     * returns true if it succeded, false if it didn't
     */
    public boolean removeFromWallet(int amountToSubtract);

    /**
     * Set the target's Wallet to the specified amount
     */
    public void setWallet(int setAmount, EntityPlayer player);

    /**
     * Gets the singular or plural term of the currency used
     */
    public String currency(int amount);

    /**
     * Gets a combo of getWallet + currency
     */
    public String getMoneyString();
    
    /**
     * Saves all wallets to disk
     * (for users still on the server when it's stopping)
     */
    public void save();

    public Map<String, Integer> getItemTables();
}
