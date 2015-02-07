package mytown.util;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;

public interface IEconManager {

    /**
     * Add a set amount to a target's Wallet
     *
     * @param amountToAdd
     * @param player
     */
    public void addToWallet(int amountToAdd);

    /**
     * get the ammount of money the player has
     *
     * @param player
     * @return
     */
    public int getWallet();

    /**
     * Remove a set amount from a target's Wallet
     * returns true if it succeded, false if it didn't
     *
     * @param amountToSubtract
     * @param player
     */
    public boolean removeFromWallet(int amountToSubtract);

    /**
     * Set the target's Wallet to the specified amount
     *
     * @param setAmount
     * @param player
     */
    public void setWallet(int setAmount, EntityPlayer player);

    /**
     * Gets the singular or plural term of the currency used
     *
     * @param amount
     */
    public String currency(int amount);

    /**
     * Gets a combo of getWallet + currency
     *
     * @param username
     * @return returns 'amount' 'currency'
     */
    public String getMoneyString();
    
    /**
     * Saves all wallets to disk 
     * (for users still on the server when it's stopping)
     */
    public void save();

    public Map<String, Integer> getItemTables();
}
