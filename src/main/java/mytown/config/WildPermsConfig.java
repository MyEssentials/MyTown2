package mytown.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import mytown.entities.Wild;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 10/2/2014.
 * Wilderness flags
 */
public class WildPermsConfig {

    private Type type = new TypeToken<List<Flag>>() {}.getType();
    private Gson gson;
    private String path;

    public WildPermsConfig(File file) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        this.path = file.getPath();

        if(!file.exists() || file.isDirectory()) {
            MyTown.instance.log.info("Creating WildPerms config file!");
            writeFile();
        } else {
            readFile();
        }
    }

    public void saveChanges() {
        try {
            Writer writer = new FileWriter(path);
            gson.toJson(Wild.getInstance().getFlags(), type, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            MyTown.instance.log.error("An error occurred when trying to save wild perms.");
        }
    }


    @SuppressWarnings("unchecked")
    private void writeFile() {
        for(FlagType type : FlagType.values()) {
            if(type.isWildPerm())
                Wild.getInstance().addFlag(new Flag(type, type.getDefaultWildPerm()));
        }

        try {
            Writer writer = new FileWriter(path);
            gson.toJson(Wild.getInstance().getFlags(), type, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            MyTown.instance.log.error("An error occurred when trying to save wild perms.");
        }
    }

    @SuppressWarnings("unchecked")
    private void readFile() {
        List<Flag> list = null;
        try {
            Reader reader = new FileReader(path);
            list = gson.fromJson(reader, type);
            reader.close();
            MyTown.instance.log.info("Loaded wild perms successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            MyTown.instance.log.error("An error occurred when trying to load wild perms.");
        }
        if(list != null) {
            for (Flag f : list) {
                if(f.flagType.isWildPerm())
                    Wild.getInstance().addFlag(f);
                else {
                    MyTown.instance.log.error("A non wild perm has been found in the wild perm list. Deleting and saving: " + f.flagType.toString());
                }
            }
            for(FlagType type : FlagType.values()) {
                if(type.isWildPerm()) {
                    boolean ok = false;
                    for (Flag f : list) {
                        if (f.flagType == type)
                            ok = true;
                    }
                    if (!ok) {
                        MyTown.instance.log.error("A wild perm flag did not exist, adding to the list and saving: " + type.toString());
                        Wild.getInstance().addFlag(new Flag(type, type.getDefaultValue()));
                    }
                }
            }
            saveChanges();
        } else {
            MyTown.instance.log.error("Failed to read wild perms!");
        }
    }
}
