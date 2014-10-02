package mytown.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import mytown.core.utils.command.CommandManager;
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
 *
 */
public class RanksConfig {
    private Gson gson;
    private String path;

    public RanksConfig(File file) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        this.path = file.getPath();

        if(!file.exists() || file.isDirectory()) {
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
            String outsiderRank = "Outsider";
            List<String> pMayor = new ArrayList<String>();
            List<String> pAssistant = new ArrayList<String>();
            List<String> pResident = new ArrayList<String>();
            List<String> pOutsider = new ArrayList<String>();

            // Filling arrays

            for(String s : CommandManager.commandList.keySet()) {
                if (s.startsWith("mytown.cmd")) {
                    pMayor.add(s);
                    if (s.startsWith("mytown.cmd.assistant") || s.startsWith("mytown.cmd.everyone") || s.startsWith("mytown.cmd.outsider")) {
                        pAssistant.add(s);
                    }
                    if (s.startsWith("mytown.cmd.everyone") || s.startsWith("mytown.cmd.outsider")) {
                        pResident.add(s);
                    }
                    if(s.startsWith("mytown.cmd.outsider")) {
                        pOutsider.add(s);
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

            Rank.theOutsiderPerms = pOutsider;


            // Preparing to add them to JSON file

            Wrapper[] wrappedObjects = new Wrapper[4];
            wrappedObjects[0] = new Wrapper(mayorRank, pMayor, RankType.DefaultMayor);
            wrappedObjects[1] = new Wrapper(assistantRank, pAssistant, RankType.Other);
            wrappedObjects[2] = new Wrapper(residentRank, pResident, RankType.Default);
            wrappedObjects[3] = new Wrapper(outsiderRank, pOutsider, RankType.Outsider);

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
                    if(!CommandManager.commandList.containsKey(s))
                        throw new RuntimeException("Permission node " + s + " does not exist!");
                }
                if(w.type != RankType.Outsider)
                    Rank.defaultRanks.put(w.name, w.permissions);
                if(w.type == RankType.Default)
                    Rank.theDefaultRank = w.name;
                if(w.type == RankType.DefaultMayor)
                    Rank.theMayorDefaultRank = w.name;
                if(w.type == RankType.Outsider)
                    Rank.theOutsiderPerms = w.permissions;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private enum RankType {
        Default,
        DefaultMayor,
        Outsider,
        Other
    }

    private class Wrapper {
        public String name;
        public RankType type;
        public List<String> permissions;

        public Wrapper(String name, List<String> permissions, RankType type) {
            this.name = name;
            this.permissions = permissions;
            this.type = type;
        }
    }




}
