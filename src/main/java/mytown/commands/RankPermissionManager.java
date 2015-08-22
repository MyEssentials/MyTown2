package mytown.commands;

import myessentials.utils.PlayerUtils;
import mypermissions.api.IPermissionManager;
import mypermissions.api.entities.PermissionLevel;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.util.exceptions.MyTownCommandException;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class RankPermissionManager implements IPermissionManager {

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        if(permission.startsWith("mytown.cmd.outsider") || permission.equals("mytown.cmd"))
            return true;

        EntityPlayer player = PlayerUtils.getPlayerFromUUID(uuid);
        Resident resident = MyTownUniverse.instance.getOrMakeResident(player);
        Town town = Commands.getTownFromResident(resident);
        if(town.residentsMap.get(resident).permissionsContainer.hasPermission(permission) != PermissionLevel.ALLOWED) {
            throw new MyTownCommandException("mytown.cmd.err.rankPerm");
        }
        return true;
    }
}
