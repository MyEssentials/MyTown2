package mytown.bukkit;

import mytown.MyTown;
import mytown.proxies.EconomyProxy;
import mytown.util.IEconManager;
import mytown.util.UtilEconomy;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;

/**
 * @author Joe Goett
 */
public class BukkitCompat {
    public static void initCompat(File pluginFile) {
        if (MinecraftServer.getServer().getServerModName().contains("cauldron") || MinecraftServer.getServer().getServerModName().contains("mcpc")) {
            Server server = Bukkit.getServer();

            RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                VaultEcon.econ = economyProvider.getProvider();
                if (VaultEcon.econ != null && VaultEcon.econ.isEnabled()) {
                    EconomyProxy.econManagerClass = (Class<IEconManager>) ((Class<?>) VaultEcon.class);
                    MyTown.instance.log.info("Enabling MyTown Bukkit Compat (Economy)");
                }
            }
        } else {
            EconomyProxy.econManagerClass = (Class<IEconManager>) ((Class<?>) UtilEconomy.class);
        }
    }
}
