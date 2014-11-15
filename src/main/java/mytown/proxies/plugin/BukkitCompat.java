package mytown.proxies.plugin;

import mytown.MyTown;
import mytown.util.Utils;
import org.bukkit.Bukkit;

import java.io.File;

/**
 * Created by AfterWind on 10/29/2014.
 * Compatibility with bukkit stuff goes here
 */
public class BukkitCompat {

    private static BukkitCompat instance;

    public static BukkitCompat getInstance() {
        if (instance == null)
            instance = new BukkitCompat();
        return instance;
    }

    public boolean hasPlugin(String pluginID) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginID);
    }

    public void loadPEX(File sourceFile) {

        File pluginsFolder = new File(sourceFile.getParentFile().getParentFile(), "plugins/PermissionsEx.jar");
        MyTown.instance.log.info("Trying to load: " + pluginsFolder.toURI().toString());
        if (pluginsFolder.exists())
            try {
              //  Bukkit.
            } catch (Exception e) {
                MyTown.instance.log.error("Failed to load PEX!");
                MyTown.instance.log.error(e.toString());
            }
            //Utils.addURL(pluginsFolder);
            //Utils.injectBukkitBridge(sourceFile, pluginsFolder);
        else
            MyTown.instance.log.error("Plugins folder does not exist, therefore not checking for PEX.");
    }


    public boolean hasPEX() {
        return hasPlugin("PermissionEx");
    }
}
