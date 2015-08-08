package mytown.api.container;

import myessentials.entities.Container;

public class PermissionsContainer extends Container<String> {

    public boolean hasPermissionOrSuperPermission(String permission) {
        if (contains(permission))
            return true;
        for (String p : items) {
            if (permission.contains(p)) {
                return true;
            }
        }
        return false;
    }
}
