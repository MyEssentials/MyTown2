package mytown.new_protection;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.flag.FlagType;
import mytown.new_protection.segment.*;
import mytown.new_protection.segment.enums.EntityType;
import mytown.new_protection.segment.enums.ItemType;
import mytown.proxies.DatasourceProxy;
import mytown.util.BlockPos;
import mytown.util.ChunkPos;
import mytown.util.Formatter;
import mytown.util.MyTownUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 1/1/2015.
 * A protection object which offers protection for a specific mod
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
                segmentsEntities.add((SegmentEntity)segment);
            else if(segment instanceof SegmentItem)
                segmentsItems.add((SegmentItem)segment);
            else if(segment instanceof SegmentBlock)
                segmentsBlocks.add((SegmentBlock)segment);
        }

        this.modid = modid;
        this.version = version;
    }

    public boolean checkTileEntity(TileEntity te) {
        //MyTown.instance.log.info("Protection: " + this.modid);
        for(SegmentTileEntity segment : segmentsTiles) {
            //MyTown.instance.log.info("Segment: " + segment.theClass.getName());
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
                        for (ChunkPos chunk : chunks) {
                            TownBlock tblock = getDatasource().getBlock(te.getWorldObj().provider.dimensionId, chunk.getX(), chunk.getZ());
                            if (tblock != null) {
                                boolean modifyValue = (Boolean) tblock.getTown().getValue(FlagType.modifyBlocks);
                                if (!modifyValue && !tblock.getTown().hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.modifyBlocks)) {
                                    tblock.getTown().notifyEveryone(FlagType.modifyBlocks.getLocalizedTownNotification());
                                    return true;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MyTown.instance.log.error("Failed to check tile entity: " + te.toString());
                    MyTown.instance.log.error("Skipping...");

                    // Disabling protection if something errors.
                    this.disable();
                }
                return false;
            }
        }
        return false;
    }

    public boolean checkEntity(Entity entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if (segment.type == EntityType.hostile) {
                if (segment.theClass.isAssignableFrom(entity.getClass())) {
                    Town town = MyTownUtils.getTownAtPosition(entity.dimension, ((int) entity.posX) >> 4, ((int) entity.posZ) >> 4);
                    if (town != null) {
                        String mobsValue = (String) town.getValueAtCoords(entity.dimension, (int) entity.posX, (int) entity.posY, (int) entity.posZ, FlagType.mobs);
                        if (mobsValue.equals("hostiles"))
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
        for(SegmentItem segment : segmentsItems) {
            if(segment.type == ItemType.rightClickBlock && segment.theClass.isAssignableFrom(item.getItem().getClass())) {

                if(segment.onAdjacent) {
                    ForgeDirection dir = ForgeDirection.getOrientation(face);
                    bp = new BlockPos(bp.x + dir.offsetX, bp.y + dir.offsetY, bp.z + dir.offsetZ, bp.dim);
                }

                if(segment.checkCondition(item)) {
                    Town town = MyTownUtils.getTownAtPosition(bp.dim, bp.x >> 4, bp.z >> 4);
                    if(town != null && !town.checkPermission(res, segment.flag, bp.dim, bp.x, bp.y, bp.z)) {
                        res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(town.getOwnersAtPosition(bp.dim, bp.x, bp.y, bp.z)));
                        if(segment.flag == FlagType.modifyBlocks && segment.onAdjacent) {
                            //DimensionManager.getWorld(bp.dim).setBlock(bp.x, bp.y, bp.z, Blocks.air);
                            //MyTown.instance.log.info("Block deleted!");
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking item usage for right click on entity
     */
    public boolean checkItem(ItemStack item, Resident res, Entity entity) {
        for(SegmentItem segment : segmentsItems) {
            if(segment.type == ItemType.rightClickEntity && segment.theClass.isAssignableFrom(item.getItem().getClass())) {
                MyTown.instance.log.info("Checking item: " + item.getDisplayName());

                if(segment.checkCondition(item)) {
                    Town town = MyTownUtils.getTownAtPosition(entity.dimension, ((int)entity.posX) >> 4, ((int)entity.posZ) >> 4);
                    if(town != null && !town.checkPermission(res, segment.flag, entity.dimension, ((int)entity.posX), ((int)entity.posY), ((int)entity.posZ))) {
                        res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(town.getOwnersAtPosition(entity.dimension, ((int)entity.posX), ((int)entity.posY), ((int)entity.posZ))));
                        return true;
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
        for(SegmentItem segment : segmentsItems) {
            if(segment.type == ItemType.rightClickAir && segment.theClass.isAssignableFrom(item.getItem().getClass())) {
                MyTown.instance.log.info("Checking item: " + item.getDisplayName());
                EntityPlayer entity = res.getPlayer();

                if(segment.checkCondition(item)) {
                    Town town = MyTownUtils.getTownAtPosition(entity.dimension, ((int)entity.posX) >> 4, ((int)entity.posZ) >> 4);
                    if(town != null && !town.checkPermission(res, segment.flag, entity.dimension, ((int)entity.posX), ((int)entity.posY), ((int)entity.posZ))) {
                        res.protectionDenial(segment.flag.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(town.getOwnersAtPosition(entity.dimension, ((int)entity.posX), ((int)entity.posY), ((int)entity.posZ))));
                        return true;
                    }
                }
            }
        }
        return false;
    }



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
        return type != null && type == EntityType.passive;
    }

    public boolean isEntityHostile(Class<? extends Entity> entity) {
        EntityType type = getEntityType(entity);
        return type != null && type == EntityType.hostile;
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



    private void disable() {
        Protections.getInstance().removeProtection(this);
    }

    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}
