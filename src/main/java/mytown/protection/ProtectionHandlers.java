package mytown.protection;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import myessentials.entities.BlockPos;
import myessentials.entities.Volume;
import mytown.MyTown;
import mytown.new_datasource.MyTownUniverse;
import mytown.config.Config;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.thread.ThreadPlacementCheck;
import mytown.util.MyTownUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles all the protections
 */
public class ProtectionHandlers {

    public static final ProtectionHandlers instance = new ProtectionHandlers();


    public Map<TileEntity, Resident> ownedTileEntities = new HashMap<TileEntity, Resident>();

    public int activePlacementThreads = 0;
    public int maximalRange = 0;

    // ---- All the counters/tickers for preventing check every tick ----
    private int tickerTilesChecks = 20;
    private int tickerTilesChecksStart = 20;
    private int itemPickupCounter = 0;

    public Resident getOwnerForTileEntity(TileEntity te) {
        return this.ownedTileEntities.get(te);
    }

    // ---- Main ticking method ----

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent ev) {
        // TODO: Add a command to clean up the block whitelist table periodically
        if (MinecraftServer.getServer().getTickCounter() % 600 == 0) {
            for (Town town : MyTownUniverse.instance.towns)
                for (int i = 0; i < town.blockWhitelistsContainer.size(); i++) {
                    BlockWhitelist bw = town.blockWhitelistsContainer.get(i);
                    if (!ProtectionManager.isBlockWhitelistValid(bw)) {
                        MyTown.instance.datasource.deleteBlockWhitelist(bw, town);
                    }
                }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent ev) {
        if (ev.side == Side.CLIENT)
            return;
        if(ev.phase == TickEvent.Phase.END) {
            return;
        }

        //MyTown.instance.LOG.info("Tick number: " + MinecraftServer.getServer().getTickCounter());

        // Entity check
        // TODO: Rethink this system a couple million times before you come up with the best algorithm :P
        for (int i = 0; i < ev.world.loadedEntityList.size(); i++) {
            Entity entity = (Entity) ev.world.loadedEntityList.get(i);
            Town town = MyTownUtils.getTownAtPosition(entity.dimension, (int) Math.floor(entity.posX) >> 4, (int) Math.floor(entity.posZ) >> 4);
            //MyTown.instance.log.info("Checking player...");
            // Player check, every tick
            if (entity instanceof EntityPlayerMP && !(entity instanceof FakePlayer)) {
                ProtectionManager.check((EntityPlayerMP) entity);
            } else {
                // Other entity checks
                if(MinecraftServer.getServer().getTickCounter() % 20 == 0) {
                    ProtectionManager.check(entity);
                }
            }
        }

        // TileEntity check
        if(MinecraftServer.getServer().getTickCounter() % 20 == 0) {
            if (activePlacementThreads == 0) {
                for (int i = 0; i < ev.world.loadedTileEntityList.size(); i++) {
                    TileEntity te = (TileEntity) ev.world.loadedTileEntityList.get(i);
                    ProtectionManager.check(te);
                }
            }
        }
    }



    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntityEvent(AttackEntityEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        ProtectionManager.checkInteraction(ev.target, res, ev);
    }

    @SubscribeEvent
    public void onBlockPlacement(BlockEvent.PlaceEvent ev) {
        onAnyBlockPlacement(ev.player, ev);
    }

    @SubscribeEvent
    public void onMultiBlockPlacement(BlockEvent.MultiPlaceEvent ev) {
        onAnyBlockPlacement(ev.player, ev);
    }

    public void onAnyBlockPlacement(EntityPlayer player, BlockEvent.PlaceEvent ev) {
        if(ev.world.isRemote || ev.isCanceled()) {
            return;
        }

        if(player instanceof FakePlayer) {
            if(!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                ev.setCanceled(true);
            }
        } else {
            Resident res = MyTownUniverse.instance.getOrMakeResident(player);
            int range = Config.instance.placeProtectionRange.get();
            Volume placeBox = new Volume(ev.x-range, ev.y-range, ev.z-range, ev.x+range, ev.y+range, ev.z+range);

            if(!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.dimensionId, placeBox)) {
                ev.setCanceled(true);
                return;
            }

            if(ev.block instanceof ITileEntityProvider && ev.itemInHand != null) {
                TileEntity te = ((ITileEntityProvider) ev.block).createNewTileEntity(MinecraftServer.getServer().worldServerForDimension(ev.world.provider.dimensionId), ev.itemInHand.getItemDamage());
                if (te != null && ProtectionManager.isOwnable(te.getClass())) {
                    ThreadPlacementCheck thread = new ThreadPlacementCheck(res, ev.x, ev.y, ev.z, ev.world.provider.dimensionId);
                    activePlacementThreads++;
                    thread.start();
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled()) {
            return;
        }
        int x = (int) Math.floor(ev.target.posX);
        int y = (int) Math.floor(ev.target.posY);
        int z = (int) Math.floor(ev.target.posZ);

        if(ev.entityPlayer instanceof FakePlayer) {
            if(!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.target.dimension, x, y, z)) {
                ev.setCanceled(true);
            }
        } else {
            Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
            ProtectionManager.checkInteraction(ev.target, res, ev);
            if(ev.entityPlayer.getHeldItem() != null) {
                BlockPos bp = new BlockPos(x, y, z, ev.target.dimension);
                ProtectionManager.checkUsage(ev.entityPlayer.getHeldItem(), res, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, bp, -1, ev);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        if(ev.entityPlayer instanceof FakePlayer) {
            if(!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                ev.setCanceled(true);
            }
        } else {
            Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
            if(ev.entityPlayer.getHeldItem() != null) {
                ProtectionManager.checkUsage(ev.entityPlayer.getHeldItem(), res, ev.action, createBlockPos(ev), ev.face, ev);
            }
            ProtectionManager.checkBlockInteraction(res, new BlockPos(ev.x, ev.y, ev.z, ev.world.provider.dimensionId), ev.action, ev);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if(ev.world.isRemote || ev.isCanceled()) {
            return;
        }

        if(ev.getPlayer() instanceof FakePlayer) {
            if(!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                ev.setCanceled(true);
            }
        } else {
            Resident res = MyTownUniverse.instance.getOrMakeResident(ev.getPlayer());
            if(!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                ev.setCanceled(true);
                return;
            }

            if(ev.getPlayer().getHeldItem() != null) {
                ProtectionManager.checkBreakWithItem(ev.getPlayer().getHeldItem(), res, new BlockPos(ev.x, ev.y, ev.z, ev.world.provider.dimensionId), ev);
            }
        }

        if (!ev.isCanceled() && ev.block instanceof ITileEntityProvider) {
            TileEntity te = ((ITileEntityProvider) ev.block).createNewTileEntity(ev.world, ev.blockMetadata);
            if(te != null && ProtectionManager.isOwnable(te.getClass())) {
                te = ev.world.getTileEntity(ev.x, ev.y, ev.z);
                ownedTileEntities.remove(te);
                MyTown.instance.LOG.info("Removed te {}", te.toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        if(!ProtectionManager.hasPermission(res, FlagType.PICKUP, ev.item.dimension, (int) Math.floor(ev.item.posX), (int) Math.floor(ev.item.posY), (int) Math.floor(ev.item.posZ))) {
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        if(ev.source.getEntity() != null) {
            if(ev.entity instanceof EntityPlayer) {
                if (ev.source.getEntity() instanceof EntityPlayer) {
                    Resident res = MyTownUniverse.instance.getOrMakeResident(ev.source.getEntity());
                    int x = (int) Math.floor(ev.entityLiving.posX);
                    int y = (int) Math.floor(ev.entityLiving.posY);
                    int z = (int) Math.floor(ev.entityLiving.posZ);
                    if(!ProtectionManager.hasPermission(res, FlagType.PVP, ev.entityLiving.dimension, x, y, z)) {
                        ev.setCanceled(true);
                    }
                } else {
                    Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entity);
                    ProtectionManager.checkPVP(ev.source.getEntity(), res, ev);
                }
            } else {
                Resident res = ProtectionManager.getOwner(ev.source.getEntity());
                if(res != null) {
                    ProtectionManager.checkInteraction(ev.entity, res, ev);
                }
            }
        }
    }

    @SubscribeEvent
    public void onBucketFill(FillBucketEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        int x = (int) Math.floor(ev.target.blockX);
        int y = (int) Math.floor(ev.target.blockY);
        int z = (int) Math.floor(ev.target.blockZ);

        if(ev.entityPlayer instanceof FakePlayer) {
            if(!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.world.provider.dimensionId, x, y, z)) {
                ev.setCanceled(true);
            }
        } else {
            Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
            if(!ProtectionManager.hasPermission(res, FlagType.USAGE, ev.world.provider.dimensionId, x, y, z)) {
                ev.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent ev) {
        if(MyTown.instance.datasource == null) {
            return;
        }

        if (!(ev.entity instanceof EntityLiving)) {
            return;
        }

        ProtectionManager.check(ev.entity);
    }

    @SubscribeEvent
    public void specialSpawn(LivingSpawnEvent.SpecialSpawn ev) {
        if (ev.isCanceled()) return;

        ProtectionManager.check(ev.entity);
    }

    @SubscribeEvent
    public void checkSpawn(LivingSpawnEvent.CheckSpawn ev) {
        if (ev.getResult() == Event.Result.DENY) {
            return;
        }

        if(ProtectionManager.check(ev.entity)) {
            ev.setResult(Event.Result.DENY);
        }
    }

    // Fired AFTER the teleport
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent ev) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.player);
        if(!ProtectionManager.hasPermission(res, FlagType.ENTER, ev.player.dimension, (int) Math.floor(ev.player.posX), (int) Math.floor(ev.player.posY), (int) Math.floor(ev.player.posZ))) {
            // Because of badly written teleportation code by Mojang we can only send the player back to spawn. :I
            res.respawnPlayer();
        }
    }

    private BlockPos createBlockPos(PlayerInteractEvent ev) {
        Block block = ev.world.getBlock(ev.x, ev.y, ev.z);
        int x = ev.x, y = ev.y, z = ev.z;
        if(block == Blocks.air) {
            x = (int) Math.floor(ev.entityPlayer.posX);
            y = (int) Math.floor(ev.entityPlayer.posY);
            z = (int) Math.floor(ev.entityPlayer.posZ);
        }
        return new BlockPos(x, y, z, ev.world.provider.dimensionId);
    }
}
