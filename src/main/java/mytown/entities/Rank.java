package mytown.entities;

import mypermissions.api.command.CommandManager;
import mypermissions.command.CommandTreeNode;
import mytown.api.container.PermissionsContainer;
import myessentials.utils.ColorUtils;

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

        // Filling lists
        fillLists(CommandManager.getTree("mytown.cmd").getRoot().getChildren(), pMayor, pAssistant, pResident);

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

    /**
     * Fills the given permission lists, looping through children of children, etc
     */
    private static void fillLists(List<CommandTreeNode> children, List<String> pMayor, List<String> pAssistant, List<String> pResident) {
        if (children == null || children.size() <= 0) return;
        for(CommandTreeNode node : children) {
            String s = node.getAnnotation().permission();
            pMayor.add(s);
            if (s.startsWith("mytown.cmd.assistant") || s.startsWith("mytown.cmd.everyone") || s.startsWith("mytown.cmd.outsider")) {
                pAssistant.add(s);
            }
            if (s.startsWith("mytown.cmd.everyone") || s.startsWith("mytown.cmd.outsider")) {
                pResident.add(s);
            }
            // Loop through its children
            fillLists(node.getChildren(), pMayor, pAssistant, pResident);
        }
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
        String color;
        if (Rank.theMayorDefaultRank.equals(getName())) {
            color = ColorUtils.colorRankMayor;
        } else if (Rank.theDefaultRank.equals(getName())) {
            color = ColorUtils.colorRankDefault;
        } else {
            color = ColorUtils.colorRankOther;
        }
        return color + getName();
    }
}
