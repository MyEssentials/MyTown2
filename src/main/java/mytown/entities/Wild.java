package mytown.entities;

import mytown.api.container.FlagsContainer;
import mytown.entities.flag.FlagType;
import net.minecraft.util.EnumChatFormatting;

/**
 * Wilderness permissions
 */
public class Wild {

    public static final Wild instance = new Wild();

    public final FlagsContainer flagsContainer = new FlagsContainer();

    /**
     * Checks if Resident is allowed to do the action specified by the FlagType in the Wild
     */
    public boolean hasPermission(Resident res, FlagType<Boolean> flagType) {
        if (flagsContainer.getValue(flagType)) {
            res.protectionDenial(flagType, EnumChatFormatting.RED + "SERVER OWNERS");
            return false;
        }
        return true;
    }
}
