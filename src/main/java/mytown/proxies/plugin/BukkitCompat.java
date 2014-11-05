package mytown.proxies.plugin;

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

    public boolean hasPlugin(String pluginID) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginID);
    }

    public void loadPEX(File sourceFile) {
        File pluginsFolder = new File("plugins/PermissionsEx.jar");
        if(pluginsFolder.exists())
            Utils.addURL(pluginsFolder);
            //Utils.injectBukkitBridge(sourceFile, pluginsFolder);
        else
            MyTown.instance.log.error("Plugins folder does not exist, therefore not checking for PEX.");
    }


    public boolean hasPEX() { return hasPlugin("PermissionEx"); }
}
