package mytown.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import mytown.core.utils.command.CommandManager;
import mytown.entities.Rank;
import mytown.entities.flag.Flag;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by AfterWind on 7/4/2014
 * JSON Default ranks config
 */
public class RanksConfig {

    private Type type = new TypeToken<List<Wrapper>>() {}.getType();
    private Gson gson;
    private String path;

    public RanksConfig(File file) {
        gson = new GsonBuilder().setPrettyPrinting().create();
        this.path = file.getPath();

        if (!file.exists() || file.isDirectory()) {
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

            for (String s : CommandManager.commandList.keySet()) {
                if (s.startsWith("mytown.cmd")) {
                    pMayor.add(s);
                    if (s.startsWith("mytown.cmd.assistant") || s.startsWith("mytown.cmd.everyone") || s.startsWith("mytown.cmd.outsider")) {
                        pAssistant.add(s);
                    }
                    if (s.startsWith("mytown.cmd.everyone") || s.startsWith("mytown.cmd.outsider")) {
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
            MyTown.instance.log.info("Added mayor rank.");
            Rank.theMayorDefaultRank = mayorRank;


            // Preparing to add them to JSON file

            Wrapper[] wrappedObjects = new Wrapper[3];
            wrappedObjects[0] = new Wrapper(mayorRank, pMayor, RankType.DefaultMayor);
            wrappedObjects[1] = new Wrapper(assistantRank, pAssistant, RankType.Other);
            wrappedObjects[2] = new Wrapper(residentRank, pResident, RankType.Default);

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

            // Just for showing the nodes that were omitted.
            List<String> notExistingPermNodes = new ArrayList<String>();
            List<Wrapper> wrappedObjects = gson.fromJson(reader, type);

            for (Wrapper w : wrappedObjects) {
                for (Iterator<String> it = w.permissions.iterator(); it.hasNext(); ) {
                    String s = it.next();
                    if (!CommandManager.commandList.containsKey(s)) {
                        // Omitting permissions that don't exist
                        boolean ok = true;
                        for(String s1 : notExistingPermNodes)
                            if(s1.equals(s))
                                ok = false;
                        if(ok)
                            notExistingPermNodes.add(s);
                        it.remove();
                    }
                }

                Rank.defaultRanks.put(w.name, w.permissions);
                if (w.type == RankType.Default)
                    Rank.theDefaultRank = w.name;
                if (w.type == RankType.DefaultMayor)
                    Rank.theMayorDefaultRank = w.name;
            }
            for(String s : notExistingPermNodes) {
                MyTown.instance.log.error("Permission node " + s + " does not exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private enum RankType {
        Default,
        DefaultMayor,
        Other
    }

    /**
     * Wraps around a set of fields needed to instantiate Rank objects.
     */
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
