package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.core.ChatUtils;
import mytown.entities.interfaces.IHasPlots;
import mytown.entities.interfaces.IHasTowns;
import net.minecraft.entity.player.EntityPlayer;

import java.lang.ref.WeakReference;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

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

    public Resident(String uuid, String playerName) {
        setUUID(uuid);
        this.playerName = playerName;
        this.joinDate = new Date();
        this.lastOnline = joinDate;
    }

    /**
     * Creates a new Resident with the given uuid, playerName, joinDate, and lastOnline. Used only during datasource loading!
     *
     * @param uuid
     * @param playerName
     * @param joinDate
     * @param lastOnline
     */
    public Resident(String uuid, String playerName, String joinDate, String lastOnline) {
        setUUID(uuid);

        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS", Locale.ENGLISH);

        try {
            this.joinDate = format.parse(joinDate);
            setLastOnline(format.parse(lastOnline));
        } catch(ParseException e) {
            e.printStackTrace();
        }
        this.playerName = playerName;
    }

    /**
     * Returns the EntityPlayer, or null
     *
     * @return
     */
    public EntityPlayer getPlayer() {
        return playerRef.get();
    }

    /**
     * Sets the player and the UUID
     *
     * @param pl
     */
    public void setPlayer(EntityPlayer pl) {
        this.playerRef = new WeakReference<EntityPlayer>(pl);
        setUUID(pl.getPersistentID());
        this.playerName = pl.getDisplayName();
    }

    /**
     * Returns the players UUID
     *
     * @return
     */
    public UUID getUUID() {
        return playerUUID;
    }

    /**
     * Sets the UUID
     *
     * @param uuid
     */
    public void setUUID(UUID uuid) {
        this.playerUUID = uuid;
    }

    /**
     * Sets the UUID from the given string
     *
     * @param uuid
     */
    public void setUUID(String uuid) {
        setUUID(UUID.fromString(uuid));
    }

    /**
     * Returns the name of the player for display purposes. <br/>
     * NEVER rely on this to store info against. The player name can change at any point, use the UUID instead.
     *
     * @return
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets when the Resident first joined
     *
     * @return
     */
    public Date getJoinDate() {
        return joinDate;
    }

    /**
     * Gets when the Resident was last online
     *
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
     *
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

    private List<Plot> plots = new ArrayList<Plot>();

    @Override
    public void addPlot(Plot plot) {
        plots.add(plot);
    }

    @Override
    public void removePlot(Plot plot) {
        plots.remove(plot);
    }

    @Override
    public boolean hasPlot(Plot plot) {
        return plots.contains(plot);
    }

    @Override
    public ImmutableList<Plot> getPlots() {
        return ImmutableList.copyOf(plots);
    }

    /**
     * This does NOT perform as well as some other methods of retrieving plots. Please use sparingly and with caution!
     *
     * @param dim
     * @param x
     * @param y
     * @param z
     * @return
     * @see mytown.entities.interfaces.IHasPlots
     */
    @Override
    public Plot getPlotAtCoord(int dim, int x, int y, int z) {
        for (Plot plot : plots) {
            if (plot.isCoordWithin(dim, x, y, z)) {
                return plot;
            }
        }
        return null;
    }

    /* ----- IHasTowns ----- */

    private List<Town> towns = new ArrayList<Town>();
    private Town selectedTown = null;

    @Override
    public void addTown(Town town) {
        towns.add(town);
    }

    @Override
    public void removeTown(Town town) {
        towns.remove(town);
    }

    @Override
    public boolean hasTown(Town town) {
        return towns.contains(town);
    }

    @Override
    public ImmutableList<Town> getTowns() {
        return ImmutableList.copyOf(towns);
    }

    /**
     * Returns the currently selected Town, the first Town (if none is selected), or null if not part of a town.
     *
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
     *
     * @param town
     */
    public void selectTown(Town town) {
        selectedTown = town;
    }

    /**
     * Returns this Residents rank in the given Town, or null if the player is not part of the Town (TODO Maybe change?)
     *
     * @param town
     * @return
     */
    public Rank getTownRank(Town town) {
        if (!towns.contains(town)) return null;
        return town.getResidentRank(this);
    }

    /**
     * Shortcut for Town#getTownRank(Resident#getSelectedTown())
     *
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

    /* ----- Invites ----- */

    private List<Town> invites = new ArrayList<Town>();

    public void addInvite(Town invite) {
        invites.add(invite);
    }

    public void removeInvite(Town invite) {
        invites.remove(invite);
    }

    public void removeInvite(String name) {
        for (Town t : invites) {
            if (t.getName().equals(name)) {
                invites.remove(t);
                break;
            }
        }
    }

    public ImmutableList<Town> getInvites() {
        return ImmutableList.copyOf(invites);
    }

    public Town getInvite(String townName) {
        for (Town t : invites) {
            if (t.getName().equals(townName)) {
                return t;
            }
        }
        return null;
    }

    public boolean hasInvite() {
        return !invites.isEmpty();
    }

    /* ----- Helpers ----- */

    public void sendMessage(String msg) {
        ChatUtils.sendChat(getPlayer(), msg);
    }
}
