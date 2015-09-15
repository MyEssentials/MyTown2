package mytown.protection.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import mytown.MyTown;
import myessentials.entities.Volume;
import mytown.entities.flag.FlagType;
import mytown.protection.Protection;
import mytown.protection.ProtectionUtils;
import mytown.protection.segment.*;
import mytown.protection.segment.caller.Caller;
import mytown.protection.segment.getter.Getter;
import mytown.util.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;

/**
 * JSON Parser used to parse protection files.
 */
public class ProtectionParser {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Caller.class, new Caller.Serializer())
            .registerTypeAdapter(Getter.class, new Getter.Serializer())
            .registerTypeAdapter(Protection.class, new Protection.Serializer())
            .registerTypeAdapter(Segment.class, new Segment.Serializer())
            .registerTypeAdapter(Volume.class, new Volume.Serializer())
            .registerTypeAdapter(FlagType.class, new FlagType.Serializer())
            .setPrettyPrinting().create();

    private ProtectionParser() {
    }

    public static boolean start() {
        String folderPath = Constants.CONFIG_FOLDER + "protections/";
        File folder = new File(folderPath);
        if(!folder.exists()) {
            if(!folder.mkdir()) {
                return false;
            }
        }

        String[] extensions = new String[1];
        extensions[0] = "json";
        ProtectionUtils.protections.clear();
        Protection vanillaProtection = null;
        for (File file : FileUtils.listFiles(folder, extensions, true)) {
            Protection protection = read(file);
            if (protection != null) {
                if ("Minecraft".equals(protection.modid)) {
                    vanillaProtection = protection;
                } else if (isModLoaded(protection.modid, protection.version)) {
                    MyTown.instance.LOG.info("Adding protection for mod: {}", protection.modid);
                    ProtectionUtils.protections.add(protection);
                }
            }
        }
        if(vanillaProtection != null) {
            MyTown.instance.LOG.info("Adding vanilla protection.");
            ProtectionUtils.protections.add(vanillaProtection);
        }

        if(ProtectionUtils.protections.isEmpty()) {
            MyTown.instance.LOG.warn("No protection files were loaded, consider getting them at http://github.com/MyEssentials/MyTown2-Protections");
            MyTown.instance.LOG.warn("Your server will be missing MOST of the protection without it!");
        }

        return true;
    }

    private static Protection read(File file) {
        try {
            FileReader reader = new FileReader(file);
            MyTown.instance.LOG.info("Loading protection file: {}", file.getName());
            Protection protection = gson.fromJson(reader, Protection.class);
            reader.close();
            return protection;
        } catch (IOException ex) {
            MyTown.instance.LOG.error("Encountered error when parsing protection file: {}", file.getName());
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
            return null;
        }
    }

    private static boolean isModLoaded(String modid, String version) {
        for(ModContainer mod : Loader.instance().getModList()) {
            if(mod.getModId().equals(modid) && mod.getVersion().startsWith(version)) {
                return true;
            }
        }
        return false;
    }
}
