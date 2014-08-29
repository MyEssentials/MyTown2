package mytown.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.core.utils.x_command.CommandUtils;
import mytown.entities.Rank;

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

    public RanksConfig(File file) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        this.path = file.getPath();

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

            Rank.defaultRanks.put(mayorRank, pMayor);
            Rank.defaultRanks.put(assistantRank, pAssistant);
            Rank.defaultRanks.put(residentRank, pResident);


            Rank.theDefaultRank = residentRank;
            Rank.theMayorDefaultRank = mayorRank;


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

                Rank.defaultRanks.put(w.name, w.permissions);
                if(w.isDefault)
                    Rank.theDefaultRank = w.name;
                if(w.isSuperRank)
                    Rank.theMayorDefaultRank = w.name;
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
