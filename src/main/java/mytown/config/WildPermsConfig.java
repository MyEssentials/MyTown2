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


    private void writeFile() {
        //Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.enter, false));
        Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.breakBlocks, true));
        Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.explosions, false));
        Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.accessBlocks, true));
        Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.activateBlocks, true));
        //Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.useItems, false));
        //Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.pickupItems, true));
        //Wild.getInstance().addFlag(new Flag<String>(FlagType.mobs, "all"));
        //Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.attackEntities, false));
        Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.placeBlocks, true));
        //Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.pumps, true));
        //Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.ic2EnergyFlow, false));
        Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.bcBuildingMining, true));
        //Wild.getInstance().addFlag(new Flag<Boolean>(FlagType.bcPipeFlow, false));

        try {
            Writer writer = new FileWriter(path);
            gson.toJson(Wild.getInstance().getFlags(), type, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            MyTown.instance.log.error("An error occurred when trying to save wild perms.");
        }
    }

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
                Wild.getInstance().addFlag(f);
            }
        } else {
            MyTown.instance.log.error("Failed to read wild perms!");
        }
    }
}
