package mytown.entities;

import mytown.core.ChatUtils;
import mytown.entities.interfaces.IHasPlots;
import mytown.entities.interfaces.IHasTowns;
import net.minecraft.entity.player.EntityPlayer;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Joe Goett
 */
public class Resident implements IHasPlots, IHasTowns { // TODO Make Comparable
    private WeakReference<EntityPlayer> playerRef;
    private UUID playerUUID;
    private String playerName; // This is only for display purposes when the player is offline
    private Date joinDate, lastOnline;

    public Resident(EntityPlayer pl) {
        setPlayer(pl);
        this.joinDate = new Date();
        this.lastOnline = joinDate;
    }

    public Resident(String uuid) {
        setUUID(uuid);
        this.joinDate = new Date();
        this.lastOnline = joinDate;
    }

    /**
     * Creates a new Resident with the given uuid, playerName, joinDate, and lastOnline. Used only during datasource loading!
     * @param uuid
     * @param playerName
     * @param joinDate
     * @param lastOnline
     */
    public Resident(String uuid, String playerName, Date joinDate, Date lastOnline) {
        setUUID(uuid);
        this.playerName = playerName;
        this.joinDate = joinDate;
        setLastOnline(lastOnline);
    }

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
     * Sets the UUID from the given string
     * @param uuid
     */
    public void setUUID(String uuid) {
        setUUID(UUID.fromString(uuid));
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
     * Gets when the Resident first joined
     * @return
     */
    public Date getJoinDate() {
        return joinDate;
    }

    /**
     * Gets when the Resident was last online
     * @return
     */
    public Date getLastOnline() {
        if (this.playerRef != null && this.playerRef.get() != null) {
            lastOnline = new Date(); // TODO Do we REALLY need to update this each time its received, or can we do this better?
        }
        return lastOnline;
    }

    /**
     * Sets when the resident was last online
     * @param date
     */
    public void setLastOnline(Date date) {
        this.lastOnline = date;
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
    public Plot getPlotAtCoord(int dim, int x, int y, int z) {
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

    /**
     * Returns this Residents rank in the given Town, or null if the player is not part of the Town (TODO Maybe change?)
     * @param town
     * @return
     */
    public Rank getTownRank(Town town) {
        if (!towns.contains(town)) return null;
        return town.getResidentRank(this);
    }

    /**
     * Shortcut for getTownRank(getSelectedTown())
     * @return
     */
    public Rank getTownRank() {
        return getTownRank(getSelectedTown());
    }

    /* ----- Map ----- */

    private boolean mapOn = false;

    public boolean isMapOn() {
        return mapOn;
    }

    public void setMapOn(boolean isOn) {
        mapOn = isOn;
    }

    /* ----- Helpers ----- */

    public void sendMessage(String msg) {
        ChatUtils.sendChat(getPlayer(), msg);
    }
}
