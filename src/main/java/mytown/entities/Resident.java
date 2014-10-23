package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.flag.FlagType;
import mytown.api.interfaces.IBlockWhitelister;
import mytown.api.interfaces.IHasPlots;
import mytown.api.interfaces.IHasTowns;
import mytown.api.interfaces.IPlotSelector;
import mytown.handlers.VisualsTickHandler;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Joe Goett
 */
public class Resident implements IHasPlots, IHasTowns, IPlotSelector, IBlockWhitelister { // TODO Make Comparable
    private EntityPlayer player;
    private UUID playerUUID;
    private String playerName; // This is only for display purposes when the player is offline
    private Date joinDate, lastOnline;

    // Plot selection variables
    private int selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2, selectionDim;
    private Town selectionTown;
    private boolean firstSelectionActive = false, secondSelectionActive = false;
    private boolean selectionExpandedVert = false;

    // Location checking
    private int lastChunkZ, lastChunkX;
    private int lastDim;

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
    public Resident(String uuid, String playerName, long joinDate, long lastOnline) {
        setUUID(uuid);
        this.joinDate = new Date((long)joinDate * 1000L);
        this.lastOnline = new Date((long)lastOnline * 1000L);
        this.playerName = playerName;
    }

    /**
     * Returns the EntityPlayer, or null
     *
     * @return
     */
    public EntityPlayer getPlayer() {
        return player;
    }

