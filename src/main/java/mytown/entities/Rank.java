package mytown.entities;

import com.google.common.base.Joiner;
import mypermissions.command.CommandManager;
import mypermissions.command.CommandTreeNode;
import mytown.api.container.PermissionsContainer;

import java.util.*;

public class Rank {

    /**
     * Map that holds the name and the rank's permission of all the ranks that are added to a town on creation.
     * And can be configured in the config file.
     */
    public static final Map<String, List<String>> defaultRanks = new HashMap<String, List<String>>();
    public static String theDefaultRank;
    public static String theMayorDefaultRank; // ok not the best name

    public static void initDefaultRanks() {
        String mayorRank = "Mayor";
        String assistantRank = "Assistant";
        String residentRank = "Resident";
        List<String> pMayor = new ArrayList<String>();
        List<String> pAssistant = new ArrayList<String>();
        List<String> pResident = new ArrayList<String>();

        // Filling arrays
        for(CommandTreeNode node : CommandManager.getTree("mytown.cmd").getRoot().getChildren()) {
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
        Rank.theMayorDefaultRank = mayorRank;
    }

    private String key, name;
    private Town town;

    public final PermissionsContainer permissionsContainer = new PermissionsContainer();

    public Rank(String name, Town town) {
        this.name = name;
        this.town = town;
        updateKey();
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    private void updateKey() {
        key = String.format("%s;%s", town.getName(), name);
    }

    public Town getTown() {
        return town;
    }

    @Override
    public String toString() {
        return String.format("Rank: {Name: %s, Town: %s, Permissions: [%s]}", getName(), getTown().getName(), Joiner.on(", ").join(permissionsContainer.asList()));
    }
}
