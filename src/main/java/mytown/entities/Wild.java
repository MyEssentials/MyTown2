package mytown.entities;

import myessentials.chat.api.ChatManager;
import myessentials.localization.api.LocalManager;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import net.minecraft.util.EnumChatFormatting;
import sun.util.locale.LocaleMatcher;

/**
 * Wilderness permissions
 */
public class Wild {

    public static final Wild instance = new Wild();

    public final Flag.Container flagsContainer = new Flag.Container();

    /**
     * Checks if Resident is allowed to do the action specified by the FlagType in the Wild
     */
    public boolean hasPermission(Resident res, FlagType<Boolean> flagType) {
        if (res == null) {
            return true;
        }

        if (!flagsContainer.getValue(flagType)) {
            ChatManager.send(res.getPlayer(), flagType.getDenialKey());
            ChatManager.send(res.getPlayer(), "mytown.notification.town.owners", LocalManager.get("mytown.notification.town.owners.admins"));
            return false;
        }
        return true;
    }
}
