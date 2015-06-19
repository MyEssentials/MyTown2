package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.MyTown;
import mytown.api.interfaces.IPlotsContainer;
import mytown.api.interfaces.ITownsContainer;
import mytown.config.Config;
import mytown.core.utils.ChatUtils;
import mytown.datasource.MyTownDatasource;
import mytown.entities.flag.FlagType;
import mytown.entities.tools.Tool;
import mytown.handlers.VisualsHandler;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Resident implements IPlotsContainer, ITownsContainer { // TODO Make Comparable
    private EntityPlayer player;
    private UUID playerUUID;
    private String playerName; // This is only for display purposes when the player is offline
    private Date joinDate, lastOnline;
    private int extraBlocks = 0;
    private int teleportCooldown = 0;

    // Plot selection variables
    private int selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2, selectionDim;
    private Town selectionTown;
    private boolean firstSelectionActive = false, secondSelectionActive = false;
    private boolean mapOn = false;

    private Tool currentTool;

    private List<Plot> plots = new ArrayList<Plot>();
    private List<Town> towns = new ArrayList<Town>();
    private Town selectedTown = null;
    private List<Town> invites = new ArrayList<Town>();
    private List<Resident> friends = new ArrayList<Resident>();
    private List<Resident> friendRequests = new ArrayList<Resident>();

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
     */
    public Resident(String uuid, String playerName, long joinDate, long lastOnline, int extraBlocks) {
        setUUID(uuid);
        this.joinDate = new Date(joinDate * 1000L);
        this.lastOnline = new Date(lastOnline * 1000L);
        this.playerName = playerName;
        this.extraBlocks = extraBlocks;
    }

    /**
     * Returns the EntityPlayer, or null
     */
    public EntityPlayer getPlayer() {
        return player;
    }

    /**
     * Sets the player and the UUID
     */
    public void setPlayer(EntityPlayer pl) {
        this.player = pl;
        setUUID(pl.getPersistentID());
        this.playerName = pl.getDisplayName();
    }

    public UUID getUUID() {
        return playerUUID;
    }

    public void setUUID(UUID uuid) {
        this.playerUUID = uuid;
    }

    public void setUUID(String uuid) {
        setUUID(UUID.fromString(uuid));
    }

    /**
     * Returns the name of the player for display purposes. <br/>
     * NEVER rely on this to store info against. The player name can change at any point, use the UUID instead.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets when the Resident first joined
     */
    public Date getJoinDate() {
        return joinDate;
    }

    /**
     * Gets when the Resident was last online
     */
    public Date getLastOnline() {
        if (this.player != null) {
            lastOnline = new Date(); // TODO Do we REALLY need to update this each time its received, or can we do this better?
        }
        return lastOnline;
    }

    /**
     * Sets when the resident was last online
     */
    public void setLastOnline(Date date) {
        this.lastOnline = date;
    }

    /**
     * Gets the extra blocks the resident adds to the town's max total blocks
     */
    public int getExtraBlocks() {
        return extraBlocks;
    }

    /**
     * Sets the extra blocks the resident adds to the town's max total blocks
     */
    public void setExtraBlocks(int extraBlocks) {
        this.extraBlocks = extraBlocks;
    }

    public void setTeleportCooldown(int cooldownTicks) {
        this.teleportCooldown = cooldownTicks;
    }

    public int getTeleportCooldown() {
        return teleportCooldown;
    }

    /**
     * Tick function called every tick
     */
    public void tick() {
        if(teleportCooldown > 0)
            teleportCooldown--;
    }

    @Override
    public String toString() {
        return String.format("Resident: {Name: %s, UUID: %s}", playerName, playerUUID);
    }

    /* ----- IHasPlots ----- */

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
     * @see IPlotsContainer
     */
    @Override
    public Plot getPlotAtCoords(int dim, int x, int y, int z) {
        for (Plot plot : plots) {
            if (plot.isCoordWithin(dim, x, y, z)) {
                return plot;
            }
        }
        return null;
    }

    @Override
    public Plot getPlot(String name) {
        for(Plot plot : plots) {
            if(plot.getName().equals(name))
                return plot;
        }
        return null;
    }

    /* ----- IHasTowns ----- */

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
     */
    public Town getSelectedTown() {
        if (selectedTown == null) {
            return towns.isEmpty() ? null : towns.get(0);
        }
        return selectedTown;
    }

    public void selectTown(Town town) {
        selectedTown = town;
    }

    /**
     * Returns this Residents rank in the given Town, or null if the player is not part of the Town
     */
    public Rank getTownRank(Town town) {
        if (!towns.contains(town))
            return null;
        return town.getResidentRank(this);
    }

    /**
     * Shortcut for Town#getTownRank(Resident#getSelectedTown())
     */
    public Rank getTownRank() {
        return getTownRank(getSelectedTown());
    }

    /* ----- Map ----- */

    public boolean isMapOn() {
        return mapOn;
    }

    public void setMapOn(boolean isOn) {
        mapOn = isOn;
    }

    /**
     * Called when a player changes location from a chunk to another
     */
    public void checkLocation(int oldChunkX, int oldChunkZ, int newChunkX, int newChunkZ, int dimension) {
        if (oldChunkX != newChunkX || oldChunkZ != newChunkZ && player != null) {
            TownBlock oldTownBlock, newTownBlock;

            oldTownBlock = getDatasource().getBlock(dimension, oldChunkX, oldChunkZ);
            newTownBlock = getDatasource().getBlock(dimension, newChunkX, newChunkZ);

            if (oldTownBlock == null && newTownBlock != null || oldTownBlock != null && newTownBlock != null && !oldTownBlock.getTown().getName().equals(newTownBlock.getTown().getName())) {
                if (towns.contains(newTownBlock.getTown())) {
                    sendMessage(MyTown.getLocal().getLocalization("mytown.notification.enter.ownTown", newTownBlock.getTown().getName()));
                } else {
                    sendMessage(MyTown.getLocal().getLocalization("mytown.notification.enter.town", newTownBlock.getTown().getName()));
                }
            } else if (oldTownBlock != null && newTownBlock == null) {
                sendMessage(MyTown.getLocal().getLocalization("mytown.notification.enter.wild"));
            }
        }
    }

    /**
     * More simpler version of location check, without the need to know the old chunk's coords
     */
    public void checkLocationOnDimensionChanged(int newChunkX, int newChunkZ, int dimension) {
        TownBlock newTownBlock;

        newTownBlock = getDatasource().getBlock(dimension, newChunkX, newChunkZ);

        if (newTownBlock == null) {
            sendMessage(MyTown.getLocal().getLocalization("mytown.notification.enter.wild"));
        } else if (towns.contains(newTownBlock.getTown())) {
            sendMessage(MyTown.getLocal().getLocalization("mytown.notification.enter.ownTown", newTownBlock.getTown().getName()));
        } else {
            sendMessage(MyTown.getLocal().getLocalization("mytown.notification.enter.town", newTownBlock.getTown().getName()));
        }
    }

    /* ----- Invites ----- */

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

    public boolean hasInvite(Town town) {
        return invites.contains(town);
    }

    /* ---- Friends ---- */

    public boolean addFriend(Resident res) {
        return friends.add(res);
    }

    public boolean removeFriend(Resident res) {
        return friends.remove(res);
    }

    public boolean hasFriend(Resident res) {
        return friends.contains(res);
    }

    public List<Resident> getFriends() {
        return friends;
    }

    public boolean addFriendRequest(Resident res) {
        if (friends.contains(res) || friendRequests.contains(res))
            return false;
        return friendRequests.add(res);
    }

    public boolean removeFriendRequest(Resident res) {
        return friendRequests.remove(res);
    }

    public boolean hasFriendRequest(Resident res) {
        return friendRequests.contains(res);
    }

    /* ----- Helpers ----- */

    public void sendMessage(String msg) {
        if (getPlayer() != null && !(getPlayer() instanceof FakePlayer))
            ChatUtils.sendChat(getPlayer(), msg);
    }

    /**
     * Sends a localized message and a list of owners to which the protection was bypassed
     */
    public void protectionDenial(String message, String owner) {
        if (getPlayer() != null) {
            ChatUtils.sendChat(getPlayer(), message);
            ChatUtils.sendChat(getPlayer(), owner);
        }
    }

    /**
     * Respawns the player at town's spawn point or, if that doesn't exist, at his own spawn point.
     */
    public void respawnPlayer() {
        if (getSelectedTown() != null) {
            getSelectedTown().sendToSpawn(this);
            return;
        }

        ChunkCoordinates spawn = player.getBedLocation(player.dimension);
        if (spawn == null)
            spawn = player.worldObj.getSpawnPoint();
        ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(spawn.posX, spawn.posY, spawn.posZ, player.rotationYaw, player.rotationPitch);
    }

    /**
     * Moves the player to the position he was last tick.
     */
    public void knockbackPlayer() {
        if(this.player != null) {
            player.setPositionAndUpdate(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
        }
    }

    /**
     * Moves the player to the nearest place (in the positive X direction) in which it has permission to enter.
     */
    public void knockbackPlayerToBorder(Town town) {
        if(this.player != null) {
            int x = (int) Math.floor(player.posX);
            int y = (int) Math.floor(player.posY);
            int z = (int) Math.floor(player.posZ);
            boolean ok = false;
            while(!ok) {
                while (!town.checkPermission(this, FlagType.ENTER, false, player.dimension, x, y, z) && town.isPointInTown(player.dimension, x, z))
                    x++;
                x += 3;

                while(player.worldObj.getBlock(x, y, z) != Blocks.air && player.worldObj.getBlock(x, y + 1, z) != Blocks.air && y < 256)
                    y++;

                if(town.checkPermission(this, FlagType.ENTER, false, player.dimension, x, y, z) || !town.isPointInTown(player.dimension, x, z))
                    ok = true;
            }
            player.setPositionAndUpdate(x, y, z);
        }
    }

    /* ----- Plot Selection ----- */

    public Tool getCurrentTool() {
        return this.currentTool;
    }

    public boolean hasTool() {
        return currentTool != null;
    }

    /**
     * Intended to only be used when processing a command
     */
    public void setCurrentTool(Tool tool) {
        if(this.currentTool != null)
            throw new MyTownCommandException("mytown.cmd.err.inventory.tool.already");

        this.currentTool = tool;
    }

    public void removeCurrentTool() {
        this.currentTool = null;
    }

    private MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}
