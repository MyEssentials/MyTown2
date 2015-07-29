package mytown.config.json;

import com.google.common.reflect.TypeToken;
import myessentials.command.CommandManagerNew;
import myessentials.command.CommandTreeNode;
import myessentials.json.JSONConfig;
import mytown.MyTown;
import mytown.entities.Rank;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.util.*;

/**
 * JSON Default ranks config
 */
public class RanksConfig extends JSONConfig<RanksConfig.Wrapper> {

    public RanksConfig(String path) {
        super(path);
        gsonType = new TypeToken<List<Wrapper>>() {}.getType();
    }

    @Override
    protected List<Wrapper> create() {
        List<Wrapper> wrappers = new ArrayList<Wrapper>();
        try {
            Writer writer = new FileWriter(path);

            String mayorRank = "Mayor";
            String assistantRank = "Assistant";
            String residentRank = "Resident";
            List<String> pMayor = new ArrayList<String>();
            List<String> pAssistant = new ArrayList<String>();
            List<String> pResident = new ArrayList<String>();

            // Filling arrays
            for(CommandTreeNode node : CommandManagerNew.getTree("mytown.cmd").getRoot().getChildren()) {
                String s = node.getAnnotation().permission();
                pMayor.add(s);
                if (s.startsWith("mytown.cmd.assistant") || s.startsWith("mytown.cmd.everyone") || s.startsWith("mytown.cmd.outsider")) {
                    pAssistant.add(s);
                }
                if (s.startsWith("mytown.cmd.everyone") || s.startsWith("mytown.cmd.outsider")) {
                    pResident.add(s);
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
            MyTown.instance.LOG.info("Added mayor rank.");
            Rank.theMayorDefaultRank = mayorRank;


            // Preparing to add them to JSON file

            wrappers.add(new Wrapper(mayorRank, pMayor, RankType.MAYOR));
            wrappers.add(new Wrapper(assistantRank, pAssistant, RankType.OTHER));
            wrappers.add(new Wrapper(residentRank, pResident, RankType.RESIDENT));

            // Adding to JSON file
            gson.toJson(wrappers, gsonType, writer);

            writer.close();
            MyTown.instance.LOG.info("Created new DefaultRanks file successfully!");
        } catch (IOException e) {
            MyTown.instance.LOG.error("Failed to create DefaultRanks file!");
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(e));
        }
        return wrappers;
    }

    @Override
    public void write(List<Wrapper> items) {
        try {
            Writer writer = new FileWriter(path);
            gson.toJson(items, gsonType, writer);
            writer.close();

            MyTown.instance.LOG.info("Updated DefaultRanks file successfully!");
        } catch (IOException e) {
            MyTown.instance.LOG.error("Failed to update DefaultRanks file!");
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public List<Wrapper> read() {
        List<Wrapper> wrappedObjects = new ArrayList<Wrapper>();
        try {
            Reader reader = new FileReader(path);

            // Just for showing the nodes that were omitted.
            List<String> notExistingPermNodes = new ArrayList<String>();
            wrappedObjects = gson.fromJson(reader, gsonType);

            for (Wrapper w : wrappedObjects) {
                for (Iterator<String> it = w.permissions.iterator(); it.hasNext(); ) {
                    String s = it.next();
                    if (!CommandManagerNew.getTree("mytown.cmd").hasCommandNode(CommandManagerNew.getTree("mytown.cmd").getRoot(), s) || s.startsWith("-") && !CommandManagerNew.getTree("mytown.cmd").hasCommandNode(s.substring(1))) {
                        // Omitting permissions that don't exist
                        if(!notExistingPermNodes.contains(s)) {
                            notExistingPermNodes.add(s);
                            MyTown.instance.LOG.error("Permission node {} does not exist! Removing...", s);
                        }

                        it.remove();
                    }

                }
            }
            MyTown.instance.LOG.info("Loaded DefaultRanks successfully!");
        } catch (Exception e) {
            MyTown.instance.LOG.info("Failed to read from DefaultRanks file!");
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(e));
        }
        return wrappedObjects;
    }

    @Override
    public void update(List<Wrapper> items) {
        boolean updated = false;
        for(CommandTreeNode node : CommandManagerNew.getTree("mytown.cmd").getRoot().getChildren()) {
            String perm = node.getAnnotation().permission();
            if(perm.startsWith("mytown.cmd")) {
                for (Wrapper wrapper : items) {
                    if(wrapper.type == RankType.MAYOR ||
                            wrapper.type == RankType.RESIDENT && (perm.startsWith("mytown.cmd.everyone") || perm.startsWith("mytown.cmd.outsider")) ||
                            wrapper.type == RankType.ASSISTANT && (perm.startsWith("mytown.cmd.assistant") || perm.startsWith("mytown.cmd.everyone") || perm.startsWith("mytown.cmd.outsider")))
                        if (!wrapper.permissions.contains(perm) && !wrapper.permissions.contains("-" + perm)) {
                            wrapper.permissions.add(perm);
                            MyTown.instance.LOG.info("Permission node {} is missing from the configs in rank {}", perm, wrapper.type);
                            updated = true;
                        }
                }
            }
        }

        boolean mayorExists = false, residentExists = false, assistantExists = false;
        for(Wrapper wrapper : items) {
            Collections.sort(wrapper.permissions);
            if(wrapper.type != null) {
                switch (wrapper.type) {
                    case MAYOR:
                        mayorExists = true;
                        break;
                    case RESIDENT:
                        residentExists = true;
                        break;
                    case ASSISTANT:
                        assistantExists = true;
                        break;
                }
            }
        }

        if(!mayorExists) {
            MyTown.instance.LOG.info("Ranks config is missing Mayor rank. Adding...");
            List<String> permissions = new ArrayList<String>();
            for(CommandTreeNode node : CommandManagerNew.getTree("mytown.cmd").getRoot().getChildren()) {
                permissions.add(node.getAnnotation().permission());
            }
            Collections.sort(permissions);
            items.add(new Wrapper("Mayor", permissions, RankType.MAYOR));
            updated = true;
        }

        if(!assistantExists) {
            MyTown.instance.LOG.info("Ranks config is missing Assistant rank. Adding...");
            List<String> permissions = new ArrayList<String>();
            for(CommandTreeNode node : CommandManagerNew.getTree("mytown.cmd").getRoot().getChildren()) {
                String perm = node.getAnnotation().permission();
                if(perm.startsWith("mytown.cmd.assistant") || perm.startsWith("mytown.cmd.everyone") || perm.startsWith("mytown.cmd.outsider")) {
                    permissions.add(perm);
                }
            }
            Collections.sort(permissions);
            items.add(new Wrapper("Assistant", permissions, RankType.ASSISTANT));
            updated = true;
        }

        if(!residentExists) {
            MyTown.instance.LOG.info("Ranks config is missing Resident rank. Adding...");
            List<String> permissions = new ArrayList<String>();
            for(CommandTreeNode node : CommandManagerNew.getTree("mytown.cmd").getRoot().getChildren()) {
                String perm = node.getAnnotation().permission();
                if(perm.startsWith("mytown.cmd.everyone") || perm.startsWith("mytown.cmd.outsider")) {
                    permissions.add(perm);
                }
            }
            Collections.sort(permissions);
            items.add(new Wrapper("Resident", permissions, RankType.RESIDENT));
            updated = true;
        }

        for(Wrapper wrapper : items) {
            List<String> newPerms = new ArrayList<String>();
            for(String node : wrapper.permissions) {
                if(!node.startsWith("-"))
                    newPerms.add(node);
            }

            Rank.defaultRanks.put(wrapper.name, newPerms);
            if (wrapper.type == RankType.RESIDENT)
                Rank.theDefaultRank = wrapper.name;
            if (wrapper.type == RankType.MAYOR)
                Rank.theMayorDefaultRank = wrapper.name;
        }

        if(updated)
            write(items);
    }

    private enum RankType {
        RESIDENT,
        MAYOR,
        ASSISTANT,
        OTHER
    }

    /**
     * Wraps around a set of fields needed to instantiate Rank objects.
     */
    public class Wrapper {
        public final String name;
        public final RankType type;
        public final List<String> permissions;

        public Wrapper(String name, List<String> permissions, RankType type) {
            this.name = name;
            this.permissions = permissions;
            this.type = type;
        }
    }


}
