package mytown.bukkit;

import mytown.util.IEconManager;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.entity.player.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

/**
 * @author Joe Goett
 */
public class VaultEcon implements IEconManager {
    public static Economy econ;

    private UUID uuid;
    private OfflinePlayer player;

    public VaultEcon(UUID uuid) {
        this.uuid = uuid;
        player = Bukkit.getServer().getOfflinePlayer(uuid);
    }

    public VaultEcon() {
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
        player = Bukkit.getServer().getOfflinePlayer(uuid);
    }

    @Override
    public void addToWallet(int amountToAdd) {
        econ.depositPlayer(player, amountToAdd);
    }

    @Override
    public int getWallet() {
        return (int) econ.getBalance(player);
    }

    @Override
    public boolean removeFromWallet(int amountToSubtract) {
        return econ.withdrawPlayer(player, amountToSubtract).transactionSuccess();
    }

    @Override
    public void setWallet(int setAmount, EntityPlayer player) {
        // TODO Find some way to support this?
    }

    @Override
    public String currency(int amount) {
        return econ.format(amount);
    }

    @Override
    public String getMoneyString() {
        return currency(getWallet());
    }

    @Override
    public void save() {
    }

    @Override
    public Map<String, Integer> getItemTables() {
        return null;
    }
}
