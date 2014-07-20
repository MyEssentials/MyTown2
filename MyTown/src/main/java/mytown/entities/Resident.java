package mytown.entities;

import mytown.core.ChatUtils;
import mytown.entities.interfaces.IHasPlots;
import mytown.entities.interfaces.IHasTowns;
import net.minecraft.entity.player.EntityPlayer;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author Joe Goett
 */
public class Resident implements IHasPlots, IHasTowns { // TODO Make Comparable
    private UUID playerUUID;
    private String playerName; // This is only for display purposes when the player is offline
    private WeakReference<EntityPlayer> playerRef;

    /**
     * Returns the EntityPlayer, or null
     * @return
     */
    public EntityPlayer getPlayer() {
        return playerRef.get();
    }

    /**
     * Sets the player and the UUID
     * @param pl
     */
    public void setPlayer(EntityPlayer pl) {
        this.playerRef = new WeakReference<EntityPlayer>(pl);
        setUUID(pl.getPersistentID());
        this.playerName = pl.getDisplayName();
    }

    /**
     * Returns the players UUID
     * @return
     */
    public UUID getUUID() {
        return playerUUID;
    }

    /**
     * Sets the UUID
     * @param uuid
     */
    public void setUUID(UUID uuid) {
        this.playerUUID = uuid;
    }

    /**
     * Returns the name of the player for display purposes. <br/>
     * NEVER rely on this to store info against. The player name can change at any point, use the UUID instead.
     * @return
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Sets the UUID from the given string
     * @param uuid
     */
    public void setUUID(String uuid) {
        setUUID(UUID.fromString(uuid));
    }

    @Override
    public String toString() {
        return String.format("Resident: {Name: %s, UUID: %s}", playerName, playerUUID);
    }

    /* ----- IHasPlots ----- */
    private List<Plot> plots = null;

    public void addPlot(Plot plot) {
        plots.add(plot);
    }

    public void removePlot(Plot plot) {
        plots.remove(plot);
    }

    public boolean hasPlot(Plot plot) {
        return plots.contains(plot);
    }

    public Collection<Plot> getPlots() {
        return plots;
    }

    /**
     * This does NOT perform as well as some other methods of retrieving plots. Please use sparingly and with caution!
     * @see mytown.entities.interfaces.IHasPlots
     * @param dim
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Plot getPlotAtCoords(int dim, int x, int y, int z) {
        for (Plot plot : plots) {
            if (plot.isCoordWithin(dim, x, y, z)) {
                return plot;
            }
        }
        return null;
    }

    /* ----- IHasTowns ----- */
    private List<Town> towns = null;
    private Town selectedTown = null;

    public void addTown(Town town) {
        towns.add(town);
    }

    public void removeTown(Town town) {
        towns.remove(town);
    }

    public boolean hasTown(Town town) {
        return towns.contains(town);
    }

    public Collection<Town> getTowns() {
        return towns;
    }

    /**
     * Returns the currently selected Town, the first Town (if none is selected), or null if not part of a town.
     * @return
     */
    public Town getSelectedTown() {
        if (selectedTown == null) {
            return towns.size() >= 1 ? towns.get(0) : null;
        }
        return selectedTown;
    }

    /**
     * Selects the given Town
     * @param town
     */
    public void selectTown(Town town) {
        selectedTown = town;
    }

    /* ----- Helpers ----- */
    public void sendMessage(String msg) {
        ChatUtils.sendChat(getPlayer(), msg);
    }
}
