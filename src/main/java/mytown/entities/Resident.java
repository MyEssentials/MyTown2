package mytown.entities;

import myessentials.utils.ChatUtils;
import mytown.MyTown;
import mytown.api.container.GenericContainer;
import mytown.api.container.PlotsContainer;
import mytown.api.container.TownsContainer;
import mytown.config.Config;
import mytown.datasource.MyTownUniverse;
import mytown.entities.flag.FlagType;
import mytown.entities.tools.Tool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Date;
import java.util.UUID;

public class Resident {
    private EntityPlayer player;
    private UUID playerUUID;
    private String playerName;
    private Date joinDate = new Date();
    private Date lastOnline = new Date();

    private int teleportCooldown = 0;

    private int extraBlocks = 0;

    public final PlotsContainer plotsContainer = new PlotsContainer(Config.defaultMaxPlots);
    public final TownsContainer townInvitesContainer = new TownsContainer();
    public final TownsContainer townsContainer = new TownsContainer();
    public final GenericContainer<Tool> toolContainer = new GenericContainer<Tool>();

    public Resident(EntityPlayer pl) {
        setPlayer(pl);
        this.playerUUID = pl.getPersistentID();
    }

    public Resident(UUID uuid, String playerName) {
        this.playerUUID = uuid;
        this.playerName = playerName;
    }

    public Resident(UUID uuid, String playerName, long joinDate, long lastOnline) {
        this(uuid, playerName);
        this.joinDate.setTime(joinDate * 1000L);
        this.lastOnline.setTime(lastOnline * 1000L);
    }

    /**
     * Tick function called every tick
     */
    public void tick() {
        if(teleportCooldown > 0)
            teleportCooldown--;
    }

    /* ----- Map ----- */

    /**
     * Called when a player changes location from a chunk to another
     */
    public void checkLocation(int oldChunkX, int oldChunkZ, int newChunkX, int newChunkZ, int dimension) {
        if (oldChunkX != newChunkX || oldChunkZ != newChunkZ && player != null) {
            TownBlock oldTownBlock, newTownBlock;

            oldTownBlock = MyTownUniverse.instance.blocks.get(dimension, oldChunkX, oldChunkZ);
            newTownBlock = MyTownUniverse.instance.blocks.get(dimension, newChunkX, newChunkZ);

            if (oldTownBlock == null && newTownBlock != null || oldTownBlock != null && newTownBlock != null && !oldTownBlock.getTown().getName().equals(newTownBlock.getTown().getName())) {
                if (townsContainer.contains(newTownBlock.getTown())) {
                    sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.enter.ownTown", newTownBlock.getTown().getName()));
                } else {
                    sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.enter.town", newTownBlock.getTown().getName()));
                }
            } else if (oldTownBlock != null && newTownBlock == null) {
                sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.enter.wild"));
            }
        }
    }

    /**
     * More simpler version of location check, without the need to know the old chunk's coords
     */
    public void checkLocationOnDimensionChanged(int newChunkX, int newChunkZ, int dimension) {
        TownBlock newTownBlock;

        newTownBlock = MyTownUniverse.instance.blocks.get(dimension, newChunkX, newChunkZ);

        if (newTownBlock == null) {
            sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.enter.wild"));
        } else if (townsContainer.contains(newTownBlock.getTown())) {
            sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.enter.ownTown", newTownBlock.getTown().getName()));
        } else {
            sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.enter.town", newTownBlock.getTown().getName()));
        }
    }

    /* ----- Helpers ----- */

    public void sendMessage(String msg) {
        try {
            if (getPlayer() != null && !(getPlayer() instanceof FakePlayer))
                ChatUtils.sendChat(getPlayer(), msg);
        } catch (NullPointerException ex) {
            MyTown.instance.LOG.info("You are probably using a modified server that messes with order of Player joining/leaving. This crash is nothing serious.");
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * Sends a localized message and a list of owners to which the protection was bypassed
     */
    public void protectionDenial(FlagType flag, String owners) {
        if (getPlayer() != null) {
            ChatUtils.sendChat(getPlayer(), flag.getLocalizedProtectionDenial());
            ChatUtils.sendChat(getPlayer(), MyTown.instance.LOCAL.getLocalization("mytown.notification.town.owners", owners));
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

    public EntityPlayer getPlayer() {
        return player;
    }

    public void setPlayer(EntityPlayer pl) {
        this.player = pl;
        this.playerName = pl.getDisplayName();
    }

    public UUID getUUID() {
        return playerUUID;
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

    public int getExtraBlocks() {
        return extraBlocks;
    }

    public void setExtraBlocks(int extraBlocks) {
        this.extraBlocks = extraBlocks;
    }
}
