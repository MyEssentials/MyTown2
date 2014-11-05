package mytown.commands;

import mytown.MyTown;
import mytown.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Created by AfterWind on 10/29/2014.
 * Compatibility with bukkit stuff goes here
 */
public class BukkitCompat {

    private static BukkitCompat instance;
    public static BukkitCompat getInstance() {
        if(instance == null)
            instance = new BukkitCompat();
        return instance;
    }

    public BukkitCompat() {
        if(Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
            Plugin pex = Bukkit.getPluginManager().getPlugin("PermissionsEx");

            ClassLoader.getSystemClassLoader();

            MyTown.instance.log.info("Found PEX!");
            Utils.addSoftwareLibrary(new File(pex.getDataFolder().getAbsolutePath() + ".jar"));
            PEXCompat.getInstance();
        }
        MyTown.instance.log.info("Initialised bukkit stuff successfully!");
    }

}
