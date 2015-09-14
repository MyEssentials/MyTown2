package mytown.protection;

import mytown.api.container.SegmentsContainer;
import mytown.protection.segment.*;

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

    /*  ---- Protection instance utilities ---- */

    private void disable() {
        ProtectionUtils.protections.remove(this);
    }
}