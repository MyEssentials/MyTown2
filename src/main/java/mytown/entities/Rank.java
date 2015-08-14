package mytown.entities;

import com.google.common.base.Joiner;
import mypermissions.command.CommandManager;
import mypermissions.command.CommandTreeNode;

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
     * @param children
     * @param pMayor
     * @param pAssistant
     * @param pResident
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
    private List<String> permissions;
    private Town town;

    public Rank(String name, Town town) {
        this(name, new ArrayList<String>(), town);
    }

    public Rank(String name, List<String> permissions, Town town) {
        this.name = name;
        this.town = town;
        this.permissions = permissions;
        updateKey();
    }

    public String getName() {
        return name;
    }

    public boolean addPermission(String permission) {
        return permissions.add(permission);
    }

    public void addPermissions(Collection<String> permissions) {
        this.permissions.addAll(permissions);
    }

    public boolean removePermission(String permission) {
        return permissions.remove(permission);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermissionOrSuperPermission(String permission) {
        if (hasPermission(permission))
            return true;
        for (String p : permissions) {
            if (permission.contains(p)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public String getPermissionsString() {
        return Joiner.on(", ").join(getPermissions());
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
        return String.format("Rank: {Name: %s, Town: %s, Permissions: [%s]}", getName(), getTown().getName(), Joiner.on(", ").join(getPermissions()));
    }
}
