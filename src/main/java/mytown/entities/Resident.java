package mytown.entities;

import myessentials.chat.api.ChatComponentFormatted;
import myessentials.chat.api.ChatManager;
import myessentials.chat.api.IChatFormat;
import myessentials.localization.api.LocalManager;
import mytown.MyTown;
import mytown.config.Config;
import mytown.entities.flag.FlagType;
import mytown.new_datasource.MyTownUniverse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;

import java.util.*;

public class Resident implements IChatFormat {
    private EntityPlayer player;
    private UUID playerUUID;
    private String playerName;
    private Date joinDate = new Date();
    private Date lastOnline = new Date();

    private int teleportCooldown = 0;

    private int extraBlocks = 0;

    private boolean isFakePlayer = false;

    public final Plot.Container plotsContainer = new Plot.Container(Config.instance.defaultMaxPlots.get());
    public final Town.Container townInvitesContainer = new Town.Container();
    public final Town.Container townsContainer = new Town.Container();

    public Resident(EntityPlayer pl) {
        setPlayer(pl);
        this.playerUUID = pl.getPersistentID();
    }

    public Resident(UUID uuid, String playerName) {
        this.playerUUID = uuid;
        this.playerName = playerName;
        tryLoadPlayer();
    }

    public Resident(UUID uuid, String playerName, boolean isFakePlayer) {
        this.playerUUID = uuid;
        this.playerName = playerName;
        this.isFakePlayer = isFakePlayer;
        if (!isFakePlayer) {
            tryLoadPlayer();
        }
    }

    public Resident(UUID uuid, String playerName, long joinDate, long lastOnline) {
        this(uuid, playerName);
        this.joinDate.setTime(joinDate * 1000L);
        this.lastOnline.setTime(lastOnline * 1000L);
    }

    private void tryLoadPlayer() {
        for (EntityPlayer player : (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if(player.getPersistentID().equals(playerUUID)) {
                this.player = player;
            }
        }
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
                    ChatManager.send(player, "mytown.notification.enter.ownTown", newTownBlock.getTown());
                } else {
                    ChatManager.send(player, "mytown.notification.enter.town", newTownBlock.getTown());
                }
            } else if (oldTownBlock != null && newTownBlock == null) {
                ChatManager.send(player, "mytown.notification.enter.wild");
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
            ChatManager.send(player, "mytown.notification.enter.wild");
        } else if (townsContainer.contains(newTownBlock.getTown())) {
            ChatManager.send(player, "mytown.notification.enter.ownTown", newTownBlock.getTown());
        } else {
            ChatManager.send(player, "mytown.notification.enter.town", newTownBlock.getTown());
        }
    }

    /* ----- Helpers ----- */

    public void protectionDenial(FlagType flag) {
        if (getPlayer() != null) {
            getPlayer().addChatMessage(MyTown.instance.LOCAL.getLocalization(flag.getDenialKey()));
        }
    }
    /**
     * Sends a localized message and a list of owners to which the protection was bypassed
     */
    public void protectionDenial(FlagType flag, String owners) {
        if (getPlayer() != null) {
            getPlayer().addChatMessage(MyTown.instance.LOCAL.getLocalization(flag.getDenialKey()));
            getPlayer().addChatMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.owners", owners));
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
                while (!town.hasPermission(this, FlagType.ENTER, player.dimension, x, y, z) && town.isPointInTown(player.dimension, x, z))
                    x++;
                x += 3;

                while(player.worldObj.getBlock(x, y, z) != Blocks.air && player.worldObj.getBlock(x, y + 1, z) != Blocks.air && y < 256)
                    y++;

                if(town.hasPermission(this, FlagType.ENTER, player.dimension, x, y, z) || !town.isPointInTown(player.dimension, x, z))
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

    @Override
    public IChatComponent toChatMessage() {
        return LocalManager.get("mytown.format.resident.short", playerName);
    }

    public boolean getFakePlayer() {
        return isFakePlayer;
    }

    public void setFakePlayer(boolean isFakePlayer) {
        this.isFakePlayer = isFakePlayer;
    }

    public static class Container extends ArrayList<Resident> implements IChatFormat {

        public Resident get(UUID uuid) {
            for (Resident res : this) {
                if (res.getUUID().equals(uuid)) {
                    return res;
                }
            }
            return null;
        }

        public Resident get(String username) {
            for (Resident res : this) {
                if (res.getPlayerName().equals(username)) {
                    return res;
                }
            }
            return null;
        }

        public void remove(Resident res) {
            /*
            for (Iterator<Plot> it = res.getCurrentTown().plotsContainer.asList().iterator(); it.hasNext(); ) {
                Plot plot = it.next();
                if (plot.ownersContainer.contains(res) && plot.ownersContainer.size() <= 1) {
                    it.remove();
                }
            }
            */
            super.remove(res);
        }

        public void remove(UUID uuid) {
            for(Iterator<Resident> it = iterator(); it.hasNext();) {
                Resident res = it.next();
                if(res.getUUID().equals(uuid)) {
                    it.remove();
                }
            }
        }

        public boolean contains(String username) {
            for (Resident res : this) {
                if (res.getPlayerName().equals(username)) {
                    return true;
                }
            }
            return false;
        }

        public boolean contains(UUID uuid) {
            for (Resident res : this) {
                if (res.getUUID().equals(uuid)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public IChatComponent toChatMessage() {
            IChatComponent root = new ChatComponentText("");

            for (Resident res : this) {
                if (root.getSiblings().size() > 0) {
                    root.appendSibling(new ChatComponentFormatted("{7|, }"));
                }
                root.appendSibling(res.toChatMessage());
            }
            return root;
        }
    }
}
