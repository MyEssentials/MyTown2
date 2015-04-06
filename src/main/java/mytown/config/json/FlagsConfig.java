package mytown.config.json;

import com.google.common.reflect.TypeToken;
import mytown.MyTown;
import mytown.entities.flag.FlagType;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by AfterWind on 10/4/2014.
 * Default flags on town creation config file.
 */
public class FlagsConfig extends JSONConfig<FlagsConfig.Wrapper> {
    public FlagsConfig(String path) {
        super(path);
        gsonType = new TypeToken<List<Wrapper>>() {}.getType();
    }

    @Override
    protected List<Wrapper> create() {
        List<Wrapper> wrappers = new ArrayList<Wrapper>();

        for (FlagType type : FlagType.values()) {
            wrappers.add(new Wrapper(type, type.getDefaultValue(), type.canTownsModify()));
        }
        try {
            Writer writer = new FileWriter(path);
            gson.toJson(wrappers, gsonType, writer);
            writer.close();
            MyTown.instance.log.info("Created new DefaultFlags file successfully!");
        } catch (IOException ex) {
            ex.printStackTrace();
            MyTown.instance.log.error("Failed to create DefaultFlags file!");
        }
        return wrappers;
    }

    @Override
    public void write(List<Wrapper> items) {
        try {
            Writer writer = new FileWriter(path);
            gson.toJson(items, gsonType, writer);
            writer.close();
            MyTown.instance.log.info("Updated the DefaultFlags file successfully!");
        } catch (IOException ex) {
            ex.printStackTrace();
            MyTown.instance.log.error("Failed to update DefaultFlags file!");
        }
    }

    @Override
    public List<Wrapper> read() {
        List<Wrapper> wrappers = new ArrayList<Wrapper>();

        try {
            Reader reader = new FileReader(path);
            wrappers = gson.fromJson(reader, gsonType);
            reader.close();

            for(Iterator<Wrapper> it = wrappers.iterator(); it.hasNext();) {
                Wrapper w = it.next();
                if(w.flagType == null) {
                    MyTown.instance.log.error("Found a type of flag that does not exist. Removing...");
                    it.remove();
                    continue;
                }
                if (!w.flagType.getType().isAssignableFrom(w.defaultState.getClass())) {
                    MyTown.instance.log.error("The default value for the flag is of invalid type for flag " + w.flagType.toString() + "! Needed " + w.flagType.getType().getSimpleName() + " Removing...");
                    it.remove();
                }
            }
            MyTown.instance.log.info("Loaded DefaultFlags successfully!");
        } catch (IOException ex) {
            ex.printStackTrace();
            MyTown.instance.log.error("Failed to read from DefaultFlags file!");
        }
        return wrappers;
    }

    @Override
    public void update(List<Wrapper> items) {
        boolean ok, updated = false;
        for(Wrapper wrapper : items) {
            wrapper.flagType.setDefaultValue(wrapper.defaultState);
            wrapper.flagType.setModifiableForTowns(wrapper.isAllowedInTowns);
        }

        for(FlagType type : FlagType.values()) {
            ok = false;
            for(Wrapper w : items) {
                if(w.flagType == type)
                    ok = true;
            }
            if(!ok) {
                items.add(new Wrapper(type, type.getDefaultValue(), type.canTownsModify()));
                MyTown.instance.log.error("Flag config is missing (or is an invalid entry) " + type.toString() + " flag! Adding with default settings...");
                updated = true;
            }
        }

        // Write the updated list to the file
        if(updated)
            write(items);
    }

    /**
     * Wraps around a flagType object.
     */
    public class Wrapper {
        public FlagType flagType;
        public Object defaultState;
        public boolean isAllowedInTowns;

        public Wrapper(FlagType flagType, Object defaultState, boolean isAllowedInTowns) {
            this.flagType = flagType;
            this.defaultState = defaultState;
            this.isAllowedInTowns = isAllowedInTowns;
        }
    }
}
