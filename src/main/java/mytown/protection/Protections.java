package mytown.protection;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mytown.MyTown;
import mytown.api.events.TownEvent;
import mytown.config.Config;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.BlockPos;
import mytown.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidEvent;

import java.util.*;

/**
 * Created by AfterWind on 9/2/2014.
 * Class handling all the protections
 */
public class Protections {
    public Map<String, Protection> protections;

    public Map<TileEntity, Boolean> checkedTileEntities;
    public Map<Entity, Boolean> checkedEntities;

    private int ticker = 20;
    private int tickStart = 20;

    private int ticker2 = 600;
    private int tickStart2 = 600;

    public int maximalRange = 0;

    public static Protections instance = new Protections();

    public Protections() {

        MyTown.instance.log.info("Protections initializing started...");
        protections = new HashMap<String, Protection>();
        checkedTileEntities = new HashMap<TileEntity, Boolean>();
        checkedEntities = new HashMap<Entity, Boolean>();
        addProtection(new VanillaProtection(), "");
    }

    /**
     * Adds a protection with the specified mod id.
     *
     * @param prot
     * @param modid
     */
    public void addProtection(Protection prot, String modid) {
        protections.put(modid, prot);
        if(prot.getRange() > maximalRange)
            maximalRange = prot.getRange();

        if (prot.isHandlingEvents) {
            MinecraftForge.EVENT_BUS.register(prot);
        }
    }

    /**
     * Adds to the whitelist of the specified town. Used when placing blocks.
     *
     * @param te
     * @param dim
     * @param x
     * @param y
     * @param z
     * @param town
     */
    private void addToBlockWhitelist(Class<? extends TileEntity> te, int dim, int x, int y, int z, Town town) {
        for(Protection prot : protections.values()) {
            if(prot.trackedTileEntities.contains(te))
                for(FlagType flagType : prot.getFlagTypeForTile(te)) {
                    if (!town.hasBlockWhitelist(dim, x, y, z, flagType)) {
                        BlockWhitelist bw = new BlockWhitelist(dim, x, y, z, flagType);
                        DatasourceProxy.getDatasource().saveBlockWhitelist(bw, town);
                    }
                }
        }
    }

    /**
     * Removes from the whitelist. Used when breaking blocks.
     *
     * @param te
     * @param dim
     * @param x
     * @param y
     * @param z
     * @param town
     */
    private void removeFromWhitelist(Class<? extends TileEntity> te, int dim, int x, int y, int z, Town town) {
        for (Protection prot : protections.values()) {
            if (prot.trackedTileEntities.contains(te))
                for (FlagType flagType : prot.getFlagTypeForTile(te)) {
                    BlockWhitelist bw = town.getBlockWhitelist(dim, x, y, z, flagType);
                    if (bw != null) {
                        bw.delete();
                    }
                }
        }
    }
    public List<FlagType> getFlagTypesForTile(TileEntity te) {
        List<FlagType> list = new ArrayList<FlagType>();
        for(Protection prot : protections.values())
            if(prot.hasToCheckTileEntity(te))
                list.addAll(prot.getFlagTypeForTile(te.getClass()));
        return list;
    }

    public boolean checkTileEntity(TileEntity te) {
        for(Protection prot : protections.values())
            if(prot.checkTileEntity(te))
                return true;
        return false;
    }

    public boolean checkItemUsage(ItemStack stack, Resident res, BlockPos bp) {
        for(Protection prot : protections.values())
            if(prot.checkItemUsage(stack, res, bp))
                return true;
        return false;
    }

    public boolean checkActivatedBlocks(Block block) {
        for(Protection prot : protections.values()) {
            if(prot.activatedBlocks.contains(block))
                return true;
        }
        return false;
    }

