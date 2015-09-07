package mytown.entities;

import myessentials.teleport.Teleport;
import myessentials.utils.PlayerUtils;
import mypermissions.api.entities.PermissionLevel;
import mypermissions.proxies.PermissionProxy;
import mytown.MyTown;
import mytown.api.container.*;
import mytown.config.Config;
import mytown.entities.flag.FlagType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a Town. A Town is made up of Residents, Ranks, Blocks, and Plots.
 */
public class Town implements Comparable<Town> {
    private String name, oldName = null;

    protected int extraBlocks = 0;
    protected int maxFarClaims = Config.maxFarClaims;

    private Nation nation;
    private Teleport spawn;

    public final ResidentRankMap residentsMap = new ResidentRankMap();
    public final RanksContainer ranksContainer = new RanksContainer();
    public final PlotsContainer plotsContainer = new PlotsContainer(Config.defaultMaxPlots);
    public final FlagsContainer flagsContainer = new FlagsContainer();
    public final TownBlocksContainer townBlocksContainer = new TownBlocksContainer();
    public final BlockWhitelistsContainer blockWhitelistsContainer = new BlockWhitelistsContainer();

    public final Bank bank = new Bank(this);

    public Town(String name) {
        this.name = name;
    }

    public void notifyResidentJoin(Resident res) {
        for (Resident toRes : residentsMap.keySet()) {
            toRes.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.joined", res.getPlayerName(), getName()));
        }
    }

    /**
     * Notifies every resident in this town sending a message.
     */
    public void notifyEveryone(String message) {
        for (Resident r : residentsMap.keySet()) {
            r.sendMessage(message);
        }
    }

    /**
     * Checks if the Resident is allowed to do the action specified by the FlagType at the coordinates given.
     * This method will go through all the plots and prioritize the plot's flags over town flags.
     */
    @SuppressWarnings("unchecked")
    public boolean hasPermission(Resident res, FlagType flagType, Object denialValue, int dim, int x, int y, int z) {
        Plot plot = plotsContainer.get(dim, x, y, z);

        if (plot == null) {
            return hasPermission(res, flagType, denialValue);
        } else {
        	return plot.hasPermission(res, flagType, denialValue);
        }
    }

    /**
     * Checks if the Resident is allowed to do the action specified by the FlagType in this town.
     */
    public boolean hasPermission(Resident res, FlagType flagType, Object denialValue) {
        if(PlayerUtils.isOp(res.getUUID())) {
            return true;
        }

        if(!flagsContainer.getValue(flagType).equals(denialValue)) {
            return true;
        }

        boolean rankBypass;
        boolean permissionBypass;

        if(residentsMap.containsKey(res)) {
            if((Boolean) flagsContainer.getValue(FlagType.RESTRICTIONS)) {
                rankBypass = hasPermission(res, FlagType.RESTRICTIONS.getBypassPermission());
                permissionBypass = PermissionProxy.getPermissionManager().hasPermission(res.getUUID(), FlagType.RESTRICTIONS.getBypassPermission());

                if(!rankBypass && !permissionBypass) {
                    return false;
                }
            }

            rankBypass = hasPermission(res, flagType.getBypassPermission());
            permissionBypass = PermissionProxy.getPermissionManager().hasPermission(res.getUUID(), flagType.getBypassPermission());

            if(!rankBypass && !permissionBypass) {
                return false;
            }

        } else {
            permissionBypass = PermissionProxy.getPermissionManager().hasPermission(res.getUUID(), flagType.getBypassPermission());

            if(!permissionBypass) {
                return false;
            }
        }

        return true;
    }

    /**
     * Permission node check for Residents
     */
    public boolean hasPermission(Resident res, String permission) {
        if(!residentsMap.containsKey(res)) {
            return false;
        }

        Rank rank = residentsMap.get(res);
        return rank.permissionsContainer.hasPermission(permission) == PermissionLevel.ALLOWED;
    }

