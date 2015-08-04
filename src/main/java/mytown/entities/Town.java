package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.api.container.FlagsContainer;
import mytown.api.container.PlotsContainer;
import mytown.api.container.ResidentsContainer;
import mytown.api.container.TownBlocksContainer;
import mytown.api.container.interfaces.*;
import mytown.config.Config;
import myessentials.utils.PlayerUtils;
import myessentials.teleport.Teleport;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.handlers.VisualsHandler;
import mytown.proxies.LocalizationProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.*;

/**
 * Defines a Town. A Town is made up of Residents, Ranks, Blocks, and Plots.
 */
public class Town implements Comparable<Town> {
    private String name, oldName = null;

    private Rank defaultRank = null;
    protected int extraBlocks = 0;
    protected int maxFarClaims = Config.maxFarClaims;

    private int bankAmount = 0;
    private int daysNotPaid = 0;

    private Nation nation = null;
    private Teleport spawn = null;

    public final ResidentsContainer residentsContainer = new ResidentsContainer();
    public final PlotsContainer plotsContainer = new PlotsContainer(Config.defaultMaxPlots);
    public final FlagsContainer flagsContainer = new FlagsContainer();
    public final TownBlocksContainer townBlocksContainer = new TownBlocksContainer();

    public Town(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    /* ----- Nation ----- */

    public Nation getNation() { return nation; }
    public void setNation(Nation nation) { this.nation = nation; }

    /* ----- Spawn ----- */

    public void sendToSpawn(Resident res) {
        EntityPlayer pl = res.getPlayer();
        if (pl != null) {
            PlayerUtils.teleport((EntityPlayerMP)pl, spawn.getDim(), spawn.getX(), spawn.getY(), spawn.getZ());
            res.setTeleportCooldown(Config.teleportCooldown);
        }
    }

    public boolean hasSpawn() { return spawn != null; }
    public Teleport getSpawn() { return spawn; }
    public void setSpawn(Teleport spawn) { this.spawn = spawn; }



    /* ----- Helpers ----- */

    /**
     * Checks if the given block in non-chunk coordinates is in this Town
     */
    public boolean isPointInTown(int dim, int x, int z) {
        return isChunkInTown(dim, x >> 4, z >> 4);
    }

    public boolean isChunkInTown(int dim, int chunkX, int chunkZ) {
        return townBlocksContainer.contains(dim, chunkX, chunkZ);
    }

    public void notifyResidentJoin(Resident res) {
        for (Resident toRes : residentsContainer.asList()) {
            toRes.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.joined", res.getPlayerName(), getName()));
        }
    }

    /**
     * Notifies every resident in this town sending a message.
     */
    public void notifyEveryone(String message) {
        for (Resident r : residentsContainer.asList()) {
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
        if(flagsContainer.getValue(flagType).equals(denialValue) && (!residentsContainer.contains(res) || ((Boolean)flagsContainer.getValue(FlagType.RESTRICTIONS) && flagType != FlagType.ENTER && getMayor() != res))) {
            return PlayerUtils.isOp(res.getPlayer());
        }
        return true;
    }

    /**
     * Used to get the owners of a plot (or a town) at the position given
     * Returns null if position is not in town
     */
    public List<Resident> getOwnersAtPosition(int dim, int x, int y, int z) {
        List<Resident> list = new ArrayList<Resident>();
        Plot plot = plotsContainer.get(dim, x, y, z);
        if (plot == null) {
            if (isPointInTown(dim, x, z) && !(this instanceof AdminTown) && !residentsContainer.isEmpty()) {
            	Resident mayor = getMayor();
                if (mayor != null) {
                	list.add(mayor);
                }
            }
        } else {
            for (Resident res : plot.ownersContainer.asList()) {
                list.add(res);
            }
        }
        return list;
    }

    /**
     * Gets the resident with the rank of 'Mayor' (or whatever it's named)
     */
    /*
    public Resident getMayor() {
        for (Resident res : residents.keySet()) {
            if (residents.get(res).getName().equals(Rank.theMayorDefaultRank))
                return res;
        }
        return null;
    }
    */

    /* ----- Comparable ----- */

    @Override
    public int compareTo(Town t) { // TODO Flesh this out more for ranking towns?
        int thisNumberOfResidents = residentsContainer.size(),
                thatNumberOfResidents = t.residentsContainer.size();
        if (thisNumberOfResidents > thatNumberOfResidents)
            return -1;
        else if (thisNumberOfResidents == thatNumberOfResidents)
            return 0;
        else if (thisNumberOfResidents < thatNumberOfResidents)
            return 1;

        return -1;
    }
}