    public boolean checkIsEntityHostile(Class<? extends Entity> ent) {
        for(Protection prot : protections.values()) {
            if(prot.hostileEntities.contains(ent)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBlockWhitelistValid(BlockWhitelist bw) {
        // TODO: Maybe make this better
        // Delete if the town is gone
        if(Utils.getTownAtPosition(bw.dim, bw.x >> 4, bw.z >> 4) == null)
            bw.delete();

        if(bw.getFlagType() == FlagType.activateBlocks && !checkActivatedBlocks(DimensionManager.getWorld(bw.dim).getBlock(bw.x, bw.y, bw.z)))
            return false;
        if((bw.getFlagType() == FlagType.breakBlocks || bw.getFlagType() == FlagType.placeBlocks || bw.getFlagType() == FlagType.activateBlocks || bw.getFlagType() == FlagType.useItems || bw.getFlagType() == FlagType.pumps || bw.getFlagType() == FlagType.bcPipeFlow)) {
            TileEntity te = DimensionManager.getWorld(bw.dim).getTileEntity(bw.x, bw.y, bw.z);
            if(te == null) return false;
            return getFlagTypesForTile(te).contains(bw.getFlagType());
        }
        return true;
    }


    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent ev) {
        if(ev.world.isRemote)
            return;

        // Ticker for updating the map
        if(ticker == 0) {
            //MyTown.instance.log.info("Updating check maps.");
            for (Map.Entry<Entity, Boolean> entry : checkedEntities.entrySet()) {
                entry.setValue(false);
            }

            for (Iterator<Map.Entry<TileEntity, Boolean>> it = checkedTileEntities.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<TileEntity, Boolean> entry = it.next();
                if(entry.getKey().isInvalid())
                    it.remove();
                else
                    entry.setValue(false);
            }
            ticker = MinecraftServer.getServer().worldServers.length * tickStart;
        } else {
            ticker--;
        }

        // TODO: Add a command to clean up the block whitelist table periodically periodically

        if(ticker2 == 0) {
            // Also updating the block whitelists

            for(Town town : MyTownUniverse.getInstance().getTownsMap().values()) {
                for(BlockWhitelist bw : town.getWhitelists()) {
                    if(!isBlockWhitelistValid(bw)) {
                        bw.delete();
                    }
                }
            }

            ticker2 = MinecraftServer.getServer().worldServers.length * tickStart2;
        } else {
            ticker2--;
        }


        // Entity check
        // TODO: Rethink this system a couple million times before you come up with the best algorithm :P
        for (Entity entity : (List<Entity>) ev.world.loadedEntityList) {
            // Player check, every tick
            Town town = Utils.getTownAtPosition(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);

            if(entity instanceof EntityPlayer) {
                Resident res = DatasourceProxy.getDatasource().getOrMakeResident(entity);
                ChunkCoordinates playerPos = res.getPlayer().getPlayerCoordinates();

                if(Protections.instance.maximalRange != 0) {
                    // Just firing event if there is such a case
                    List<Town> towns = Utils.getTownsInRange(res.getPlayer().dimension, playerPos.posX, playerPos.posZ, Protections.instance.maximalRange, Protections.instance.maximalRange);
                    for (Town t : towns) {
                        //Comparing it to last tick position
                        if(!Utils.getTownsInRange(res.getPlayer().dimension, (int)res.getPlayer().lastTickPosX, (int)res.getPlayer().lastTickPosZ, Protections.instance.maximalRange, Protections.instance.maximalRange).contains(t))
                            TownEvent.fire(new TownEvent.TownEnterInRangeEvent(t, res));
                    }
                }
                if(town != null) {
                    if (!town.checkPermission(res, FlagType.enter, entity.dimension, playerPos.posX, playerPos.posY, playerPos.posZ)) {
                        res.respawnPlayer();
                        res.sendMessage("§cYou have been moved because you can't access this place!");
                        MyTown.instance.log.info("Player " + entity.toString() + " was respawned!");
                    }
                }
            } else {
                // DEV:
                /*
                if(entity instanceof EntityWither) {
                    entity.getDataWatcher().getWatchableObjectInt(20);
                }
                */
                // Other entity checks
                for (Protection prot : protections.values()) {
                    if (prot.hasToCheckEntity(entity)) {
                        if ((checkedEntities.get(entity) == null || !checkedEntities.get(entity)) && prot.checkEntity(entity)) {
                            MyTown.instance.log.info("Entity " + entity.toString() + " was ATOMICALLY DISINTEGRATED!");
                            checkedEntities.remove(entity);
                            entity.setDead();
                        } else {
                            checkedEntities.put(entity, true);
                        }
                    }
                }
            }
        }

        // TileEntity check
        for (Iterator<TileEntity> it = ev.world.loadedTileEntityList.iterator(); it.hasNext(); ) {
            TileEntity te = it.next();
            //MyTown.instance.log.info("Checking tile: " + te.toString());
            for (Protection prot : protections.values()) {
                // Prechecks go here
                if (prot.hasToCheckTileEntity(te)) {
                    // Checks go here
                    if((checkedTileEntities.get(te) == null || !checkedTileEntities.get(te)) && prot.checkTileEntity(te)) {
                        Utils.dropAsEntity(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, new ItemStack(te.getBlockType(), 1, te.getBlockMetadata()));
                        //te.getBlockType().breakBlock(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, te.blockType, te.blockMetadata);
                        te.getWorldObj().setBlock(te.xCoord, te.yCoord, te.zCoord, Blocks.air);
                        checkedTileEntities.put(te, true);
                        MyTown.instance.log.info("TileEntity " + te.toString() + " was ATOMICALLY DISINTEGRATED!");
                    } else {
                        checkedTileEntities.put(te, true);
                    }
                }
            }
        }
    }

    /*
    @SubscribeEvent
    private void onPlayerTick(TickEvent.PlayerTickEvent ev) {
        // Inventory check
        // TODO: Check inventory
    }
    */

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntityEvent(AttackEntityEvent ev) {
        // TODO: More wilderness goes here
        TownBlock block = DatasourceProxy.getDatasource().getBlock(ev.target.dimension, ev.target.chunkCoordX, ev.target.chunkCoordZ);
        if(block != null) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
            Town town = block.getTown();
            if(!town.checkPermission(res, FlagType.attackEntities, ev.target.dimension, (int)ev.target.posX, (int)ev.target.posY, (int)ev.target.posZ)) {
                for (Protection prot : protections.values()) {
                    if (prot.protectedEntities.contains(ev.target.getClass())) {
                        ev.setCanceled(true);
                        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.animalCruelty"));
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public void onBlockPlacement(BlockEvent.PlaceEvent ev) {
        TownBlock tblock = DatasourceProxy.getDatasource().getBlock(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.player);

        if(tblock == null) {
            if(!Wild.getInstance().checkPermission(res, FlagType.placeBlocks)) {
                res.sendMessage(FlagType.placeBlocks.getLocalizedProtectionDenial());
                ev.setCanceled(true);
            } else {
                // If it has permission, then check nearby
                List<Town> nearbyTowns = Utils.getTownsInRange(ev.world.provider.dimensionId, ev.x, ev.z, Config.placeProtectionRange, Config.placeProtectionRange);
                for(Town t : nearbyTowns) {
                    if(!t.checkPermission(res, FlagType.placeBlocks)) {
                        res.sendMessage(FlagType.placeBlocks.getLocalizedProtectionDenial());
                        ev.setCanceled(true);
                        return;
                    }
                }
            }
        } else {
            if (!tblock.getTown().checkPermission(res, FlagType.placeBlocks, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                res.sendMessage(FlagType.placeBlocks.getLocalizedProtectionDenial());
                ev.setCanceled(true);
                return;
            } else {
                // If it has permission, then check nearby
                List<Town> nearbyTowns = Utils.getTownsInRange(ev.world.provider.dimensionId, ev.x, ev.z, Config.placeProtectionRange, Config.placeProtectionRange);
                for(Town t : nearbyTowns) {
                    if(!t.checkPermission(res, FlagType.placeBlocks)) {
                        res.sendMessage(FlagType.placeBlocks.getLocalizedProtectionDenial());
                        ev.setCanceled(true);
                        return;
                    }
                }
            }
            if (res.hasTown(tblock.getTown()) && ev.block instanceof ITileEntityProvider) {
                Class<? extends TileEntity> clsTe = ((ITileEntityProvider)ev.block).createNewTileEntity(ev.world, ev.itemInHand.getItemDamage()).getClass();
                addToBlockWhitelist(clsTe, ev.world.provider.dimensionId, ev.x, ev.y, ev.z, tblock.getTown());
            }
        }
    }

    @SubscribeEvent
    public void onMultiBlockPlacement(BlockEvent.MultiPlaceEvent ev) {
        TownBlock tblock = DatasourceProxy.getDatasource().getBlock(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.player);

        if(tblock == null) {
            if(!Wild.getInstance().checkPermission(res, FlagType.placeBlocks)) {
                res.sendMessage(FlagType.placeBlocks.getLocalizedProtectionDenial());
                ev.setCanceled(true);
            } else {
                // If it has permission, then check nearby
                List<Town> nearbyTowns = Utils.getTownsInRange(ev.world.provider.dimensionId, ev.x, ev.z, Config.placeProtectionRange, Config.placeProtectionRange);
                for(Town t : nearbyTowns) {
                    if(!t.checkPermission(res, FlagType.placeBlocks)) {
                        res.sendMessage(FlagType.placeBlocks.getLocalizedProtectionDenial());
                        ev.setCanceled(true);
                        return;
                    }
                }
            }
        } else {
            if (!tblock.getTown().checkPermission(res, FlagType.placeBlocks, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                res.sendMessage(FlagType.placeBlocks.getLocalizedProtectionDenial());
                ev.setCanceled(true);
                return;
            } else {
                // If it has permission, then check nearby
                List<Town> nearbyTowns = Utils.getTownsInRange(ev.world.provider.dimensionId, ev.x, ev.z, Config.placeProtectionRange, Config.placeProtectionRange);
                for(Town t : nearbyTowns) {
                    if(!t.checkPermission(res, FlagType.placeBlocks)) {
                        res.sendMessage(FlagType.placeBlocks.getLocalizedProtectionDenial());
                        ev.setCanceled(true);
                        return;
                    }
                }
            }
            if (res.hasTown(tblock.getTown()) && ev.block instanceof ITileEntityProvider) {
                Class<? extends TileEntity> clsTe = ((ITileEntityProvider)ev.block).createNewTileEntity(ev.world, ev.itemInHand.getItemDamage()).getClass();
                addToBlockWhitelist(clsTe, ev.world.provider.dimensionId, ev.x, ev.y, ev.z, tblock.getTown());
            }
        }
    }



    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote)
            return;

        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
        if (res == null) {
            return;
        }
        // Use this to find position if a mod is using fake players
        ChunkCoordinates playerPos = ev.entityPlayer.getPlayerCoordinates();

        ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();

        // Item usage check here
        if(currentStack != null && !(currentStack.getItem() instanceof ItemBlock)) {

            for(Protection protection : protections.values()) {
                if(protection.checkItemUsage(currentStack, res, new BlockPos(playerPos.posX, playerPos.posY, playerPos.posZ, ev.world.provider.dimensionId))) {
                    ev.setCanceled(true);
                    return;
                }
            }
        }

        // Activate and access check here
        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {

            TileEntity te = ev.world.getTileEntity(ev.x, ev.y, ev.z);

            // DEV: Developement only
            if (te != null) {
                MyTown.instance.log.info("Found tile with name " + te.toString() + " on block " + ev.world.getBlock(ev.x, ev.y, ev.z).getUnlocalizedName());
            }
            TownBlock tblock = DatasourceProxy.getDatasource().getBlock(ev.entity.dimension, ev.x >> 4, ev.z >> 4);

            // If player is trying to open an inventory
            if (te instanceof IInventory) {
                if (tblock == null) {
                    if (!Wild.getInstance().checkPermission(res, FlagType.accessBlocks)) {
                        res.sendMessage(FlagType.accessBlocks.getLocalizedProtectionDenial());
                        ev.setCanceled(true);
                    }
                } else {
                    if (tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.accessBlocks))
                        return;

                    // Checking if a player can access the block here
                    if (!tblock.getTown().checkPermission(res, FlagType.accessBlocks, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                        res.sendMessage(FlagType.accessBlocks.getLocalizedProtectionDenial());
                        ev.setCanceled(true);
                    }
                }
                // If player is trying to "activate" block
            } else {
                if (tblock == null) {
                    if (checkActivatedBlocks(ev.world.getBlock(ev.x, ev.y, ev.z))) {
                        if (!Wild.getInstance().checkPermission(res, FlagType.activateBlocks)) {
                            res.sendMessage(FlagType.activateBlocks.getLocalizedProtectionDenial());
                            ev.setCanceled(true);
                        }
                    }
                } else {
                    if (tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.activateBlocks))
                        return;

                    if (!tblock.getTown().checkPermission(res, FlagType.activateBlocks, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                        if (checkActivatedBlocks(ev.world.getBlock(ev.x, ev.y, ev.z))) {
                            res.sendMessage(FlagType.activateBlocks.getLocalizedProtectionDenial());
                            ev.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        TownBlock block = DatasourceProxy.getDatasource().getBlock(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.getPlayer());
        if (block == null) {
            if(!Wild.getInstance().checkPermission(res, FlagType.breakBlocks)) {
                res.sendMessage(FlagType.breakBlocks.getLocalizedProtectionDenial());
                ev.setCanceled(true);
            }
        } else {
            Town town = block.getTown();
            if (!town.checkPermission(res, FlagType.breakBlocks, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                res.sendMessage(FlagType.breakBlocks.getLocalizedProtectionDenial());
                ev.setCanceled(true);
                return;
            }

            if(ev.block instanceof ITileEntityProvider)
                removeFromWhitelist(((ITileEntityProvider) ev.block).createNewTileEntity(ev.world, ev.blockMetadata).getClass(), ev.world.provider.dimensionId, ev.x, ev.y, ev.z, town);
        }
    }





    /*
    For now checking in player interact event, and autonomous activator-like blocks will be checked somewhere else

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onItemUse(PlayerUseItemEvent.Start ev) {
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
        for(Protection protection : protections.values()) {
            if(protection.checkItemUsage(ev.item, res)) {
                ev.setCanceled(true);
            }
        }
    }
    */



    private int counter = 0;

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent ev) {
        TownBlock block = DatasourceProxy.getDatasource().getBlock(ev.entityPlayer.dimension, ev.entityPlayer.chunkCoordX, ev.entityPlayer.chunkCoordZ);
        if(block != null) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
            Town town = block.getTown();
            if(!town.checkPermission(res, FlagType.pickupItems, ev.item.dimension, (int)ev.item.posX, (int)ev.item.posY, (int)ev.item.posZ)) {
                if(!res.hasTown(town)) {
                    //TODO: Maybe centralise this too
                    if(counter == 0) {
                        res.sendMessage(FlagType.pickupItems.getLocalizedProtectionDenial());
                        counter = 100;
                    } else
                        counter--;
                    ev.setCanceled(true);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onFluidMoving(FluidEvent.FluidMotionEvent ev) {
        Town town = Utils.getTownAtPosition(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
        if(town != null) {
            boolean placeFlag = (Boolean)town.getValue(FlagType.placeBlocks);
            if(!placeFlag) {
                ev.setCanceled(true);
            }
        }
    }


    @SubscribeEvent
    public void onTownEnterRange(TownEvent.TownEnterInRangeEvent ev) {
        ev.resident.sendMessage("You have entered a town's range -_-' ");
    }

    @SubscribeEvent
    public void onTownEnter(TownEvent.TownEnterEvent ev) {
        ev.resident.sendMessage("You have entered a town o_o ");
    }

    /*
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void potentialSpawns(WorldEvent.PotentialSpawns ev) {
        Town town = Utils.getTownAtPosition(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);

        if(town != null) {
            String value = (String) town.getValueAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.mobs);
            if (value.equals("none")) {
                ev.setCanceled(true);
            } else if(value.equals("hostiles")){
                for (Iterator<BiomeGenBase.SpawnListEntry> it = ev.list.iterator(); it.hasNext(); ) {
                    BiomeGenBase.SpawnListEntry entry = it.next();
                    if(checkIsEntityHostile(entry.entityClass))
                        it.remove();
                }
            }
        }
    }
    */

    /*
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent ev) {
        if(DatasourceProxy.getDatasource() == null)
            return;
        Block block = DatasourceProxy.getDatasource().getBlock(ev.world.provider.dimensionId, ev.entity.chunkCoordX, ev.entity.chunkCoordZ);
        if(block == null)
            return;
        for(Protection prot : protections) {
            Flag<String> mobsFlag = block.getTown().getFlagAtCoords(ev.world.provider.dimensionId, (int) ev.entity.posX, (int) ev.entity.posY, (int) ev.entity.posZ, "mobs");
            if (mobsFlag.getValue().equals("all")) {
                if (!(ev.entity instanceof EntityPlayer)) {
                    ev.setCanceled(true);
                    return;
                }
            } else if (mobsFlag.getValue().equals("hostiles")) {
                if (prot.hostileEntities.contains(ev.entity.getClass())) {
                    ev.setCanceled(true);
                    return;
                }
            }
        }
    }
    */


}
