package mytown.config.json;

import com.google.common.reflect.TypeToken;
import mytown.MyTown;
import mytown.entities.Wild;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Wilderness flags
 */
public class WildPermsConfig extends JSONConfig<Flag> {

    public WildPermsConfig(String path) {
        super(path);
        gsonType = new TypeToken<List<Flag>>() {}.getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Flag> create() {
        for (FlagType type : FlagType.values()) {
            if (type.isWildPerm())
                Wild.instance.addFlag(new Flag(type, type.getDefaultWildPerm()));
        }

        try {
            Writer writer = new FileWriter(path);
            gson.toJson(Wild.instance.getFlags(), gsonType, writer);
            writer.close();
            MyTown.instance.LOG.error("Successfully created WildPerms config file!");
        } catch (IOException e) {
            MyTown.instance.LOG.error("Failed to create WildPerms config file!");
            MyTown.instance.LOG.error(ExceptionUtils.getFullStackTrace(e));
        }
        return Wild.instance.getFlags();
    }

    @Override
    public void write(List<Flag> items) {
        try {
            Writer writer = new FileWriter(path);
            gson.toJson(items, gsonType, writer);
            writer.close();

            MyTown.instance.LOG.info("Updated WildPerms file successfully!");
        } catch (IOException e) {
            MyTown.instance.LOG.error("Failed to update WildPerms file!");
            MyTown.instance.LOG.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    @SuppressWarnings("unchecked")
    public List<Flag> read() {
        List<Flag> list = new ArrayList<Flag>();
        try {
            Reader reader = new FileReader(path);
            list = gson.fromJson(reader, gsonType);
            reader.close();
            MyTown.instance.LOG.info("Loaded WildPerms config file successfully!");
        } catch (IOException e) {
            MyTown.instance.LOG.error("Failed to load WildPerms config file!");
            MyTown.instance.LOG.error(ExceptionUtils.getFullStackTrace(e));
        }

        for (Iterator<Flag> it = list.iterator(); it.hasNext(); ) {
            Flag flag = it.next();
            if(flag.getFlagType() == null) {
                MyTown.instance.LOG.error("An unrecognized flagType has been found. Removing...");
                it.remove();
                continue;
            }
            if (!flag.getFlagType().isWildPerm()) {
                MyTown.instance.LOG.error("A non wild flagType has been found in WildPerms config file. Removing...");
                it.remove();
            }
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(List<Flag> items) {
        boolean updated = false;
        for (FlagType type : FlagType.values()) {
            if (type.isWildPerm()) {
                boolean ok = false;
                for (Flag f : items) {
                    if (f.getFlagType() == type)
                        ok = true;
                }
                if (!ok) {
                    MyTown.instance.LOG.error("FlagType " + type.toString() + " for Wild does not exist in the WildPerms file. Adding...");
                    items.add(new Flag(type, type.getDefaultValue()));
                    updated = true;
                }
            }
        }

        for(Flag flag : items) {
            Wild.instance.addFlag(flag);
        }
        if(updated)
            write(items);
    }
}
