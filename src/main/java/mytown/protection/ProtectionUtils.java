package mytown.protection;

import cpw.mods.fml.common.eventhandler.Event;
import myessentials.entities.BlockPos;
import myessentials.entities.EntityPos;
import myessentials.entities.Volume;
import myessentials.utils.PlayerUtils;
import myessentials.utils.WorldUtils;
import mytown.MyTown;
import mytown.api.container.SegmentsContainer;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.*;
import mytown.proxies.DatasourceProxy;
import mytown.util.MyTownUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for the protections
 */
public class ProtectionUtils {

    public static final SegmentsContainer<SegmentBlock> segmentsBlock = new SegmentsContainer<SegmentBlock>();
    public static final SegmentsContainer<SegmentEntity> segmentsEntity = new SegmentsContainer<SegmentEntity>();
    public static final SegmentsContainer<SegmentItem> segmentsItem = new SegmentsContainer<SegmentItem>();
    public static final SegmentsContainer<SegmentTileEntity> segmentsTile = new SegmentsContainer<SegmentTileEntity>();
    private static final Map<EntityPlayer, EntityPos> lastTickPlayerPos = new HashMap<EntityPlayer, EntityPos>();

    private ProtectionUtils() {
    }

    public static void addProtection(Protection protection) {
        segmentsBlock.addAll(protection.segmentsBlocks);
        segmentsEntity.addAll(protection.segmentsEntities);
        segmentsItem.addAll(protection.segmentsItems);
        segmentsTile.addAll(protection.segmentsTiles);
    }

