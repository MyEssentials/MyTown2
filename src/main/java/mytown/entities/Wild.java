package mytown.entities;

import myessentials.utils.PlayerUtils;
import mytown.api.container.FlagsContainer;
import mytown.entities.flag.FlagType;

/**
 * Wilderness permissions
 */
public class Wild {

    public static final Wild instance = new Wild();

    public final FlagsContainer flagsContainer = new FlagsContainer();

    /**
     * Checks if Resident is allowed to do the action specified by the FlagType in the Wild
     */
    public boolean hasPermission(Resident res, FlagType type, Object denialValue) {
        if (flagsContainer.getValue(type).equals(denialValue)) {
            return PlayerUtils.isOp(res.getPlayer());
        }
        return true;
    }
}
