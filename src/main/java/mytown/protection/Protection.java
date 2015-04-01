package mytown.protection;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.Wild;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.*;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.BlockPos;
import mytown.util.ChunkPos;
import mytown.util.Formatter;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by AfterWind on 1/1/2015.
 * An object which offers protection for a specific mod and version
 */
public class Protection {

    public String modid;
    public String version;

    public List<SegmentTileEntity> segmentsTiles;
    public List<SegmentEntity> segmentsEntities;
    public List<SegmentItem> segmentsItems;
    public List<SegmentBlock> segmentsBlocks;

    public Protection(String modid, List<Segment> segments) {
        this(modid, "", segments);
    }

    public Protection(String modid, String version, List<Segment> segments) {

        segmentsTiles = new ArrayList<SegmentTileEntity>();
        segmentsEntities = new ArrayList<SegmentEntity>();
        segmentsItems = new ArrayList<SegmentItem>();
        segmentsBlocks = new ArrayList<SegmentBlock>();

        for(Segment segment : segments) {
            if(segment instanceof SegmentTileEntity)
                segmentsTiles.add((SegmentTileEntity)segment);
            else if(segment instanceof SegmentEntity)
                segmentsEntities.add((SegmentEntity) segment);
            else if(segment instanceof SegmentItem)
                segmentsItems.add((SegmentItem)segment);
            else if(segment instanceof SegmentBlock)
                segmentsBlocks.add((SegmentBlock)segment);
        }

        this.modid = modid;
        this.version = version;
    }

    public boolean checkTileEntity(TileEntity te) {
        for(Iterator<SegmentTileEntity> it = segmentsTiles.iterator(); it.hasNext();) {
            SegmentTileEntity segment = it.next();
            if(segment.theClass.isAssignableFrom(te.getClass())) {
                try {
                    if(segment.checkCondition(te)) {

                        int x1 = segment.getX1(te);
                        //int y1 = segmentTE.getY1(te);
                        int z1 = segment.getZ1(te);
                        int x2 = segment.getX2(te);
                        //int y2 = segmentTE.getY2(te);
                        int z2 = segment.getZ2(te);

                        List<ChunkPos> chunks = MyTownUtils.getChunksInBox(x1, z1, x2, z2);
                        boolean inWild = false;
                        for (ChunkPos chunk : chunks) {
                            TownBlock block = getDatasource().getBlock(te.getWorldObj().provider.dimensionId, chunk.getX(), chunk.getZ());
                            if(block == null) {
                                inWild = true;
                            } else {
                                if(segment.hasOwner()) {
                                    Resident res = Protections.getInstance().getOwnerForTileEntity(te);
                                    if (res == null || !block.getTown().checkPermission(res, segment.flag, segment.denialValue))
                                        return true;
                                } else if (!(Boolean) block.getTown().getValue(segment.flag) && !block.getTown().hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.modifyBlocks)) {
                                    block.getTown().notifyEveryone(FlagType.modifyBlocks.getLocalizedTownNotification());
                                    return true;
                                }
                            }
                        }
                        if(inWild && Wild.getInstance().getValue(segment.flag).equals(segment.denialValue)) {
                            if (segment.hasOwner()) {
                                Resident res = Protections.getInstance().getOwnerForTileEntity(te);
                                if (res == null || !Wild.getInstance().checkPermission(res, segment.flag, segment.denialValue))
                                    return true;

                            } else {
                                return true;
                            }
                        }

                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    MyTown.instance.log.error("Failed to check tile entity: " + te.getClass().getSimpleName() + "( " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " | WorldID: " + te.getWorldObj().provider.dimensionId + " )");
                    // Disabling protection if something errors.
                    if(ex instanceof GetterException || ex instanceof ConditionException) {
                        this.disableSegment(it, segment, ex.getMessage());
                    } else {
                        MyTown.instance.log.error("Skipping...");
                    }
                }
                return false;
            }
        }
        return false;
    }

