package mytown.api.container;

import java.util.ArrayList;

public class PermissionsContainer extends ArrayList<String> {

    public boolean hasPermissionOrSuperPermission(String permission) {
        if (contains(permission))
            return true;
        for (String p : this) {
            if (permission.contains(p)) {
                return true;
            }
        }
        return false;
    }
}
