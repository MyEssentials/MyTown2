package mytown.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import mytown.entities.flag.FlagType;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 10/4/2014.
 * Default flags on town creation config file.
 */
public class FlagsConfig {

    private Type type = new TypeToken<List<Wrapper>>() {} .getType();
    private String path;
    private Gson gson;

    public FlagsConfig(File file) {
        path = file.getAbsolutePath();
        gson = new GsonBuilder().setPrettyPrinting().create();

        if(!file.exists() || file.isDirectory())
            writeFile();
        else
            readFile();
    }

    private void writeFile() {
        List<Wrapper> wrappers = new ArrayList<Wrapper>();

        for(FlagType type : FlagType.values()) {
            wrappers.add(new Wrapper(type, type.getDefaultValue(), type.isUsableForTowns()));
        }
        try {
            Writer writer = new FileWriter(path);
            gson.toJson(wrappers, type, writer);
            writer.close();
            MyTown.instance.log.info("Loaded flags successfully!");
        } catch (IOException ex) {
            ex.printStackTrace();
            MyTown.instance.log.error("Failed to write to json flag config.");
        }
    }

    @SuppressWarnings({"unchecked"})
    private void readFile() {
        List<Wrapper> wrappers;

        try {
            Reader reader = new FileReader(path);
            wrappers = gson.fromJson(reader, type);
            reader.close();

            // Checking
            boolean ok;
            for(Wrapper w : wrappers) {
                ok = false;
                for(FlagType type : FlagType.values()) {
                    if(type == w.flagType)
                        ok = true;
                }
                if(!ok) {
                    MyTown.instance.log.error("Flag config is missing a flagType! Make sure you add them all. If you have trouble you can delete the file to get a new one.");
                    return;
                }
            }

            for(Wrapper w : wrappers) {
                if(w.flagType.getType().isAssignableFrom(w.defaultState.getClass())) {
                    w.flagType.setDefaultValue(w.defaultState);
                    w.flagType.setIsUsableForTowns(w.isAllowedInTowns);
                    //MyTown.instance.log.info("Added flag: " + w.flagType.toString() + " with default " + w.defaultState.toString() + " and allowed in towns: " + w.isAllowedInTowns );
                } else {
                    MyTown.instance.log.error("The default value provided is not the proper one (for flag " + w.flagType.toString() + ")!");
                }
            }
            //MyTown.instance.log.info("Loaded flags successfully!");
            writeFile();
        } catch (IOException ex) {
            ex.printStackTrace();
            MyTown.instance.log.error("Failed to read from json flag config.");
        }
    }

    private class Wrapper {
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
