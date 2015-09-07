package mytown.protection;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import myessentials.entities.BlockPos;
import myessentials.entities.EntityPos;
import myessentials.utils.PlayerUtils;
import myessentials.utils.WorldUtils;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
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
public class Protections {

    public static final Protections instance = new Protections();

    public Map<EntityPlayer, EntityPos> lastTickPlayerPos = new HashMap<EntityPlayer, EntityPos>();
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
                checkPlayer((EntityPlayerMP) entity, town);
            } else {
                // Other entity checks
                if(MinecraftServer.getServer().getTickCounter() % 20 == 0) {
                    if(town != null && entity instanceof EntityLiving && "none".equals(town.getValueAtCoords(entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ), FlagType.MOBS))) {
                        entity.setDead();
                    }
                    // Don't check twice
                    if(!entity.isDead) {
                        for (Protection prot : protectionList) {
                            if (prot.isEntityTracked(entity.getClass()) && prot.checkEntity(entity)) {
                                entity.setDead();
                                break;
                            }
                        }
                    }
                }
            }
        }

        // TileEntity check
        if(MinecraftServer.getServer().getTickCounter() % 20 == 0) {
            if (activePlacementThreads == 0) {
                for (int i = 0; i < ev.world.loadedTileEntityList.size(); i++) {
                    TileEntity te = (TileEntity) ev.world.loadedTileEntityList.get(i);
                    for (Protection prot : protectionList) {
                        if (prot.isTileTracked(te.getClass()) && prot.checkTileEntity(te)) {
                            ItemStack itemStack = new ItemStack(te.getBlockType(), 1, te.getBlockMetadata());
                            NBTTagCompound nbt = new NBTTagCompound();
                            te.writeToNBT(nbt);
                            itemStack.setTagCompound(nbt);
                            WorldUtils.dropAsEntity(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, itemStack);
                            te.getWorldObj().setBlock(te.xCoord, te.yCoord, te.zCoord, Blocks.air);
                            te.invalidate();
                            MyTown.instance.LOG.info("TileEntity {} was ATOMICALLY DISINTEGRATED!", te.toString());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void checkPlayer(EntityPlayerMP player, Town town) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(player);
        EntityPos lastTickPos = lastTickPlayerPos.get(player);
        
        if (res == null) {
        	return;
        }

        if (town != null && !town.hasPermission(res, FlagType.ENTER, false, player.dimension, (int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ))) {
            res.protectionDenial(FlagType.ENTER, town.formatOwners(player.dimension, (int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ)));
            if(lastTickPos == null)
                res.knockbackPlayerToBorder(town);
            else
            if(lastTickPos.getX() != player.posX || lastTickPos.getY() != player.posY || lastTickPos.getZ() != player.posZ || lastTickPos.getDim() != player.dimension) {
                PlayerUtils.teleport(player, lastTickPos.getDim(), lastTickPos.getX(), lastTickPos.getY(), lastTickPos.getZ());
            }
        } else {
            // Chunk changed
            // TODO: Refactor so that it's understandable
            if(lastTickPos != null && (((int) Math.floor(lastTickPos.getX())) >> 4 != (int)(Math.floor(player.posX)) >> 4 || ((int) Math.floor(lastTickPos.getZ())) >> 4 != (int)(Math.floor(player.posZ)) >> 4)) {
                if (lastTickPos.getDim() == player.dimension) {
                    res.checkLocation(((int) Math.floor(lastTickPos.getX())) >> 4, ((int) Math.floor(lastTickPos.getZ())) >> 4,
                            ((int) Math.floor(player.posX)) >> 4, ((int) (Math.floor(player.posZ))) >> 4, player.dimension);
                } else {
                    res.checkLocationOnDimensionChanged((int) (Math.floor(player.posX)), (int) (Math.floor(player.posZ)), player.dimension);
                }
            }

            if(lastTickPos != null && town != null) {
                Plot currentPlot = town.plotsContainer.get(player.dimension, (int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ));
                Plot lastTickPlot = town.plotsContainer.get(lastTickPos.getDim(), (int) Math.floor(lastTickPos.getX()), (int) Math.floor(lastTickPos.getY()), (int) Math.floor(lastTickPos.getZ()));

                if(currentPlot != null && (lastTickPlot == null || currentPlot != lastTickPlot)) {
                    res.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.plot.enter", currentPlot.getName()));
                } else if(currentPlot == null && lastTickPlot != null) {
                    res.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.plot.enter", EnumChatFormatting.RED + "Unassigned"));
                }
            }

            lastTickPlayerPos.put(player, new EntityPos(player.posX, player.posY, player.posZ, player.dimension));
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntityEvent(AttackEntityEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled())
            return;
        TownBlock block = MyTownUniverse.instance.blocks.get(ev.target.dimension, ev.target.chunkCoordX, ev.target.chunkCoordZ);
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        if (block == null) {
            // Bypass for fakePlayers
            if(ev.entityPlayer instanceof FakePlayer && (Boolean)Wild.instance.flagsContainer.getValue(FlagType.FAKERS))
                return;

            // Allow pvp on players.
            if(ev.target instanceof EntityPlayer && (Boolean)Wild.instance.flagsContainer.getValue(FlagType.PVP))
                return;

            if(!Wild.instance.hasPermission(res, FlagType.PVE, false)) {
                for(Protection prot : protectionList) {
                    if(prot.isEntityProtected(ev.target.getClass())) {
                        ev.setCanceled(true);
                        res.sendMessage(FlagType.PVE.getLocalizedProtectionDenial());
                        return;
                    }
                }
            }
        } else {
            // Bypass for fakePlayers
            if(ev.entityPlayer instanceof FakePlayer && (Boolean)block.getTown().getValueAtCoords(ev.target.dimension, (int) Math.floor(ev.target.posX), (int) Math.floor(ev.target.posY), (int)Math.floor(ev.target.posZ), FlagType.FAKERS))
                return;

            // Allow pvp on players.
            if(ev.target instanceof EntityPlayer && (Boolean)block.getTown().getValueAtCoords(ev.target.dimension, (int) Math.floor(ev.target.posX), (int) Math.floor(ev.target.posY), (int) Math.floor(ev.target.posZ), FlagType.PVP))
                return;

            if (!block.getTown().hasPermission(res, FlagType.PVE, false, ev.target.dimension, (int) Math.floor(ev.target.posX), (int) Math.floor(ev.target.posY), (int) Math.floor(ev.target.posZ))) {
                for (Protection prot : protectionList) {
                    if (prot.isEntityProtected(ev.target.getClass())) {
                        ev.setCanceled(true);
                        res.protectionDenial(FlagType.PVE, block.getTown().formatOwners(ev.target.dimension, (int) Math.floor(ev.target.posX), (int) Math.floor(ev.target.posY), (int) Math.floor(ev.target.posZ)));
                        return;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlacement(BlockEvent.PlaceEvent ev) {
        if(ev.world.isRemote || ev.isCanceled())
            return;
        if(onAnyBlockPlacement(ev.player, ev.itemInHand, ev.placedBlock, ev.world.provider.dimensionId, ev.x, ev.y, ev.z))
            ev.setCanceled(true);
    }

    @SubscribeEvent
    public void onMultiBlockPlacement(BlockEvent.MultiPlaceEvent ev) {
        if(ev.world.isRemote || ev.isCanceled())
            return;
        if(onAnyBlockPlacement(ev.player, ev.itemInHand, ev.placedBlock, ev.world.provider.dimensionId, ev.x, ev.y, ev.z))
            ev.setCanceled(true);
    }

    /**
     * Checks against any type of block placement
     */
    public boolean onAnyBlockPlacement(EntityPlayer player, ItemStack itemInHand, Block blockType, int dimensionId, int x, int y, int z) {
        TownBlock block = MyTownUniverse.instance.blocks.get(dimensionId, x >> 4, z >> 4);
        Resident res = MyTownUniverse.instance.getOrMakeResident(player);


        if (block == null) {
            // Bypass for fakePlayers
            if(player instanceof FakePlayer && (Boolean)Wild.instance.flagsContainer.getValue(FlagType.FAKERS))
                return false;

            if (!Wild.instance.hasPermission(res, FlagType.MODIFY, false)) {
                res.sendMessage(FlagType.MODIFY.getLocalizedProtectionDenial());
                return true;
            } else {
                // If it has permission, then check nearby
                List<Town> nearbyTowns = MyTownUtils.getTownsInRange(dimensionId, x, z, Config.placeProtectionRange, Config.placeProtectionRange);
                for (Town t : nearbyTowns) {
                    if (!t.hasPermission(res, FlagType.MODIFY, false)) {
                        res.protectionDenial(FlagType.MODIFY, t.formatOwners(dimensionId, x, y, z));
                        return true;
                    }
                }
            }
        } else {
            if(player instanceof FakePlayer && (Boolean)block.getTown().getValueAtCoords(dimensionId, x, y, z, FlagType.FAKERS))
                return false;

            if (!block.getTown().hasPermission(res, FlagType.MODIFY, false, dimensionId, x, y, z)) {
                res.protectionDenial(FlagType.MODIFY, block.getTown().formatOwners(dimensionId, x, y, z));
                return true;
            } else {
                // If it has permission, then check nearby
                List<Town> nearbyTowns = MyTownUtils.getTownsInRange(dimensionId, x, z, Config.placeProtectionRange, Config.placeProtectionRange);
                for (Town t : nearbyTowns) {
                    if (block.getTown() != t && !t.hasPermission(res, FlagType.MODIFY, false)) {
                        res.protectionDenial(FlagType.MODIFY, Formatter.formatOwnersToString(t));
                        return true;
                    }
                }
            }

            if (res.townsContainer.contains(block.getTown()) && blockType instanceof ITileEntityProvider && itemInHand != null) {
                TileEntity te = ((ITileEntityProvider) blockType).createNewTileEntity(MinecraftServer.getServer().worldServerForDimension(dimensionId), itemInHand.getItemDamage());
                if (te != null) {
                    Class<? extends TileEntity> clsTe = te.getClass();
                    ProtectionUtils.addToBlockWhitelist(clsTe, dimensionId, x, y, z, block.getTown());
                }
            }
        }
        if(blockType instanceof ITileEntityProvider && itemInHand != null) {
            TileEntity te = ((ITileEntityProvider) blockType).createNewTileEntity(MinecraftServer.getServer().worldServerForDimension(dimensionId), itemInHand.getItemDamage());
            if (te != null && ProtectionUtils.isTileEntityOwnable(te.getClass())) {
                ThreadPlacementCheck thread = new ThreadPlacementCheck(res, x, y, z, dimensionId);
                activePlacementThreads++;
                thread.start();
            }
        }

        return false;
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled())
            return;
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        ItemStack currStack = ev.entityPlayer.getHeldItem();
        TownBlock block = MyTownUniverse.instance.blocks.get(ev.target.dimension, ev.target.chunkCoordX, ev.target.chunkCoordZ);

        if(ev.entityPlayer instanceof FakePlayer) {
            if(block == null) {
                if((Boolean)Wild.instance.flagsContainer.getValue(FlagType.FAKERS))
                    return;
            } else {
                if((Boolean)block.getTown().getValueAtCoords(ev.target.dimension, (int) Math.floor(ev.target.posX), (int) Math.floor(ev.target.posY), (int) Math.floor(ev.target.posZ), FlagType.FAKERS))
                    return;
            }
        }

        for (Protection prot : protectionList) {
            if (prot.checkEntityRightClick(currStack, res, ev.target)) {
                ev.setCanceled(true);
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote || ev.isCanceled())
            return;

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        if (res == null) {
            return;
        }

        int x = ev.x, y = ev.y, z = ev.z;
        ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();

        if(ev.world.getBlock(x, y, z) == Blocks.air) {
            x = (int) Math.floor(ev.entityPlayer.posX);
            y = (int) Math.floor(ev.entityPlayer.posY);
            z = (int) Math.floor(ev.entityPlayer.posZ);
        }

        TownBlock block = MyTownUniverse.instance.blocks.get(ev.world.provider.dimensionId, x >> 4, z >> 4);
        if(ev.entityPlayer instanceof FakePlayer) {
            if(block == null) {
                if((Boolean)Wild.instance.flagsContainer.getValue(FlagType.FAKERS))
                    return;
            } else {
                if((Boolean)block.getTown().getValueAtCoords(ev.world.provider.dimensionId, x, y, z, FlagType.FAKERS))
                    return;
            }
        }

        /*
        // Testing stuff, please ignore
        if( true) {
             try {
                 Thread.sleep(5000);
             } catch (Exception e) {
                 e.printStackTrace();
             }
            ev.setCanceled(true);
            return;
        }
        */

        // Item usage check here
        if (currentStack != null/* && !(currentStack.getItem() instanceof ItemBlock)*/) {
            for (Protection protection : protectionList) {
                if (ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && protection.checkItem(currentStack, ItemType.LEFT_CLICK_BLOCK, res, new BlockPos(x, y, z, ev.world.provider.dimensionId), ev.face) ||
                		ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && protection.checkItem(currentStack, ItemType.RIGHT_CLICK_BLOCK, res, new BlockPos(x, y, z, ev.world.provider.dimensionId), ev.face) ||
                		ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && protection.checkItem(currentStack, res)) {
                    ev.setCanceled(true);
                    return;
                }
            }
        }


        // Activate and access check here
        if(ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            for (Protection protection : protectionList) {
                if (protection.checkBlockInteraction(res, new BlockPos(x, y, z, ev.world.provider.dimensionId), ev.action)) {
                    ev.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if(ev.world.isRemote || ev.isCanceled())
            return;
        TownBlock block = MyTownUniverse.instance.blocks.get(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.getPlayer());
        if (block == null) {
            if(ev.getPlayer() instanceof FakePlayer && (Boolean)Wild.instance.flagsContainer.getValue(FlagType.FAKERS))
                return;

            if (!Wild.instance.hasPermission(res, FlagType.MODIFY, false)) {
                res.sendMessage(FlagType.MODIFY.getLocalizedProtectionDenial());
                ev.setCanceled(true);
            }
        } else {
            Town town = block.getTown();
            if(ev.getPlayer() instanceof FakePlayer && (Boolean)town.getValueAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.FAKERS))
                return;

            if (!town.hasPermission(res, FlagType.MODIFY, false, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                res.protectionDenial(FlagType.MODIFY, town.formatOwners(ev.world.provider.dimensionId, ev.x, ev.y, ev.z));
                ev.setCanceled(true);
                return;
            }

            if (ev.block instanceof ITileEntityProvider) {
                TileEntity te = ((ITileEntityProvider) ev.block).createNewTileEntity(ev.world, ev.blockMetadata);
                if(te != null)
                    ProtectionUtils.removeFromWhitelist(te.getClass(), ev.world.provider.dimensionId, ev.x, ev.y, ev.z, town);
            }
        }

        if (ev.block instanceof ITileEntityProvider) {
            TileEntity te = ((ITileEntityProvider) ev.block).createNewTileEntity(ev.world, ev.blockMetadata);
            if(te != null && ProtectionUtils.isTileEntityOwnable(te.getClass())) {
                te = ev.world.getTileEntity(ev.x, ev.y, ev.z);
                ownedTileEntities.remove(te);
                MyTown.instance.LOG.info("Removed te {}", te.toString());
            }

        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled())
            return;
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        TownBlock block = MyTownUniverse.instance.blocks.get(ev.entityPlayer.dimension, ev.entityPlayer.chunkCoordX, ev.entityPlayer.chunkCoordZ);
        if (block != null) {
            if (!block.getTown().hasPermission(res, FlagType.PICKUP, false, ev.item.dimension, (int) Math.floor(ev.item.posX), (int) Math.floor(ev.item.posY), (int) Math.floor(ev.item.posZ))) {
                if (itemPickupCounter == 0) {
                    res.protectionDenial(FlagType.PICKUP, block.getTown().formatOwners(ev.item.dimension, (int) Math.floor(ev.item.posX), (int) Math.floor(ev.item.posY), (int) Math.floor(ev.item.posZ)));
                    itemPickupCounter = 100;
                } else
                    itemPickupCounter--;
                ev.setCanceled(true);
            }
        } else {
            if(!Wild.instance.hasPermission(res, FlagType.PICKUP, false)) {
                if (itemPickupCounter == 0) {
                    res.sendMessage(FlagType.PICKUP.getLocalizedProtectionDenial());
                    itemPickupCounter = 100;
                } else
                    itemPickupCounter--;
                ev.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled())
            return;
        if(ev.entityLiving instanceof EntityPlayer) {
            TownBlock block = MyTownUniverse.instance.blocks.get(ev.entityLiving.dimension, ev.entityLiving.chunkCoordX, ev.entityLiving.chunkCoordZ);
            // If the entity that "shot" the source of damage is a Player (ex. an arrow shot by player)
            if(ev.source.getEntity() != null && ev.source.getEntity() instanceof EntityPlayer) {
                Resident source = MyTownUniverse.instance.getOrMakeResident(ev.source.getEntity());
                if(block != null) {
                    if(!(Boolean)block.getTown().getValueAtCoords(ev.entityLiving.dimension, (int) Math.floor(ev.entityLiving.posX), (int) Math.floor(ev.entityLiving.posY), (int) Math.floor(ev.entityLiving.posZ), FlagType.PVP)) {
                        ev.setCanceled(true);
                        source.protectionDenial(FlagType.PVP, block.getTown().formatOwners(ev.entityLiving.dimension, (int) Math.floor(ev.entityLiving.posX), (int) Math.floor(ev.entityLiving.posY), (int) Math.floor(ev.entityLiving.posZ)));
                    }
                } else {
                    if(!(Boolean)Wild.instance.flagsContainer.getValue(FlagType.PVP)) {
                        ev.setCanceled(true);
                        source.sendMessage(FlagType.PVP.getLocalizedProtectionDenial());
                    }
                }
            // If the entity that "shot" the source of damage is null or not a player check for specified entities that can bypass pvp flag
            } else if(ev.source.getSourceOfDamage() != null && ProtectionUtils.canEntityTrespassPvp(ev.source.getSourceOfDamage().getClass())) {
                if (block != null) {
                    if (!(Boolean)block.getTown().getValueAtCoords(ev.entityLiving.dimension, (int) Math.floor(ev.entityLiving.posX), (int) Math.floor(ev.entityLiving.posY), (int) Math.floor(ev.entityLiving.posZ), FlagType.PVP)) {
                        ev.setCanceled(true);
                        block.getTown().notifyEveryone(FlagType.PVP.getLocalizedTownNotification());
                    }
                } else {
                    if (!(Boolean) Wild.instance.flagsContainer.getValue(FlagType.PVP)) {
                        ev.setCanceled(true);
                        //target.sendMessage(FlagType.pvp.getLocalizedTownNotification());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBucketFill(FillBucketEvent ev) {
        if(ev.entity.worldObj.isRemote || ev.isCanceled())
            return;
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        TownBlock block = MyTownUniverse.instance.blocks.get(ev.world.provider.dimensionId, ev.target.blockX >> 4, ev.target.blockZ >> 4);
        if(block == null) {
            if(ev.entityPlayer instanceof FakePlayer && (Boolean)Wild.instance.flagsContainer.getValue(FlagType.FAKERS))
                return;

            if(!Wild.instance.hasPermission(res, FlagType.USAGE, false)) {
                res.sendMessage(FlagType.USAGE.getLocalizedProtectionDenial());
                ev.setCanceled(true);
            }
        } else {
            if(ev.entityPlayer instanceof FakePlayer && (Boolean)block.getTown().getValueAtCoords(ev.world.provider.dimensionId, ev.target.blockX, ev.target.blockY, ev.target.blockZ, FlagType.FAKERS))
                return;

            if(!block.getTown().hasPermission(res, FlagType.USAGE, false, ev.world.provider.dimensionId, ev.target.blockX, ev.target.blockY, ev.target.blockZ)) {
                res.protectionDenial(FlagType.USAGE, block.getTown().formatOwners(ev.world.provider.dimensionId, ev.target.blockX, ev.target.blockY, ev.target.blockZ));
                ev.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent ev) {
        if(DatasourceProxy.getDatasource() == null)
            return;

        if (!(ev.entity instanceof EntityLivingBase) || ev.entity instanceof EntityPlayer)
            return;

        Town town = MyTownUtils.getTownAtPosition(ev.entity.dimension, (int) Math.floor(ev.entity.posX) >> 4, (int) Math.floor(ev.entity.posZ) >> 4);

        if(town != null && ev.entity instanceof EntityLiving && "none".equals(town.getValueAtCoords(ev.entity.dimension, (int) Math.floor(ev.entity.posX), (int) Math.floor(ev.entity.posY), (int) Math.floor(ev.entity.posZ), FlagType.MOBS))) {
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void specialSpawn(LivingSpawnEvent.SpecialSpawn ev) {
        if (ev.isCanceled()) return;

        Town town = MyTownUtils.getTownAtPosition(ev.entity.dimension, (int) Math.floor(ev.entity.posX) >> 4, (int) Math.floor(ev.entity.posZ) >> 4);

        if(town != null && ev.entity instanceof EntityLiving && "none".equals(town.getValueAtCoords(ev.entity.dimension, (int) Math.floor(ev.entity.posX), (int) Math.floor(ev.entity.posY), (int) Math.floor(ev.entity.posZ), FlagType.MOBS))) {
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void checkSpawn(LivingSpawnEvent.CheckSpawn ev) {
        if (ev.getResult() == Event.Result.DENY) return;

        Town town = MyTownUtils.getTownAtPosition(ev.entity.dimension, (int) Math.floor(ev.entity.posX) >> 4, (int) Math.floor(ev.entity.posZ) >> 4);

        if(town != null && ev.entity instanceof EntityLiving && "none".equals(town.getValueAtCoords(ev.entity.dimension, (int) Math.floor(ev.entity.posX), (int) Math.floor(ev.entity.posY), (int) Math.floor(ev.entity.posZ), FlagType.MOBS))) {
            ev.setResult(Event.Result.DENY);
        }
    }

    // Fired AFTER the teleport
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent ev) {
        TownBlock block = MyTownUniverse.instance.blocks.get(ev.player.dimension, ev.player.chunkCoordX, ev.player.chunkCoordZ);
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.player);
        if(block != null && !block.getTown().hasPermission(res, FlagType.ENTER, false, ev.player.dimension, (int) Math.floor(ev.player.posX), (int) Math.floor(ev.player.posY), (int) Math.floor(ev.player.posZ))) {
            // Because of badly written teleportation code by Mojang we can only send the player back to spawn. :I
            res.respawnPlayer();
            res.protectionDenial(FlagType.ENTER, block.getTown().formatOwners(ev.player.dimension, (int) Math.floor(ev.player.posX), (int) Math.floor(ev.player.posY), (int) Math.floor(ev.player.posZ)));
        }
    }
}