    public Object getValueAtCoords(int dim, int x, int y, int z, FlagType flagType) {
        Plot plot = plotsContainer.get(dim, x, y, z);
        if(plot == null || flagType.isTownOnly()) {
            return flagsContainer.getValue(flagType);
        } else {
            return plot.flagsContainer.getValue(flagType);
        }
    }

    /**
     * Used to get the owners of a plot (or a town) at the position given
     * Returns null if position is not in town
     */
    public List<Resident> getOwnersAtPosition(int dim, int x, int y, int z) {
        List<Resident> list = new ArrayList<Resident>();
        Plot plot = plotsContainer.get(dim, x, y, z);
        if (plot == null) {
            if (isPointInTown(dim, x, z) && !(this instanceof AdminTown) && !residentsMap.isEmpty()) {
            	Resident mayor = residentsMap.getMayor();
                if (mayor != null) {
                	list.add(mayor);
                }
            }
        } else {
            for (Resident res : plot.ownersContainer) {
                list.add(res);
            }
        }
        return list;
    }

    public void sendToSpawn(Resident res) {
        EntityPlayer pl = res.getPlayer();
        if (pl != null) {
            PlayerUtils.teleport((EntityPlayerMP)pl, spawn.getDim(), spawn.getX(), spawn.getY(), spawn.getZ());
            res.setTeleportCooldown(Config.teleportCooldown);
        }
    }

    public int getMaxBlocks() {
        int mayorBlocks = Config.blocksMayor;
        int residentsBlocks = Config.blocksResident * (residentsMap.size() - 1);
        int residentsExtra = 0;
        for(Resident res : residentsMap.keySet()) {
            residentsExtra += res.getExtraBlocks();
        }
        int townExtra = townBlocksContainer.getExtraBlocks();

        return mayorBlocks + residentsBlocks + residentsExtra + townExtra;
    }

    public int getExtraBlocks() {
        int residentsExtra = 0;
        for(Resident res : residentsMap.keySet()) {
            residentsExtra += res.getExtraBlocks();
        }
        return residentsExtra + townBlocksContainer.getExtraBlocks();
    }

    public String formatOwners(int dim, int x, int y, int z) {
        List<Resident> residents = getOwnersAtPosition(dim, x, y, z);
        String formattedList = "";

        for (Resident r : residents) {
            if (formattedList.equals("")) {
                formattedList = r.getPlayerName();
            } else {
                formattedList += ", " + r.getPlayerName();
            }
        }

        if(formattedList.equals("")) {
            formattedList = EnumChatFormatting.RED + "SERVER ADMINS";
        }

        return formattedList;
    }

    /* ----- Comparable ----- */

    @Override
    public int compareTo(Town t) { // TODO Flesh this out more for ranking towns?
        int thisNumberOfResidents = residentsMap.size(),
                thatNumberOfResidents = t.residentsMap.size();
        if (thisNumberOfResidents > thatNumberOfResidents)
            return -1;
        else if (thisNumberOfResidents == thatNumberOfResidents)
            return 0;
        else if (thisNumberOfResidents < thatNumberOfResidents)
            return 1;

        return -1;
    }

    public String getName() {
        return name;
    }

    public String getOldName() {
        return oldName;
    }

    /**
     * Renames this current Town setting oldName to the previous name. You MUST set oldName to null after saving it in the Datasource
     */
    public void rename(String newName) {
        oldName = name;
        name = newName;
    }

    /**
     * Resets the oldName to null. You MUST call this after a name change in the Datasource!
     */
    public void resetOldName() {
        oldName = null;
    }

    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation nation) {
        this.nation = nation;
    }

    public boolean hasSpawn() {
        return spawn != null;
    }

    public Teleport getSpawn() {
        return spawn;
    }

    public void setSpawn(Teleport spawn) {
        this.spawn = spawn;
    }

    /**
     * Checks if the given block in non-chunk coordinates is in this Town
     */
    public boolean isPointInTown(int dim, int x, int z) {
        return isChunkInTown(dim, x >> 4, z >> 4);
    }

    public boolean isChunkInTown(int dim, int chunkX, int chunkZ) {
        return townBlocksContainer.contains(dim, chunkX, chunkZ);
    }


}
