package mytown.entities;

import myessentials.utils.ChatUtils;
import mytown.MyTown;
import mytown.api.container.PlotsContainer;
import mytown.api.container.ToolContainer;
import mytown.api.container.TownsContainer;
import mytown.config.Config;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Date;
import java.util.UUID;

public class Resident {
    private EntityPlayer player;
    private UUID playerUUID;
    private String playerName; // This is only for display purposes when the player is offline
    private Date joinDate, lastOnline;
    private int extraBlocks = 0;
    private int teleportCooldown = 0;
    private boolean mapOn = false;

    public final PlotsContainer plotsContainer = new PlotsContainer(Config.defaultMaxPlots);
    public final TownsContainer townsContainer = new TownsContainer();
    public final TownsContainer townInvitesContaine = new TownsContainer();
    public final ToolContainer toolContainer = new ToolContainer();

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

    public Date getJoinDate() {
        return joinDate;
    }

    public Date getLastOnline() {
        if (this.player != null) {
            lastOnline = new Date(); // TODO Do we REALLY need to update this each time its received, or can we do this better?
        }
        return lastOnline;
    }

    public void setLastOnline(Date date) {
        this.lastOnline = date;
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

            oldTownBlock = DatasourceProxy.getDatasource().getBlock(dimension, oldChunkX, oldChunkZ);
            newTownBlock = DatasourceProxy.getDatasource().getBlock(dimension, newChunkX, newChunkZ);

            if (oldTownBlock == null && newTownBlock != null || oldTownBlock != null && newTownBlock != null && !oldTownBlock.getTown().getName().equals(newTownBlock.getTown().getName())) {
                if (townsContainer.contains(newTownBlock.getTown())) {
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

        newTownBlock = DatasourceProxy.getDatasource().getBlock(dimension, newChunkX, newChunkZ);

        if (newTownBlock == null) {
            sendMessage(MyTown.getLocal().getLocalization("mytown.notification.enter.wild"));
        } else if (townsContainer.contains(newTownBlock.getTown())) {
            sendMessage(MyTown.getLocal().getLocalization("mytown.notification.enter.ownTown", newTownBlock.getTown().getName()));
        } else {
            sendMessage(MyTown.getLocal().getLocalization("mytown.notification.enter.town", newTownBlock.getTown().getName()));
        }
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
        if (townsContainer.getMainTown() != null) {
            townsContainer.getMainTown().sendToSpawn(this);
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
                while (!town.hasPermission(this, FlagType.ENTER, false, player.dimension, x, y, z) && town.isPointInTown(player.dimension, x, z))
                    x++;
                x += 3;

                while(player.worldObj.getBlock(x, y, z) != Blocks.air && player.worldObj.getBlock(x, y + 1, z) != Blocks.air && y < 256)
                    y++;

                if(town.hasPermission(this, FlagType.ENTER, false, player.dimension, x, y, z) || !town.isPointInTown(player.dimension, x, z))
                    ok = true;
            }
            player.setPositionAndUpdate(x, y, z);
        }
    }
}
