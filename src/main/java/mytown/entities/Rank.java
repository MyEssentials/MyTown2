package mytown.entities;

import com.google.common.base.Joiner;
import mytown.MyTown;

import java.util.*;

/**
 * @author Joe Goett
 */
public class Rank {
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
                //MyTown.instance.log.info("Rank " + getName() + " doesn't contain " + permission + " but contains permission " + p);
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

    /**
     * Map that holds the name and the rank's permission of all the ranks that are added to a town on creation.
     * And can be configured in the config file.
     */
    public static Map<String, List<String>> defaultRanks = new HashMap<String, List<String>>();
    public static String theDefaultRank;
    public static String theMayorDefaultRank; // ok not the best name
}
