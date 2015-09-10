package mytown.protection;

import mytown.MyTown;
import mytown.api.container.SegmentsContainer;
import mytown.datasource.MyTownDatasource;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.*;
import mytown.protection.segment.enums.EntityType;
import mytown.proxies.DatasourceProxy;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An object which offers protection for a specific mod and version
 */
public class Protection {

    public final String modid;
    public final String version;

    public final SegmentsContainer<SegmentTileEntity> segmentsTiles = new SegmentsContainer<SegmentTileEntity>();
    public final SegmentsContainer<SegmentEntity> segmentsEntities = new SegmentsContainer<SegmentEntity>();
    public final SegmentsContainer<SegmentItem> segmentsItems = new SegmentsContainer<SegmentItem>();
    public final SegmentsContainer<SegmentBlock> segmentsBlocks = new SegmentsContainer<SegmentBlock>();

    public Protection(String modid, List<Segment> segments) {
        this(modid, "", segments);
    }

    public Protection(String modid, String version, List<Segment> segments) {
        for(Segment segment : segments) {
            if(segment instanceof SegmentTileEntity) {
                segmentsTiles.add((SegmentTileEntity) segment);
            } else if(segment instanceof SegmentEntity) {
                segmentsEntities.add((SegmentEntity) segment);
            } else if(segment instanceof SegmentItem) {
                segmentsItems.add((SegmentItem) segment);
            } else if(segment instanceof SegmentBlock) {
                segmentsBlocks.add((SegmentBlock) segment);
            }
        }

        this.modid = modid;
        this.version = version;
    }

    /**
     * Gets the flags which the type of TileEntity is checked against.
     */
    public List<FlagType> getFlagsForTile(Class<? extends TileEntity> te) {
        List<FlagType> flags = new ArrayList<FlagType>();
        for(SegmentTileEntity segment : segmentsTiles) {
            if(segment.getCheckClass().isAssignableFrom(te))
                flags.add(segment.getFlag());
        }
        return flags;
    }

    public EntityType getEntityType(Class<? extends Entity> entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if (segment.getCheckClass().isAssignableFrom(entity)) {
                return segment.getType();
            }
        }
        return null;
    }

    public boolean isEntityProtected(Class<? extends Entity> entity) {
        EntityType type = getEntityType(entity);
        return type != null && type == EntityType.PROTECT;
    }

    /**
     * Returns whether or not the entity should be checked each tick.
     */
    public boolean isEntityTracked(Class<? extends Entity> entity) {
        EntityType type = getEntityType(entity);
        return type != null && (type == EntityType.TRACKED);
    }

    public boolean isEntityOwnable(Class<? extends Entity> entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if(segment.getCheckClass().isAssignableFrom(entity) && segment.hasOwner())
                return true;
        }
        return false;
    }

    public boolean isTileTracked(Class<? extends TileEntity> te) {
        for(SegmentTileEntity segment : segmentsTiles) {
            if(segment.getCheckClass().isAssignableFrom(te))
                return true;
        }
        return false;
    }

    public boolean isBlockTracked(Class<? extends Block> block, int meta) {
        for(SegmentBlock segment : segmentsBlocks) {
            if(segment.getCheckClass().isAssignableFrom(block) &&( segment.getMeta() == -1 || segment.getMeta() == meta))
                return true;
        }
        return false;
    }

    public boolean isTileEntityOwnable(Class<? extends TileEntity> te) {
        for(SegmentTileEntity segment : segmentsTiles) {
            if(segment.getCheckClass().isAssignableFrom(te) && segment.hasOwner())
                return true;
        }
        return false;
    }

    public boolean canEntityTrespassPvp(Class<? extends Entity> entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if(segment.getCheckClass().isAssignableFrom(entity) && segment.getType() == EntityType.PVP) {
                return true;
            }
        }
        return false;
    }

    /*  ---- Protection instance utilities ---- */

    private void disableSegment(Iterator<? extends Segment> it, Segment segment, String message) {
        it.remove();
        MyTown.instance.LOG.error(message);
        MyTown.instance.LOG.error("Disabling segment for {} in protection {}.", segment.getCheckClass().getName(), this.modid);
        MyTown.instance.LOG.info("Reload protections to enable it again.");
    }
    private void disable() {
        Protections.instance.removeProtection(this);
    }

    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}