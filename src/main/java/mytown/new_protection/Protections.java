package mytown.new_protection;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mytown.MyTown;
import mytown.config.Config;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.*;
import mytown.util.Formatter;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;

import java.util.*;

/**
 * Created by AfterWind on 1/1/2015.
 * Handles all the protections
 */
public class Protections {

    public List<Protection> protections;

    public Map<TileEntity, Boolean> checkedTileEntities;
    public Map<Entity, Boolean> checkedEntities;

    private int tickerMap = 20;
    private int tickerMapStart = 20;

    private int tickerWhitelist = 600;
    private int tickerWhitelistStart = 600;

    private int itemPickupCounter = 0;

    public int maximalRange = 0;

    private static Protections instance;// = new Protections();
    public static Protections getInstance() {
        if(instance == null)
            instance = new Protections();
        return instance;
    }

    public Protections() {
        protections = new ArrayList<Protection>();
        checkedTileEntities = new HashMap<TileEntity, Boolean>();
        checkedEntities = new HashMap<Entity, Boolean>();
    }

    public void addProtection(Protection prot) {
        protections.add(prot);
    }


    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent ev) {
        if (ev.side == Side.CLIENT)
            return;

        if (tickerMap == 0) {
            for (Map.Entry<Entity, Boolean> entry : checkedEntities.entrySet()) {
                entry.setValue(false);
            }
            for (Iterator<Map.Entry<TileEntity, Boolean>> it = checkedTileEntities.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<TileEntity, Boolean> entry = it.next();
                if (entry.getKey().isInvalid())
                    it.remove();
                else
                    entry.setValue(false);
            }
            tickerMap = MinecraftServer.getServer().worldServers.length * tickerMapStart;
        } else {
            tickerMap--;
        }

        // TODO: Add a command to clean up the block whitelist table periodically
        if (tickerWhitelist == 0) {
            for (Town town : MyTownUniverse.getInstance().getTownsMap().values())
                for (BlockWhitelist bw : town.getWhitelists())
                    if (!ProtectionUtils.isBlockWhitelistValid(bw))
                        bw.delete();
            tickerWhitelist = MinecraftServer.getServer().worldServers.length * tickerWhitelistStart;
        } else {
            tickerWhitelist--;
        }


        // Entity check
        // TODO: Rethink this system a couple million times before you come up with the best algorithm :P
        for (Entity entity : (List<Entity>) ev.world.loadedEntityList) {
            // Player check, every tick
            Town town = MyTownUtils.getTownAtPosition(entity.dimension, (int) entity.posX >> 4, (int) entity.posZ >> 4);

            if (entity instanceof EntityPlayer) {
                Resident res = DatasourceProxy.getDatasource().getOrMakeResident(entity);
                ChunkCoordinates playerPos = res.getPlayer().getPlayerCoordinates();

                /*
                if(Protections.instance.maximalRange != 0) {
                    // Just firing event if there is such a case
                    List<Town> towns = Utils.getTownsInRange(res.getPlayer().dimension, playerPos.posX, playerPos.posZ, Protections.instance.maximalRange, Protections.instance.maximalRange);
                    for (Town t : towns) {
                        //Comparing it to last tick position
                        if(!Utils.getTownsInRange(res.getPlayer().dimension, (int)res.getPlayer().lastTickPosX, (int)res.getPlayer().lastTickPosZ, Protections.instance.maximalRange, Protections.instance.maximalRange).contains(t))
                            TownEvent.fire(new TownEvent.TownEnterInRangeEvent(t, res));
                    }
                }
                */

                if (town != null) {
                    if (!town.checkPermission(res, FlagType.enter, entity.dimension, playerPos.posX, playerPos.posY, playerPos.posZ)) {
                        res.protectionDenial("§cYou have been moved because you can't access this place!", mytown.util.Formatter.formatOwnersToString(town.getOwnersAtPosition(entity.dimension, playerPos.posX, playerPos.posY, playerPos.posZ)));
                        res.respawnPlayer();
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
                for (Protection prot : protections) {
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

        // TileEntity check
        for (TileEntity te : (Iterable<TileEntity>) ev.world.loadedTileEntityList) {
            //MyTown.instance.log.info("Checking tile: " + te.toString());
            for (Protection prot : protections) {
                if ((checkedTileEntities.get(te) == null || !checkedTileEntities.get(te)) && prot.checkTileEntity(te)) {
                    MyTownUtils.dropAsEntity(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, new ItemStack(te.getBlockType(), 1, te.getBlockMetadata()));
                    //te.getBlockType().breakBlock(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, te.blockType, te.blockMetadata);
                    te.getWorldObj().setBlock(te.xCoord, te.yCoord, te.zCoord, Blocks.air);
                    checkedTileEntities.put(te, true);
                    MyTown.instance.log.info("TileEntity " + te.toString() + " was ATOMICALLY DISINTEGRATED!");
                } else {
                    checkedTileEntities.put(te, true);
                }
            }
        }

        /*
        if(!errored) {
            try {
                //MyTown.instance.log.info("Checking...");
                    Field field = WorldServer.class.getDeclaredField("pendingTickListEntriesThisTick");
                    field.setAccessible(true);

                    List<NextTickListEntry> list = (List<NextTickListEntry>) field.get(ev.world);
                    if (list != null) {
                        for (Iterator<NextTickListEntry> it = list.iterator(); it.hasNext(); ) {
                            NextTickListEntry entry = it.next();
                            Town town = Utils.getTownAtPosition(ev.world.provider.dimensionId, entry.xCoord >> 4, entry.zCoord >> 4);
                            if(town != null) {
                                boolean placeFlag = (Boolean)town.getValueAtCoords(ev.world.provider.dimensionId, entry.xCoord, entry.yCoord, entry.zCoord, FlagType.placeBlocks);
                                if (!placeFlag && entry.func_151351_a() instanceof IFluidBlock) {
                                    it.remove();
                                }
                            }
                            MyTown.instance.log.info(entry.func_151351_a().getUnlocalizedName() + " at (" + entry.xCoord + ", " + entry.xCoord + ", " + entry.xCoord + ")");
                        }
                    } else {
                        MyTown.instance.log.info("List is null!");
                    }
            } catch (Exception e) {
                MyTown.instance.log.error("An error occurred when checking tick updates.");
                e.printStackTrace();
                errored = true;
            }
        }
        */
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntityEvent(AttackEntityEvent ev) {
        // TODO: More wilderness goes here
        Town town = MyTownUtils.getTownAtPosition(ev.target.dimension, ev.target.chunkCoordX, ev.target.chunkCoordZ);
        if (town != null) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
            if (!town.checkPermission(res, FlagType.attackEntities, ev.target.dimension, (int) ev.target.posX, (int) ev.target.posY, (int) ev.target.posZ)) {
                for (Protection prot : protections) {
                    if (prot.isEntityProtected(ev.target.getClass())) {
                        ev.setCanceled(true);
                        res.protectionDenial(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.animalCruelty"), Formatter.formatOwnersToString(town.getOwnersAtPosition(ev.target.dimension, (int) ev.target.posX, (int) ev.target.posY, (int) ev.target.posZ)));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlacement(BlockEvent.PlaceEvent ev) {
        if(onAnyBlockPlacement(ev.player, ev.itemInHand, ev.placedBlock, ev.world.provider.dimensionId, ev.x, ev.y, ev.z))
            ev.setCanceled(true);
    }

    @SubscribeEvent
    public void onMultiBlockPlacement(BlockEvent.MultiPlaceEvent ev) {
        if(onAnyBlockPlacement(ev.player, ev.itemInHand, ev.placedBlock, ev.world.provider.dimensionId, ev.x, ev.y, ev.z))
            ev.setCanceled(true);
    }

    public boolean onAnyBlockPlacement(EntityPlayer player, ItemStack itemInHand, Block block, int dimensionId, int x, int y, int z) {
        TownBlock tblock = DatasourceProxy.getDatasource().getBlock(dimensionId, x >> 4, z >> 4);
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(player);

        if (tblock == null) {
            if (!Wild.getInstance().checkPermission(res, FlagType.modifyBlocks)) {
                res.sendMessage(FlagType.modifyBlocks.getLocalizedProtectionDenial());
                return true;
            } else {
                // If it has permission, then check nearby
                List<Town> nearbyTowns = MyTownUtils.getTownsInRange(dimensionId, x, z, Config.placeProtectionRange, Config.placeProtectionRange);
                for (Town t : nearbyTowns) {
                    if (!t.checkPermission(res, FlagType.modifyBlocks)) {
                        res.protectionDenial(FlagType.modifyBlocks.getLocalizedProtectionDenial(), Formatter.formatOwnerToString(t.getMayor()));
                        return true;
                    }
                }
            }
        } else {
            if (!tblock.getTown().checkPermission(res, FlagType.modifyBlocks, dimensionId, x, y, z)) {
                res.protectionDenial(FlagType.modifyBlocks.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(tblock.getTown().getOwnersAtPosition(dimensionId, x, y, z)));
                return true;
            } else {
                // If it has permission, then check nearby
                List<Town> nearbyTowns = MyTownUtils.getTownsInRange(dimensionId, x, z, Config.placeProtectionRange, Config.placeProtectionRange);
                for (Town t : nearbyTowns) {
                    if (tblock.getTown() != t && !t.checkPermission(res, FlagType.modifyBlocks)) {
                        res.protectionDenial(FlagType.modifyBlocks.getLocalizedProtectionDenial(), Formatter.formatOwnerToString(t.getMayor()));
                        return true;
                    }
                }
            }
            if (res.hasTown(tblock.getTown()) && block instanceof ITileEntityProvider) {
                TileEntity te = ((ITileEntityProvider) block).createNewTileEntity(DimensionManager.getWorld(dimensionId), itemInHand.getItemDamage());
                if (te != null) {
                    Class<? extends TileEntity> clsTe = te.getClass();
                    ProtectionUtils.addToBlockWhitelist(clsTe, dimensionId, x, y, z, tblock.getTown());
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent ev) {
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
        ItemStack currStack = ev.entityPlayer.getHeldItem();
        if (currStack != null) {
            for (Protection prot : protections) {
                if (prot.checkItemUsage(currStack, res, new BlockPos((int) ev.target.posX, (int) ev.target.posY, (int) ev.target.posZ, ev.entityPlayer.worldObj.provider.dimensionId))) {
                    ev.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote)
            return;

        /*
        // TODO: Maybe revise it
        // If it's an entity then check is gonna occur on that entity's position
        MovingObjectPosition obj = Utils.tracePath(ev.world, (float)ev.entityPlayer.posX, (float)ev.entityPlayer.posY + ev.entityPlayer.eyeHeight, (float)ev.entityPlayer.posZ, (float)ev.entityPlayer.getLookVec().xCoord, (float)ev.entityPlayer.getLookVec().yCoord, (float)ev.entityPlayer.getLookVec().zCoord, 0.2F, null);
        //ev.entityPlayer.getLookVec()
        if(obj != null && obj.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            MyTown.instance.log.info("Found entity at: " + obj.blockX + ", " + obj.blockY + ", " + obj.blockZ);

            x = obj.blockX;
            y = obj.blockY;
            z = obj.blockZ;
        }
        */

        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
        if (res == null) {
            return;
        }
        // Use this to find position if a mod is using fake players
        ChunkCoordinates playerPos = ev.entityPlayer.getPlayerCoordinates();

        int x = ev.x, y = ev.y, z = ev.z;
        if(ev.world.getBlock(x, y, z) == Blocks.air) {
            x = playerPos.posX;
            y = playerPos.posY;
            z = playerPos.posZ;
            //MyTown.instance.log.info("Position changed to : " + x + ", " + y + ", " + z);
        }

        ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();

        // Item usage check here
        if (currentStack != null && !(currentStack.getItem() instanceof ItemBlock)) {
            //MyTown.instance.log.info("Item usage position: " + x + ", " + y + ", " + z);
            for (Protection protection : protections) {
                if (protection.checkItemUsage(currentStack, res, new BlockPos(x, y, z, ev.world.provider.dimensionId))) {
                    ev.setCanceled(true);
                    return;
                }
            }
        }


        // Activate and access check here
        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {

            TileEntity te = ev.world.getTileEntity(x, y, z);

            // DEV: Developement only
                /*
                if (te != null) {
                    MyTown.instance.log.info("Found tile with name " + te.toString() + " on block " + ev.world.getBlock(x, y, z).getUnlocalizedName());
                }
                */
            TownBlock tblock = DatasourceProxy.getDatasource().getBlock(ev.entity.dimension, x >> 4, z >> 4);

            // If player is trying to open an inventory
            if (te instanceof IInventory) {
                if (tblock == null) {
                    if (!Wild.getInstance().checkPermission(res, FlagType.accessBlocks)) {
                        res.sendMessage(FlagType.accessBlocks.getLocalizedProtectionDenial());
                        ev.setCanceled(true);
                    }
                } else {
                    if (tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, x, y, z, FlagType.accessBlocks))
                        return;

                    // Checking if a player can access the block here
                    if (!tblock.getTown().checkPermission(res, FlagType.accessBlocks, ev.world.provider.dimensionId, x, y, z)) {
                        res.protectionDenial(FlagType.accessBlocks.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(tblock.getTown().getOwnersAtPosition(ev.world.provider.dimensionId, x, y, z)));
                        ev.setCanceled(true);
                    }
                }
                // If player is trying to "activate" block
            } else {
                if (tblock == null) {
                    if (ProtectionUtils.checkActivatedBlocks(ev.world.getBlock(x, y, z), ev.world.getBlockMetadata(x, y, z))) {
                        if (!Wild.getInstance().checkPermission(res, FlagType.activateBlocks)) {
                            res.sendMessage(FlagType.activateBlocks.getLocalizedProtectionDenial());
                            ev.setCanceled(true);
                        }
                    }
                } else {
                    if (tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, x, y, z, FlagType.activateBlocks))
                        return;

                    if (!tblock.getTown().checkPermission(res, FlagType.activateBlocks, ev.world.provider.dimensionId, x, y, z)) {
                        if (ProtectionUtils.checkActivatedBlocks(ev.world.getBlock(x, y, z), ev.world.getBlockMetadata(x, y, z))) {
                            res.protectionDenial(FlagType.activateBlocks.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(tblock.getTown().getOwnersAtPosition(ev.world.provider.dimensionId, x, y, z)));
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
            if (!Wild.getInstance().checkPermission(res, FlagType.modifyBlocks)) {
                res.sendMessage(FlagType.modifyBlocks.getLocalizedProtectionDenial());
                ev.setCanceled(true);
            }
        } else {
            Town town = block.getTown();
            if (!town.checkPermission(res, FlagType.modifyBlocks, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                res.protectionDenial(FlagType.modifyBlocks.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(town.getOwnersAtPosition(ev.world.provider.dimensionId, ev.x, ev.y, ev.z)));
                ev.setCanceled(true);
                return;
            }

            if (ev.block instanceof ITileEntityProvider) {
                TileEntity te = ((ITileEntityProvider) ev.block).createNewTileEntity(ev.world, ev.blockMetadata);
                if(te != null)
                    ProtectionUtils.removeFromWhitelist(te.getClass(), ev.world.provider.dimensionId, ev.x, ev.y, ev.z, town);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent ev) {
        TownBlock block = DatasourceProxy.getDatasource().getBlock(ev.entityPlayer.dimension, ev.entityPlayer.chunkCoordX, ev.entityPlayer.chunkCoordZ);
        if (block != null) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
            Town town = block.getTown();
            if (!town.checkPermission(res, FlagType.pickupItems, ev.item.dimension, (int) ev.item.posX, (int) ev.item.posY, (int) ev.item.posZ)) {
                if (!res.hasTown(town)) {
                    //TODO: Maybe centralise this too
                    if (itemPickupCounter == 0) {
                        res.protectionDenial(FlagType.pickupItems.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(town.getOwnersAtPosition(ev.item.dimension, (int) ev.item.posX, (int) ev.item.posY, (int) ev.item.posZ)));
                        itemPickupCounter = 100;
                    } else
                        itemPickupCounter--;
                    ev.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent ev) {
        if(ev.entityLiving instanceof EntityPlayer && ev.source.getSourceOfDamage() instanceof EntityPlayer) {
            TownBlock block = DatasourceProxy.getDatasource().getBlock(ev.entityLiving.dimension, ev.entityLiving.chunkCoordX, ev.entityLiving.chunkCoordZ);
            if(block != null) {
                Boolean pvpValue = (Boolean)block.getTown().getValueAtCoords(ev.entityLiving.dimension, (int)ev.entityLiving.posX, (int)ev.entityLiving.posY, (int)ev.entityLiving.posZ, FlagType.pvp);
                if(!pvpValue) {
                    ev.setCanceled(true);
                    Resident res = DatasourceProxy.getDatasource().getOrMakeResident((EntityPlayer)ev.source.getSourceOfDamage());
                    res.protectionDenial(FlagType.pvp.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(block.getTown().getOwnersAtPosition(ev.entityLiving.dimension, (int)ev.entityLiving.posX, (int)ev.entityLiving.posY, (int)ev.entityLiving.posZ)));
                }
            }
        }
    }

    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Start ev) {
        List<ChunkPos> chunks = MyTownUtils.getChunksInBox((int)(ev.explosion.explosionX - ev.explosion.explosionSize), (int)(ev.explosion.explosionZ - ev.explosion.explosionSize), (int)(ev.explosion.explosionX + ev.explosion.explosionSize), (int)(ev.explosion.explosionZ + ev.explosion.explosionSize));
        for(ChunkPos chunk : chunks) {
            Town town = MyTownUtils.getTownAtPosition(ev.world.provider.dimensionId, chunk.getX(), chunk.getZ());
            if(town != null) {
                boolean explosionValue = (Boolean) town.getValue(FlagType.explosions);
                if (!explosionValue) {
                    ev.setCanceled(true);
                    town.notifyEveryone(FlagType.explosions.getLocalizedTownNotification());
                    return;
                }
            }
        }
    }
}
