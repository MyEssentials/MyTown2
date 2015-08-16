package mytown.entities;

import myessentials.teleport.Teleport;
import myessentials.utils.PlayerUtils;
import mytown.api.container.*;
import mytown.config.Config;
import mytown.entities.flag.FlagType;
import mytown.proxies.LocalizationProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public final Bank bank = new Bank();

    public Town(String name) {
        this.name = name;
    }

    public void notifyResidentJoin(Resident res) {
        for (Resident toRes : residentsMap.keySet()) {
            toRes.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.joined", res.getPlayerName(), getName()));
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
    @SuppressWarnings("unchecked")
    public boolean hasPermission(Resident res, FlagType flagType, Object denialValue) {
        if(flagsContainer.getValue(flagType).equals(denialValue) && (!residentsMap.containsKey(res) || ((Boolean)flagsContainer.getValue(FlagType.RESTRICTIONS) && flagType != FlagType.ENTER && getMayor() != res))) {
            return PlayerUtils.isOp(res.getPlayer());
        }
        return true;
    }

    public Object getValueAtCoords(int dim, int x, int y, int z, FlagType flagType) {
        Plot plot = plotsContainer.get(dim, x, y, z);
        if(plot == null || flagType.isTownOnly()) {
            return flagsContainer.get(flagType);
        } else {
            return plot.flagsContainer.get(flagType);
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
            	Resident mayor = getMayor();
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

    /**
     * Gets the resident with the rank of 'Mayor' (or whatever it's named)
     */
    public Resident getMayor() {
        for (Map.Entry<Resident, Rank> entry : residentsMap.entrySet()) {
            if (entry.getValue().getName().equals(Rank.theMayorDefaultRank))
                return entry.getKey();
        }
        return null;
    }

    public void sendToSpawn(Resident res) {
        EntityPlayer pl = res.getPlayer();
        if (pl != null) {
            PlayerUtils.teleport((EntityPlayerMP)pl, spawn.getDim(), spawn.getX(), spawn.getY(), spawn.getZ());
            res.setTeleportCooldown(Config.teleportCooldown);
        }
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
