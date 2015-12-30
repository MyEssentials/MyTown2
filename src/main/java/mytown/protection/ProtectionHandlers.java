package mytown.protection;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import myessentials.entities.BlockPos;
import myessentials.entities.Volume;
import myessentials.event.AE2PartPlaceEvent;
import myessentials.event.BlockTrampleEvent;
import myessentials.event.ModifyBiomeEvent;
import myessentials.event.ModifyBlockEvent;
import myessentials.event.ProjectileImpactEvent;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
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
        int loadedEntityListSize = ev.world.loadedEntityList.size();
        for (int i = 0; i < loadedEntityListSize; i++) {
            Object item = ev.world.loadedEntityList.get(i);
            if (item == null) continue;
            Entity entity = (Entity) item;
            Town town = MyTownUtils.getTownAtPosition(entity.dimension, (int) Math.floor(entity.posX) >> 4, (int) Math.floor(entity.posZ) >> 4);
            //MyTown.instance.log.info("Checking player...");
            // Player check, every tick
            if (entity instanceof EntityPlayerMP && !(entity instanceof FakePlayer)) {
                ProtectionManager.check((EntityPlayerMP) entity);
            } else {
                // Other entity checks
                if(MinecraftServer.getServer().getTickCounter() % 20 == 5) {
                    ProtectionManager.checkExist(entity, false);
                }
            }
        }

        // TileEntity check
        if(MinecraftServer.getServer().getTickCounter() % 20 == 15) {
            if (activePlacementThreads == 0) {
                int loadedTileEntityListSize = ev.world.loadedTileEntityList.size();
                for (int i = 0; i < loadedTileEntityListSize; i++) {
                    Object item = ev.world.loadedTileEntityList.get(i);
                    if (item == null) continue;
                    TileEntity te = (TileEntity) item;
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

            if (!MyTownUniverse.instance.blocks.contains(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4)) {
                int range = Config.instance.placeProtectionRange.get();
                Volume placeBox = new Volume(ev.x-range, ev.y-range, ev.z-range, ev.x+range, ev.y+range, ev.z+range);

                if(!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.dimensionId, placeBox)) {
                    ev.setCanceled(true);
                    return;
                }
            } else {
                if(!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                    ev.setCanceled(true);
                    return;
                }
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
            if(!ev.isCanceled() && ev.entityPlayer.getHeldItem() != null) {
                BlockPos bp = new BlockPos(x, y, z, ev.target.dimension);
                ProtectionManager.checkUsage(ev.entityPlayer.getHeldItem(), res, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, bp, -1, ev);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onProjectileImpact(ProjectileImpactEvent ev) {
        if (ev.entity.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        EntityLivingBase firingEntity = ev.firingEntity;
        Resident owner = null;
        if (firingEntity instanceof EntityPlayerMP && !(firingEntity instanceof FakePlayer)) {
            owner = MyTownUniverse.instance.getOrMakeResident(firingEntity);
        }
        ProtectionManager.checkImpact(ev.entity, owner, ev.movingObjectPosition, ev);
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
            if (!ev.isCanceled()) {
                ProtectionManager.checkBlockInteraction(res, new BlockPos(ev.x, ev.y, ev.z, ev.world.provider.dimensionId), ev.action, ev);
            }
        }

        // Some things (Autonomous Activator) only care about these. So always deny them if the event is canceled.
        if (ev.isCanceled()) {
            ev.useBlock = Event.Result.DENY;
            ev.useItem = Event.Result.DENY;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAE2PartPlace(AE2PartPlaceEvent ev) {
        if (ev.world.isRemote || ev.isCanceled()) {
            return;
        }

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.player);
        if(ev.player.getHeldItem() != null) {
            ProtectionManager.checkUsage(ev.player.getHeldItem(), res, Action.RIGHT_CLICK_BLOCK, new BlockPos(ev.x, ev.y, ev.z, ev.world.provider.dimensionId), ev.face, ev);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onModifyBlock(ModifyBlockEvent ev) {
        if (ev.world.isRemote || ev.isCanceled() || Config.instance.fireSpreadInTowns.get()) {
            return;
        }

        if(!ProtectionManager.getFlagValueAtLocation(FlagType.MODIFY, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
            ev.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockTrample(BlockTrampleEvent ev) {
        if(ev.world.isRemote || ev.isCanceled())
            return;

        Entity entity = ev.entity;
        Resident res = null;

        if(!(entity instanceof EntityPlayer)) {
            // Protect from players ridding any entity
            if(entity.riddenByEntity != null && (entity.riddenByEntity instanceof EntityPlayer))
                entity = entity.riddenByEntity;
            // Protect from players jumping and leaving the horse in mid-air
            else
                res = ProtectionManager.getOwner(entity);
        }

        // Fake players are special
        if(entity instanceof FakePlayer) {
            if(!ProtectionManager.getFlagValueAtLocation(FlagType.FAKERS, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                ev.setCanceled(true);
            }
        } else {
            // Will be null if we didn't find the player responsible for this trampling
            if(res == null) {
                res = MyTownUniverse.instance.getOrMakeResident(entity);
                // Will be null if it wasn't caused by a known player
                if(res == null)
                    return;
            }

            // Trampling crops will break them and will modify the terrain
            if (!ProtectionManager.checkBlockBreak(ev.block)) {
                if(!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                    ev.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onModifyBiome(ModifyBiomeEvent ev) {
        if (ev.world.isRemote || ev.isCanceled() || Config.instance.taintSpreadInTowns.get()) {
            return;
        }

        if(MyTownUniverse.instance.blocks.contains(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4)) {
            ev.setCanceled(true);
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

            if (!MyTownUniverse.instance.blocks.contains(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4)) {
                int range = Config.instance.placeProtectionRange.get();
                Volume breakBox = new Volume(ev.x-range, ev.y-range, ev.z-range, ev.x+range, ev.y+range, ev.z+range);

                if(!ProtectionManager.checkBlockBreak(ev.block)) {
                    if(!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.dimensionId, breakBox)) {
                        ev.setCanceled(true);
                        return;
                    }
                }

            } else {
                if (!ProtectionManager.checkBlockBreak(ev.block)) {
                    if(!ProtectionManager.hasPermission(res, FlagType.MODIFY, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                        ev.setCanceled(true);
                        return;
                    }
                }
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
                    // Player vs Player
                    int x = (int) Math.floor(ev.entityLiving.posX);
                    int y = (int) Math.floor(ev.entityLiving.posY);
                    int z = (int) Math.floor(ev.entityLiving.posZ);
                    if(!ProtectionManager.getFlagValueAtLocation(FlagType.PVP, ev.entityLiving.dimension, x, y, z)) {
                        ev.setCanceled(true);
                    }
                } else {
                    // Entity vs Player (Check for Player owned Entity)
                    Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entity);
                    ProtectionManager.checkPVP(ev.source.getEntity(), res, ev);
                }
            } else {
                if (ev.source.getEntity() instanceof EntityPlayer) {
                    // Player vs Living Entity
                    Resident res = MyTownUniverse.instance.getOrMakeResident(ev.source.getEntity());
                    ProtectionManager.checkInteraction(ev.entity, res, ev);
                } else {
                    // Entity vs Living Entity
                }
            }
        } else {
            // Non-Entity Damage
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

        ProtectionManager.checkExist(ev.entity, true);
    }

    @SubscribeEvent
    public void specialSpawn(LivingSpawnEvent.SpecialSpawn ev) {
        if (ev.isCanceled()) return;

        ProtectionManager.checkExist(ev.entity, true);
    }

    @SubscribeEvent
    public void checkSpawn(LivingSpawnEvent.CheckSpawn ev) {
        if (ev.getResult() == Event.Result.DENY) {
            return;
        }

        if(ProtectionManager.checkExist(ev.entity, true)) {
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
        int x, y, z;

        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            x = (int) Math.floor(ev.entityPlayer.posX);
            y = (int) Math.floor(ev.entityPlayer.posY);
            z = (int) Math.floor(ev.entityPlayer.posZ);
        } else {
            x = ev.x;
            y = ev.y;
            z = ev.z;
        }
        return new BlockPos(x, y, z, ev.world.provider.dimensionId);
    }
}
