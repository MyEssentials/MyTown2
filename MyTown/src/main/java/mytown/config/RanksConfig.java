package mytown.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.Constants;
import mytown.core.utils.command.CommandUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by AfterWind on 7/4/2014
 * JSON Default ranks config
 *
 * TODO: STOP USING THE DAMN BOOLEANS FOR EVERYTHING AND USE PROPER JSON
 */
public class RanksConfig {
    private Gson gson;
    private String path;

    public RanksConfig(String path) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        this.path = path;

        File file = new File(path);
        if(!file.exists()) {
            writeFile();
        } else {
            readFile();
        }


    }
    private void writeFile() {
        try {
            Writer writer = new FileWriter(path);

            String mayorRank = "Mayor";
            String assistantRank = "Assistant";
            String residentRank = "Resident";
            List<String> pMayor = new ArrayList<String>();
            List<String> pAssistant = new ArrayList<String>();
            List<String> pResident = new ArrayList<String>();

            // Filling arrays

            for(String s : CommandUtils.permissionList.values()) {
                if (s.startsWith("mytown.cmd")) {
                    pMayor.add(s);
                    if (s.startsWith("mytown.cmd.assistant") || s.startsWith("mytown.cmd.resident")) {
                        pAssistant.add(s);
                    }
                    if (s.startsWith("mytown.cmd.resident")) {
                        pResident.add(s);
                    }
                }
            }

            // Sorting

            Collections.sort(pMayor);
            Collections.sort(pAssistant);
            Collections.sort(pResident);

            // Adding them to the defaults

            Constants.DEFAULT_RANK_VALUES.put(mayorRank, pMayor);
            Constants.DEFAULT_RANK_VALUES.put(assistantRank, pAssistant);
            Constants.DEFAULT_RANK_VALUES.put(residentRank, pResident);

            Constants.DEFAULT_RANK = residentRank;
            Constants.DEFAULT_SUPER_RANK = mayorRank;

            // Preparing to add them to JSON file

            Wrapper[] wrappedObjects = new Wrapper[3];
            wrappedObjects[0] = new Wrapper(mayorRank, pMayor, false, true);
            wrappedObjects[1] = new Wrapper(assistantRank, pAssistant, false, false);
            wrappedObjects[2] = new Wrapper(residentRank, pResident, true, false);

            // Adding to JSON file

            gson.toJson(wrappedObjects, Wrapper[].class, writer);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readFile() {
        try {
            Reader reader = new FileReader(path);

            Wrapper[] wrappedObjects = gson.fromJson(reader, Wrapper[].class);

            for(Wrapper w : wrappedObjects) {
                for(String s : w.permissions) {
                    if(!CommandUtils.permissionList.containsValue(s))
                        throw new RuntimeException("Permission node " + s + " does not exist!");
                }

                Constants.DEFAULT_RANK_VALUES.put(w.name, w.permissions);
                if(w.isDefault)
                    Constants.DEFAULT_RANK = w.name;
                if(w.isSuperRank)
                    Constants.DEFAULT_SUPER_RANK = w.name;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private class Wrapper {
        public String name;
        public boolean isDefault;
        public boolean isSuperRank;
        public List<String> permissions;

        public Wrapper(String name, List<String> permissions, boolean isDefault, boolean isSuperRank) {
            this.name = name;
            this.permissions = permissions;
            this.isDefault = isDefault;
            this.isSuperRank = isSuperRank;
        }
    }




}
