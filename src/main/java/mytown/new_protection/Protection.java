package mytown.new_protection;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.entities.TownBlock;
import mytown.entities.flag.FlagType;
import mytown.new_protection.segment.Segment;
import mytown.new_protection.segment.SegmentTileEntity;
import mytown.proxies.DatasourceProxy;
import mytown.util.ChunkPos;
import mytown.util.MyTownUtils;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

/**
 * Created by AfterWind on 1/1/2015.
 * A protection object which offers protection for a specific mod
 */
public class Protection {

    public String modid;
    public List<Segment> segments;

    public Protection(String modid, List<Segment> segments) {
        this.segments = segments;
        this.modid = modid;
    }

    public boolean checkTileEntity(TileEntity te) {
        for(Segment segment : segments) {
            if(segment instanceof SegmentTileEntity && segment.theClass == te.getClass()) {
                SegmentTileEntity segmentTE = (SegmentTileEntity)segment;

                try {
                    int x1 = segmentTE.getX1(te);
                    //int y1 = segmentTE.getY1(te);
                    int z1 = segmentTE.getZ1(te);
                    int x2 = segmentTE.getX2(te);
                    //int y2 = segmentTE.getY2(te);
                    int z2 = segmentTE.getZ2(te);

                    List<ChunkPos> chunks = MyTownUtils.getChunksInBox(x1, z1, x2, z2);
                    for(ChunkPos chunk : chunks) {
                        TownBlock tblock =  getDatasource().getBlock(te.getWorldObj().provider.dimensionId, chunk.getX(), chunk.getZ());
                        if(tblock != null) {
                            boolean modifyValue = (Boolean)tblock.getTown().getValue(FlagType.modifyBlocks);
                            if(!modifyValue && !tblock.getTown().hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.modifyBlocks)) {
                                tblock.getTown().notifyEveryone(FlagType.modifyBlocks.getLocalizedTownNotification());
                                return true;
                            }
                        }
                    }

                } catch (Exception ex) {
                    MyTown.instance.log.error("Failed to check tile entity: " + te.toString());
                    MyTown.instance.log.error("Skipping...");
                    // TODO: Leave it completely unprotected or completely unusable?
                }
                return false;
            }
        }
        return false;
    }

    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }

}
