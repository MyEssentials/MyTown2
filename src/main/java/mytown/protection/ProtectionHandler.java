package mytown.protection;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import myessentials.entities.BlockPos;
import mytown.MyTown;
import mytown.config.Config;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.ItemType;
import mytown.proxies.DatasourceProxy;
import mytown.thread.ThreadPlacementCheck;
import mytown.util.Formatter;
import mytown.util.MyTownUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all the protections
 */
public class ProtectionHandler {

    public static final ProtectionHandler instance = new ProtectionHandler();


    public Map<TileEntity, Resident> ownedTileEntities = new HashMap<TileEntity, Resident>();

    public int activePlacementThreads = 0;

    public int maximalRange = 0;

    private List<Protection> protectionList = new ArrayList<Protection>();

    // ---- All the counters/tickers for preventing check every tick ----
    private int tickerTilesChecks = 20;
    private int tickerTilesChecksStart = 20;
    private int itemPickupCounter = 0;

    // ---- Utility methods for accessing protections ----

    public void addProtection(Protection prot) { protectionList.add(prot); }
    public void removeProtection(Protection prot) { protectionList.remove(prot); }
    public List<Protection> getProtectionList() { return this.protectionList; }

    public void reset() {
        protectionList = new ArrayList<Protection>();
    }

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
                    if (!ProtectionUtils.isBlockWhitelistValid(bw)) {
                        DatasourceProxy.getDatasource().deleteBlockWhitelist(bw, town);
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
                ProtectionUtils.check((EntityPlayerMP) entity);
            } else {
                // Other entity checks
                if(MinecraftServer.getServer().getTickCounter() % 20 == 0) {
                    ProtectionUtils.check(entity);
                }
            }
        }

        // TileEntity check
        if(MinecraftServer.getServer().getTickCounter() % 20 == 0) {
            if (activePlacementThreads == 0) {
                for (int i = 0; i < ev.world.loadedTileEntityList.size(); i++) {
                    TileEntity te = (TileEntity) ev.world.loadedTileEntityList.get(i);
                    ProtectionUtils.check(te);
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
        ProtectionUtils.checkInteraction(ev.target, res, ev);
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

        Resident res = MyTownUniverse.instance.getOrMakeResident(player);

        if(!ProtectionUtils.hasPermission(res, FlagType.MODIFY, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
            ev.setCanceled(true);
            return;
        }

        if(ev.block instanceof ITileEntityProvider && ev.itemInHand != null) {
            TileEntity te = ((ITileEntityProvider) ev.block).createNewTileEntity(MinecraftServer.getServer().worldServerForDimension(ev.world.provider.dimensionId), ev.itemInHand.getItemDamage());
            if (te != null && ProtectionUtils.isOwnable(te.getClass())) {
                ThreadPlacementCheck thread = new ThreadPlacementCheck(res, ev.x, ev.y, ev.z, ev.world.provider.dimensionId);
                activePlacementThreads++;
                thread.start();
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        ProtectionUtils.checkInteraction(ev.target, res, ev);
        BlockPos bp = new BlockPos((int) Math.floor(ev.target.posX), (int) Math.floor(ev.target.posY), (int) Math.floor(ev.target.posZ), ev.target.dimension);
        ProtectionUtils.checkUsage(ev.entityPlayer.getHeldItem(), res, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, bp, -1, ev);
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        ProtectionUtils.checkUsage(ev.entityPlayer.getHeldItem(), res, ev.action, createBlockPos(ev), ev.face, ev);
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if(ev.world.isRemote || ev.isCanceled()) {
            return;
        }

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.getPlayer());
        if(!ProtectionUtils.hasPermission(res, FlagType.MODIFY, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
            ev.setCanceled(true);
            return;
        }

        ProtectionUtils.checkBreakWithItem(ev.getPlayer().getHeldItem(), res, new BlockPos(ev.x, ev.y, ev.z, ev.world.provider.dimensionId), ev);

        if (!ev.isCanceled() && ev.block instanceof ITileEntityProvider) {
            TileEntity te = ((ITileEntityProvider) ev.block).createNewTileEntity(ev.world, ev.blockMetadata);
            if(te != null && ProtectionUtils.isOwnable(te.getClass())) {
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
        if(!ProtectionUtils.hasPermission(res, FlagType.PICKUP, ev.item.dimension, (int) Math.floor(ev.item.posX), (int) Math.floor(ev.item.posY), (int) Math.floor(ev.item.posZ))) {
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        if(ev.entity instanceof EntityPlayer) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entity);
            ProtectionUtils.checkPVP(ev.entity, res, ev);
        } else {
            Resident res = ProtectionUtils.getOwner(ev.source.getEntity());
            if(res != null) {
                ProtectionUtils.checkInteraction(ev.entity, res, ev);
            }
        }
    }

    @SubscribeEvent
    public void onBucketFill(FillBucketEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled()) {
            return;
        }

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        if(ProtectionUtils.hasPermission(res, FlagType.USAGE, ev.world.provider.dimensionId, ev.target.blockX, ev.target.blockY, ev.target.blockZ)) {
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent ev) {
        if(DatasourceProxy.getDatasource() == null) {
            return;
        }

        if (!(ev.entity instanceof EntityLiving)) {
            return;
        }

        ProtectionUtils.check(ev.entity);
    }

    @SubscribeEvent
    public void specialSpawn(LivingSpawnEvent.SpecialSpawn ev) {
        if (ev.isCanceled()) return;

        ProtectionUtils.check(ev.entity);
    }

    @SubscribeEvent
    public void checkSpawn(LivingSpawnEvent.CheckSpawn ev) {
        if (ev.getResult() == Event.Result.DENY) {
            return;
        }

        if(ProtectionUtils.check(ev.entity)) {
            ev.setResult(Event.Result.DENY);
        }
    }

    // Fired AFTER the teleport
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent ev) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.player);
        if(ProtectionUtils.hasPermission(res, FlagType.ENTER, ev.player.dimension, (int) Math.floor(ev.player.posX), (int) Math.floor(ev.player.posY), (int) Math.floor(ev.player.posZ))) {
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