    public static void check(EntityPlayerMP player) {
        Town town = MyTownUtils.getTownAtPosition(player.dimension, (int) Math.floor(player.posX) >> 4, (int) Math.floor(player.posZ) >> 4);
        Resident res = MyTownUniverse.instance.getOrMakeResident(player);
        EntityPos lastTickPos = lastTickPlayerPos.get(player);

        if (res == null) {
            return;
        }

        if (!ProtectionUtils.hasPermission(res, FlagType.ENTER, player.dimension, (int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ))) {
            if(lastTickPos == null) {
                res.knockbackPlayerToBorder(town);
            } else if(lastTickPos.getX() != player.posX || lastTickPos.getY() != player.posY || lastTickPos.getZ() != player.posZ || lastTickPos.getDim() != player.dimension) {
                PlayerUtils.teleport(player, lastTickPos.getDim(), lastTickPos.getX(), lastTickPos.getY(), lastTickPos.getZ());
            }
        } else {
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

    public static boolean check(Entity entity) {
        if(entity instanceof EntityLiving) {
            if(!getFlagValueAtLocation(FlagType.ENTITIES, entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ))) {
                entity.setDead();
                return true;
            }
        }


        for(SegmentEntity segment : segmentsEntity.get(entity.getClass())) {
            if(!segment.shouldExist(entity)) {
                entity.setDead();
                return true;
            }
        }

        return false;
    }

    public static void check(TileEntity te) {
        for (SegmentTileEntity segment : segmentsTile.get(te.getClass())) {
            if (!segment.shouldExist(te)) {
                ItemStack itemStack = new ItemStack(te.getBlockType(), 1, te.getBlockMetadata());
                NBTTagCompound nbt = new NBTTagCompound();
                te.writeToNBT(nbt);
                itemStack.setTagCompound(nbt);
                WorldUtils.dropAsEntity(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, itemStack);
                te.getWorldObj().setBlock(te.xCoord, te.yCoord, te.zCoord, Blocks.air);
                te.invalidate();
                MyTown.instance.LOG.info("TileEntity {} was ATOMICALLY DISINTEGRATED!", te.toString());
                return;
            }
        }
    }

    public static void checkInteraction(Entity entity, Resident res, Event event) {
        if(!event.isCancelable()) {
            return;
        }

        for(SegmentEntity segment : segmentsEntity.get(entity.getClass())) {
            if(!segment.shouldInteract(entity, res)) {
                event.setCanceled(true);
            }
        }
    }

    public static void checkPVP(Entity entity, Resident res, Event event) {
        if(!event.isCancelable()) {
            return;
        }

        for(SegmentEntity segment : segmentsEntity.get(entity.getClass())) {
            if(!segment.shouldAttack(entity, res)) {
                event.setCanceled(true);
            }
        }
    }

    public static void checkUsage(ItemStack stack, Resident res, PlayerInteractEvent.Action action, BlockPos bp, int face, Event ev) {
        if(!ev.isCancelable()) {
            return;
        }

        for(SegmentItem segment : segmentsItem.get(stack.getItem().getClass())) {
            if(!segment.shouldInteract(stack, res, action, bp, face)) {
                ev.setCanceled(true);
            }
        }
    }

    public static void checkBreakWithItem(ItemStack stack, Resident res, BlockPos bp, Event ev) {
        if(!ev.isCancelable()) {
            return;
        }

        for(SegmentItem segment : segmentsItem.get(stack.getItem().getClass())) {
            if(!segment.shouldBreakBlock(stack, res, bp)) {
                ev.setCanceled(true);
            }
        }
    }


    public static void checkBlockInteraction(Resident res, BlockPos bp, PlayerInteractEvent.Action action, Event ev) {
        if(!ev.isCancelable()) {
            return;
        }
        World world = MinecraftServer.getServer().worldServerForDimension(bp.getDim());
        Block block = world.getBlock(bp.getX(), bp.getY(), bp.getZ());

        for(SegmentBlock segment : segmentsBlock.get(block.getClass())) {
            if(!segment.shouldInteract(res, bp, action)) {
                ev.setCanceled(true);
            }
        }
    }

    public static boolean hasPermission(Resident res, FlagType<Boolean> flagType, int dim, int x, int y, int z) {
        if(MyTownUniverse.instance.blocks.contains(dim, x >> 4, z >> 4)) {
            Town town = MyTownUniverse.instance.blocks.get(dim, x >> 4, z >> 4).getTown();
            return town.hasPermission(res, flagType, dim, x, y, z);
        } else {
            return !flagType.isWildPerm || Wild.instance.hasPermission(res, flagType);
        }
    }

    public static <T> T getFlagValueAtLocation(FlagType<T> flagType, int dim, int x, int y, int z) {
        if(MyTownUniverse.instance.blocks.contains(dim, x >> 4, z >> 4)) {
            Town town = MyTownUniverse.instance.blocks.get(dim, x >> 4, z >> 4).getTown();
            return town.getValueAtCoords(dim, x, y, z, flagType);
        } else {
            return flagType.isWildPerm ? Wild.instance.flagsContainer.get(flagType).value : null;
        }
    }

    public static boolean hasPermission(Resident res, FlagType<Boolean> flagType, int dim, Volume volume) {
        boolean inWild = false;

        for (int townBlockX = volume.getMinX() >> 4; townBlockX <= volume.getMaxX() >> 4; townBlockX++) {
            for (int townBlockZ = volume.getMinZ() >> 4; townBlockZ <= volume.getMaxZ() >> 4; townBlockZ++) {
                TownBlock townBlock = MyTownUniverse.instance.blocks.get(dim, townBlockX, townBlockZ);

                if (townBlock == null) {
                    inWild = true;
                    continue;
                }

                Town town = townBlock.getTown();
                Volume rangeBox = volume.intersect(townBlock.toVolume());
                int totalIntersectArea = 0;

                // Check every plot in the current TownBlock and sum all plot areas
                for (Plot plot : townBlock.plotsContainer) {
                    Volume plotIntersection = volume.intersect(plot.toVolume());
                    if (plotIntersection != null) {
                        if(!plot.hasPermission(res, flagType)) {
                            return false;
                        }
                        totalIntersectArea += plotIntersection.getVolumeAmount();
                    }
                }

                // If plot area sum is not equal to range area, check town permission
                if (totalIntersectArea != rangeBox.getVolumeAmount()) {
                    if(!town.hasPermission(res, flagType)) {
                        return false;
                    }
                }
            }
        }

        if (inWild) {
            return Wild.instance.hasPermission(res, flagType);
        }

        return true;
    }

    public static Resident getOwner(Entity entity) {
        for(SegmentEntity segment : segmentsEntity.get(entity.getClass())) {
            return segment.getOwner(entity);
        }
        return null;
    }

    public static boolean isOwnable(Class<? extends TileEntity> clazz) {
        for(SegmentTileEntity segment : segmentsTile.get(clazz)) {
            if(segment.retainsOwner()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBlockWhitelistValid(BlockWhitelist bw) {
        // Delete if the town is gone
        if (MyTownUtils.getTownAtPosition(bw.getDim(), bw.getX() >> 4, bw.getZ() >> 4) == null) {
            return false;
        }

        if(!bw.getFlagType().isWhitelistable) {
            return false;
        }

        /*
        if (bw.getFlagType() == FlagType.ACTIVATE
                && !checkActivatedBlocks(MinecraftServer.getServer().worldServerForDimension(bw.getDim()).getBlock(bw.getX(), bw.getY(), bw.getZ()), MinecraftServer.getServer().worldServerForDimension(bw.getDim()).getBlockMetadata(bw.getX(), bw.getY(), bw.getZ())))
            return false;
        if (bw.getFlagType() == FlagType.MODIFY || bw.getFlagType() == FlagType.ACTIVATE || bw.getFlagType() == FlagType.USAGE) {
            TileEntity te = MinecraftServer.getServer().worldServerForDimension(bw.getDim()).getTileEntity(bw.getX(), bw.getY(), bw.getZ());
            if (te == null)
                return false;
            return getFlagsForTile(te.getClass()).contains(bw.getFlagType());
        }
        */
        return true;
    }


    public static void saveBlockOwnersToDB() {
        for(Map.Entry<TileEntity, Resident> set : ProtectionHandler.instance.ownedTileEntities.entrySet()) {
            DatasourceProxy.getDatasource().saveBlockOwner(set.getValue(), set.getKey().getWorldObj().provider.dimensionId, set.getKey().xCoord, set.getKey().yCoord, set.getKey().zCoord);
        }
    }

    /**
     * Method called by the ThreadPlacementCheck after it found a TileEntity
     */
    public static synchronized void addTileEntity(TileEntity te, Resident res) {
        ProtectionHandler.instance.ownedTileEntities.put(te, res);
        if(ProtectionHandler.instance.activePlacementThreads != 0)
            ProtectionHandler.instance.activePlacementThreads--;
    }

    public static synchronized void placementThreadTimeout() {
        ProtectionHandler.instance.activePlacementThreads--;
    }

    private MyTownUniverse getUniverse() {
        return MyTownUniverse.instance;
    }
}
