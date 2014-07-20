package mytown.entities;

import com.google.common.base.Joiner;

import java.util.Collection;
import java.util.List;

/**
 * @author Joe Goett
 */
public class Rank {
    private String key, name;
    private List<String> permissions;
    private Town town;

    /* TODO Constructors...
    public Rank(String name, Town town, List<String> permissions) {
        this.name = name;
        this.town = town;
        this.permissions = permissions;
        updateKey();
    }
    */

    public String getName() {
        return name;
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void addPermissions(Collection<String> permissions) {
        this.permissions.addAll(permissions);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public Collection<String> getPermissions() {
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