    /**
     * Sets the player and the UUID
     *
     * @param pl
     */
    public void setPlayer(EntityPlayer pl) {
        this.player = pl;
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
        if (this.player != null) {
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
     * @see mytown.api.interfaces.IHasPlots
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

    /* ----- IHasTowns ----- */

    private List<Town> towns = new ArrayList<Town>();
    public Town selectedTown = null;

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

    /**
     * Called when a player changes location from a chunk to another
     *
     * @param oldChunkX
     * @param oldChunkZ
     * @param newChunkX
     * @param newChunkZ
     * @param dimension
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

            lastDim = dimension;
            lastChunkX = newChunkX;
            lastChunkZ = newChunkZ;
        }
    }

    /**
     * More simpler version of location check, without the need to know the old chunk's coords
     *
     * @param newChunkX
     * @param newChunkZ
     * @param dimension
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

        lastDim = dimension;
        lastChunkX = newChunkX;
        lastChunkZ = newChunkZ;
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

    public boolean hasInvite(Town town) {
        return invites.contains(town);
    }

    /* ---- Friends ---- */

    private List<Resident> friends = new ArrayList<Resident>();
    private List<Resident> friendRequests = new ArrayList<Resident>();
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
        if(friends.contains(res) || friendRequests.contains(res)) return false;
        return friendRequests.add(res);
    }

    public void verifyFriendRequest(Resident res, boolean response) {
        if(response) {
            friends.add(res);
            res.addFriend(this);
        }
        friendRequests.remove(res);
    }

    public boolean hasFriendRequest(Resident res) {
        return friendRequests.contains(res);
    }

    /* ----- Helpers ----- */

    public void sendMessage(String msg) {
        if (getPlayer() != null)
            ChatUtils.sendChat(getPlayer(), msg);
    }

    public void protectionDenial(String message, String owner) {
        if(getPlayer() != null) {
            ChatUtils.sendChat(getPlayer(), message);
            ChatUtils.sendChat(getPlayer(), owner);
        }
    }

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

    public Plot getPlotAtPlayerPosition() {
        for (Plot plot : MyTownUniverse.getInstance().getPlotsMap().values()) {
            if (plot.isCoordWithin(player.dimension, (int) player.posX, (int) player.posY, (int) player.posZ))
                return plot;
        }
        return null;
    }

    // //////////////////////////////////////
    // PLOT SELECTION
    // //////////////////////////////////////

    // Mostly a workaround, might be changed

    @Override
    public void startPlotSelection() {
        ItemStack selectionTool = new ItemStack(Items.wooden_hoe);
        selectionTool.setStackDisplayName(Constants.EDIT_TOOL_NAME);
        NBTTagList lore = new NBTTagList();
        lore.appendTag(new NBTTagString(Constants.EDIT_TOOL_DESCRIPTION_PLOT));
        lore.appendTag(new NBTTagString(EnumChatFormatting.DARK_AQUA + "Uses: 1"));
        selectionTool.getTagCompound().getCompoundTag("display").setTag("Lore", lore);

        boolean ok = !player.inventory.hasItemStack(selectionTool);
        boolean result = false;
        if (ok) {
            result = player.inventory.addItemStackToInventory(selectionTool);
        }
        if (result) {
            sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.plot.start"));
        } else if (ok) {
            sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.start.failed"));
        }
    }

    @Override
    public boolean selectBlockForPlot(int dim, int x, int y, int z) {
        TownBlock tb = getDatasource().getBlock(dim, x >> 4, z >> 4);
        if (firstSelectionActive && selectionDim != dim)
            return false;
        if (tb == null || tb.getTown() != getSelectedTown() && !firstSelectionActive || tb.getTown() != selectionTown && firstSelectionActive) {

            return false;
        }
        if (!firstSelectionActive) {
            secondSelectionActive = false;
            selectionDim = dim;
            selectionX1 = x;
            selectionY1 = y;
            selectionZ1 = z;
            selectionTown = tb.getTown();
            firstSelectionActive = true;

            // This is marked twice :P
            VisualsTickHandler.instance.markBlock(x, y, z, dim);

        } else {

            selectionX2 = x;
            selectionY2 = y;
            selectionZ2 = z;
            secondSelectionActive = true;
            VisualsTickHandler.instance.markPlotCorners(selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2, selectionDim);
        }

        return true;
    }

    @Override
    public boolean isFirstPlotSelectionActive() {
        return firstSelectionActive;
    }

    @Override
    public boolean isSecondPlotSelectionActive() {
        return secondSelectionActive;
    }

    @Override
    public Plot makePlotFromSelection(String plotName) {
        // TODO: Check everything separately or throw exceptions?

        if (!secondSelectionActive || !firstSelectionActive || (Math.abs(selectionX1 - selectionX2) < Plot.minX || Math.abs(selectionY1 - selectionY2) < Plot.minY || Math.abs(selectionZ1 - selectionZ2) < Plot.minZ) && !(selectedTown instanceof AdminTown)) {
            resetSelection();
            return null;
        }

        int x1 = selectionX1, x2 = selectionX2, y1 = selectionY1, y2 = selectionY2, z1 = selectionZ1, z2 = selectionZ2;

        if (x2 < x1) {
            int aux = x1;
            x1 = x2;
            x2 = aux;
        }
        if (y2 < y1) {
            int aux = y1;
            y1 = y2;
            y2 = aux;
        }
        if (z2 < z1) {
            int aux = z1;
            z1 = z2;
            z2 = aux;
        }

        int lastX = 1000000, lastZ = 1000000;
        for (int i = x1; i <= x2; i++) {
            for (int j = z1; j <= z2; j++) {
                if (i >> 4 != lastX || j >> 4 != lastZ) {
                    lastX = i >> 4;
                    lastZ = j >> 4;
                    if (!getDatasource().hasBlock(selectionDim, lastX, lastZ, true, selectionTown)) {
                        System.out.println("Outside town boundaries");
                        resetSelection();
                        return null;
                    }
                }
                for (int k = y1; k <= y2; k++) {
                    if (selectionTown.getPlotAtCoords(selectionDim, i, k, j) != null) {
                        System.out.println("Inside another plot" + selectionTown.getPlotAtCoords(selectionDim, i, k, j) + "\n" + i + " " + k + " " + j);
                        System.out.println("For selection: " + x1 + " " + y1 + " " + z1 + " " + x2 + " " + y2 + " " + z2);
                        resetSelection();
                        return null;
                    }
                }
            }
        }

        Plot plot = DatasourceProxy.getDatasource().newPlot(plotName, selectionTown, selectionDim, selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2);

        player.setCurrentItemOrArmor(0, null);
        resetSelection();
        return plot;
    }

    @Override
    public void expandSelectionVert() {
        // When selection is expanded vertically we'll show it's borders... (Temporary solution)

        VisualsTickHandler.instance.unmarkPlotCorners(selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2, selectionDim);

        selectionY1 = 1;
        try {
            selectionY2 = player.worldObj.getActualHeight() - 1;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }
        selectionExpandedVert = true;

        VisualsTickHandler.instance.markPlotBorders(selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2, selectionDim);
    }

    @Override
    public void resetSelection() {
        firstSelectionActive = false;
        secondSelectionActive = false;

        if (selectionExpandedVert) {
            VisualsTickHandler.instance.unmarkPlotBorders(selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2, selectionDim);
        } else {
            VisualsTickHandler.instance.unmarkPlotCorners(selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2, selectionDim);
        }
    }


    // //////////////////////////////////////
    // BLOCK WHITELISTER
    // //////////////////////////////////////

    /**
     * Assists in selecting a block
     *
     * @param flagType
     * @return
     */
    @Override
    public boolean startBlockSelection(FlagType flagType, String townName, boolean inPlot) {
        //Give item to player
        ItemStack selectionTool = new ItemStack(Items.wooden_hoe);
        selectionTool.setStackDisplayName(Constants.EDIT_TOOL_NAME);
        NBTTagList lore = new NBTTagList();
        lore.appendTag(new NBTTagString(Constants.EDIT_TOOL_DESCRIPTION_BLOCK_WHITELIST));
        lore.appendTag(new NBTTagString(EnumChatFormatting.DARK_AQUA + "Flag: " + flagType.toString()));
        lore.appendTag(new NBTTagString(EnumChatFormatting.DARK_AQUA + "Town: " + townName));
        lore.appendTag(new NBTTagString(EnumChatFormatting.DARK_AQUA + "Uses: 1"));
        selectionTool.getTagCompound().getCompoundTag("display").setTag("Lore", lore);

        return player.inventory.addItemStackToInventory(selectionTool);
    }


    private MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }

}
