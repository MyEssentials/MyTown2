package mytown.bukkit;

import mytown.MyTown;
import mytown.config.Config;
import mytown.economy.forgeessentials.ForgeessentialsEconomy;
import mytown.economy.vault.VaultEconomy;
import mytown.proxies.EconomyProxy;
import mytown.economy.IEconManager;
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
        if(Config.costItemName.equals("$Vault")) {
            if (MinecraftServer.getServer().getServerModName().contains("cauldron") || MinecraftServer.getServer().getServerModName().contains("mcpc")) {
                Server server = Bukkit.getServer();

                RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = server.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (economyProvider != null) {
                    VaultEconomy.econ = economyProvider.getProvider();
                    if (VaultEconomy.econ != null && VaultEconomy.econ.isEnabled()) {
                        EconomyProxy.econManagerClass = (Class<IEconManager>) ((Class<?>) VaultEconomy.class);
                        MyTown.instance.log.info("Enabling Vault economy system!");
                    }
                }
            }
        } else if(Config.costItemName.equals("$ForgeEssentials")) {
            EconomyProxy.econManagerClass = (Class<IEconManager>) ((Class<?>) ForgeessentialsEconomy.class);
            MyTown.instance.log.info("Enabling ForgeEssentials economy system!");
        }
    }
}