    // TODO: Add condition check
    public boolean checkEntity(Entity entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if (segment.theClass.isAssignableFrom(entity.getClass())) {
                if(segment.type == EntityType.tracked) {
                    int range = segment.getRange(entity);
                    TownBlock block;
                    if(range == 0) {
                        block = getDatasource().getBlock(entity.dimension, ((int)entity.posX) >> 4, ((int)entity.posZ) >> 4);
                        if(block == null) {
                            if(Wild.getInstance().getValue(segment.flag).equals(segment.denialValue))
                                return true;
                        } else {
                            if (block.getTown().getValueAtCoords(entity.dimension, (int) entity.posX, (int) entity.posY, (int) entity.posZ, segment.flag).equals(segment.denialValue))
                                return true;
                        }
                    } else {
                        List<ChunkPos> chunks = MyTownUtils.getChunksInBox((int) (entity.posX - range), (int) (entity.posZ - range), (int) (entity.posX + range), (int) (entity.posZ + range));
                        // Just so that it doesn't check more than once for Wild permissions
                        boolean inWild = false;
                        for (ChunkPos chunk : chunks) {
                            block = getDatasource().getBlock(entity.dimension, chunk.getX(), chunk.getZ());
                            if (block == null) {
                                inWild = true;
                            } else {
                                if (block.getTown().getValue(segment.flag).equals(segment.denialValue))
                                    return true;
                            }
                        }
                        if(inWild && Wild.getInstance().getValue(segment.flag).equals(segment.denialValue))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking item usage for right click on block
     */
    public boolean checkItem(ItemStack item, Resident res, BlockPos bp, int face) {
        for(Iterator<SegmentItem> it = segmentsItems.iterator(); it.hasNext();) {
            SegmentItem segment = it.next();
            if(segment.type == ItemType.rightClickBlock && segment.theClass.isAssignableFrom(item.getItem().getClass())) {
                if(segment.onAdjacent) {
                    ForgeDirection dir = ForgeDirection.getOrientation(face);
                    bp = new BlockPos(bp.x + dir.offsetX, bp.y + dir.offsetY, bp.z + dir.offsetZ, bp.dim);
                }
                try {
                    if (segment.checkCondition(item)) {
                        int range = segment.getRange(item);
                        TownBlock block;
                        if(range == 0) {
                            block = getDatasource().getBlock(bp.dim, bp.x >> 4, bp.z >> 4);
                            if(block == null) {
                                if (!Wild.getInstance().checkPermission(res, segment.flag, segment.denialValue)) {
                                    res.sendMessage(segment.flag.getLocalizedProtectionDenial());
                                    return true;
                                }
                            } else {
                                if (!block.getTown().checkPermission(res, segment.flag, segment.denialValue, bp.dim, bp.x, bp.y, bp.z)) {
                                    res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", Formatter.formatResidentsToString(block.getTown().getOwnersAtPosition(bp.dim, bp.x, bp.y, bp.z))));
                                    return true;
                                }
                            }
                        } else {
                            List<ChunkPos> chunks = MyTownUtils.getChunksInBox(bp.x - range, bp.z - range, bp.x + range, bp.z + range);
                            boolean inWild = false;
                            for (ChunkPos chunk : chunks) {
                                block = getDatasource().getBlock(bp.dim, chunk.getX(), chunk.getZ());
                                if (block == null) {
                                    inWild = true;
                                } else {
                                    if (!block.getTown().checkPermission(res, segment.flag, segment.denialValue)) {
                                        res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", block.getTown().getMayor() == null ? "SERVER ADMINS" : block.getTown().getMayor().getPlayerName()));
                                        return true;
                                    }
                                }
                            }
                            if (inWild && !Wild.getInstance().checkPermission(res, segment.flag, segment.denialValue)) {
                                res.sendMessage(segment.flag.getLocalizedProtectionDenial());
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.log.error("Failed to check item use on " + item.getDisplayName() + " at the player " + res.getPlayerName() + "( " + bp.x
                            + ", " + bp.y + ", " + bp.z + " | WorldID: " + bp.dim + " )");
                    if(ex instanceof GetterException || ex instanceof ConditionException) {
                        this.disableSegment(it, segment, ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking item usage for right click on entity
     */
    public boolean checkEntityRightClick(ItemStack item, Resident res, Entity entity) {
        for(Iterator<SegmentEntity> it = segmentsEntities.iterator(); it.hasNext();) {
            SegmentEntity segment = it.next();
            if(segment.type == EntityType.protect && segment.theClass.isAssignableFrom(entity.getClass())) {
                TownBlock block = getDatasource().getBlock(entity.dimension, (int) entity.posX >> 4, (int) entity.posZ >> 4);
                if(block == null) {
                    if(!Wild.getInstance().checkPermission(res, segment.flag, segment.denialValue)) {
                        res.sendMessage(FlagType.protectedEntities.getLocalizedProtectionDenial());
                        return true;
                    }
                } else {
                    if(!block.getTown().checkPermission(res, segment.flag, segment.denialValue, entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ)) {
                        res.protectionDenial(FlagType.protectedEntities.getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", Formatter.formatResidentsToString(block.getTown().getOwnersAtPosition(entity.dimension, ((int) entity.posX), ((int) entity.posY), ((int) entity.posZ)))));
                        return true;
                    }
                }
            }
        }
        for(Iterator<SegmentItem> it = segmentsItems.iterator(); it.hasNext();) {
            SegmentItem segment = it.next();
            if(segment.type == ItemType.rightClickBlock && segment.theClass.isAssignableFrom(item.getItem().getClass())) {
                try {
                    if (segment.checkCondition(item)) {
                        int range = segment.getRange(item);
                        TownBlock block;
                        if(range == 0) {
                            block = getDatasource().getBlock(entity.dimension, ((int)entity.posX) >> 4, ((int)entity.posZ) >> 4);
                            if(block == null) {
                                if (!Wild.getInstance().checkPermission(res, segment.flag, segment.denialValue)) {
                                    res.sendMessage(segment.flag.getLocalizedProtectionDenial());
                                    return true;
                                }
                            } else {
                                if (!block.getTown().checkPermission(res, segment.flag, segment.denialValue, entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ)) {
                                    res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", Formatter.formatResidentsToString(block.getTown().getOwnersAtPosition(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ))));
                                    return true;
                                }
                            }
                        } else {
                            List<ChunkPos> chunks = MyTownUtils.getChunksInBox((int) entity.posX - range, (int) entity.posZ - range, (int) entity.posX + range, (int) entity.posZ + range);
                            boolean inWild = false;
                            for (ChunkPos chunk : chunks) {
                                block = getDatasource().getBlock(entity.dimension, chunk.getX(), chunk.getZ());
                                if (block == null) {
                                    inWild = true;
                                } else {
                                    if (!block.getTown().checkPermission(res, segment.flag, segment.denialValue)) {
                                        res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", block.getTown().getMayor() == null ? "SERVER ADMINS" : block.getTown().getMayor().getPlayerName()));
                                        return true;
                                    }
                                }
                            }
                            if (inWild && !Wild.getInstance().checkPermission(res, segment.flag, segment.denialValue)) {
                                res.sendMessage(segment.flag.getLocalizedProtectionDenial());
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.log.error("Failed to check item use on " + item.getDisplayName() + " at the player " + res.getPlayerName() + "( " + (int)entity.posX
                            + ", " + (int)entity.posY + ", " + (int)entity.posZ + " | WorldID: " + entity.dimension + " )");
                    if(ex instanceof GetterException || ex instanceof ConditionException) {
                        this.disableSegment(it, segment, ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking item usage for right click on air
     */
    public boolean checkItem(ItemStack item, Resident res) {
        for(Iterator<SegmentItem> it = segmentsItems.iterator(); it.hasNext();) {
            SegmentItem segment = it.next();
            if(segment.type == ItemType.rightClickAir && segment.theClass.isAssignableFrom(item.getItem().getClass())) {
                EntityPlayer entity = res.getPlayer();

                try {
                    if (segment.checkCondition(item)) {
                        int range = segment.getRange(item);
                        TownBlock block;
                        if(range == 0) {
                            block = getDatasource().getBlock(entity.dimension, ((int)entity.posX) >> 4, ((int)entity.posZ) >> 4);
                            if(block == null) {
                                if (!Wild.getInstance().checkPermission(res, segment.flag, segment.denialValue)) {
                                    res.sendMessage(segment.flag.getLocalizedProtectionDenial());
                                    return true;
                                }
                            } else {
                                if (!block.getTown().checkPermission(res, segment.flag, segment.denialValue, entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ)) {
                                    res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", Formatter.formatResidentsToString(block.getTown().getOwnersAtPosition(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ))));
                                    return true;
                                }
                            }
                        } else {
                            List<ChunkPos> chunks = MyTownUtils.getChunksInBox((int) entity.posX - range, (int) entity.posZ - range, (int) entity.posX + range, (int) entity.posZ + range);
                            boolean inWild = false;
                            for (ChunkPos chunk : chunks) {
                                block = getDatasource().getBlock(entity.dimension, chunk.getX(), chunk.getZ());
                                if (block == null) {
                                    inWild = true;
                                } else {
                                    if (!block.getTown().checkPermission(res, segment.flag, segment.denialValue)) {
                                        res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", block.getTown().getMayor() == null ? "SERVER ADMINS" : block.getTown().getMayor().getPlayerName()));
                                        return true;
                                    }
                                }
                            }
                            if (inWild && !Wild.getInstance().checkPermission(res, segment.flag, segment.denialValue)) {
                                res.sendMessage(segment.flag.getLocalizedProtectionDenial());
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.log.error("Failed to check item use on " + item.getDisplayName() + " at the player " + res.getPlayerName() + "( "
                            + ", " + entity.posX + ", " + entity.posY + ", " + entity.posZ + " | WorldID: " + entity.dimension + " )");

                    if(ex instanceof GetterException || ex instanceof ConditionException) {
                        this.disableSegment(it, segment, ex.getMessage());
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking right click actions on blocks.
     */
    public boolean checkBlockInteraction(Resident res, BlockPos bp, PlayerInteractEvent.Action action) {
        Block blockType = DimensionManager.getWorld(bp.dim).getBlock(bp.x, bp.y, bp.z);
        for(SegmentBlock segment : segmentsBlocks) {
            if(segment.theClass.isAssignableFrom(blockType.getClass())
                    && (segment.meta == -1 || segment.meta == DimensionManager.getWorld(bp.dim).getBlockMetadata(bp.x, bp.y, bp.z))
                    && (segment.type == BlockType.anyClick || segment.type == BlockType.rightClick && action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || segment.type == BlockType.leftClick && action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
                if(segment.flag == FlagType.accessBlocks || segment.flag == FlagType.activateBlocks) {
                    TownBlock block = getDatasource().getBlock(bp.dim, bp.x >> 4, bp.z >> 4);
                    if(block == null) {
                        if(!Wild.getInstance().checkPermission(res, segment.flag, segment.denialValue)) {
                            res.sendMessage(segment.flag.getLocalizedProtectionDenial());
                            return true;
                        }
                    } else {
                        if(!block.getTown().checkPermission(res, segment.flag, segment.denialValue, bp.dim, bp.x, bp.y, bp.z)) {
                            res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", Formatter.formatResidentsToString(block.getTown().getOwnersAtPosition(bp.dim, bp.x, bp.y, bp.z))));
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Gets the flags which the type of TileEntity is checked against.
     */
    public List<FlagType> getFlagsForTile(Class<? extends TileEntity> te) {
        List<FlagType> flags = new ArrayList<FlagType>();
        for(SegmentTileEntity segment : segmentsTiles) {
            if(segment.theClass.isAssignableFrom(te))
                flags.add(segment.flag);
        }
        return flags;
    }

    public EntityType getEntityType(Class<? extends Entity> entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if (segment.theClass.isAssignableFrom(entity)) {
                return segment.type;
            }
        }
        return null;
    }

    public boolean isEntityProtected(Class<? extends Entity> entity) {
        EntityType type = getEntityType(entity);
        return type != null && type == EntityType.protect;
    }

    /**
     * Returns whether or not the entity should be checked each tick.
     */
    public boolean isEntityTracked(Class<? extends Entity> entity) {
        EntityType type = getEntityType(entity);
        return type != null && (type == EntityType.tracked);
    }

    public boolean isTileTracked(Class<? extends TileEntity> te) {
        for(SegmentTileEntity segment : segmentsTiles) {
            if(segment.theClass.isAssignableFrom(te))
                return true;
        }
        return false;
    }

    public boolean isBlockTracked(Class<? extends Block> block, int meta) {
        for(SegmentBlock segment : segmentsBlocks) {
            if(segment.theClass.isAssignableFrom(block) &&( segment.meta == -1 || segment.meta == meta))
                return true;
        }
        return false;
    }

    public boolean isTileEntityOwnable(Class<? extends TileEntity> te) {
        for(SegmentTileEntity segment : segmentsTiles) {
            if(segment.theClass.isAssignableFrom(te) && segment.hasOwner())
                return true;
        }
        return false;
    }

    public boolean canEntityTrespassPvp(Class<? extends Entity> entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if(segment.theClass.isAssignableFrom(entity) && segment.type == EntityType.pvp) {
                return true;
            }
        }
        return false;
    }

    /*  ---- Protection instance utilities ---- */

    private void disableSegment(Iterator<? extends Segment> it, Segment segment, String message) {
        it.remove();
        MyTown.instance.log.error(message);
        MyTown.instance.log.error("Disabling segment for " + segment.theClass.getName() + " in protection " + this.modid + ".");
        MyTown.instance.log.info("Reload protections to enable it again.");
    }
    private void disable() {
        Protections.getInstance().removeProtection(this);
    }

    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}
