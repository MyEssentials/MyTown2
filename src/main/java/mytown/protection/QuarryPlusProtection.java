package mytown.protection;

import com.esotericsoftware.reflectasm.FieldAccess;
import mytown.MyTown;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.util.ChunkPos;
import mytown.util.Utils;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 11/5/2014.
 * Handles QuarryPlus protection
 */
public class QuarryPlusProtection extends Protection {

    FieldAccess fAccessQuarry;

    Class<? extends TileEntity> clsQuarry;

    @SuppressWarnings("unchecked")
    public QuarryPlusProtection() {
        try {
            clsQuarry = (Class<? extends TileEntity>) Class.forName("com.yogpc.qp.TileQuarry");
            fAccessQuarry = FieldAccess.get(clsQuarry);
            trackedTileEntities.add(clsQuarry);
        } catch (Exception e) {
            MyTown.instance.log.error("Failed to get classes for QuarryPlus!");
        }
    }

    @Override
    public boolean checkTileEntity(TileEntity te) {
        if (clsQuarry.isAssignableFrom(te.getClass())) {
            int xMin, xMax, zMin, zMax;
            xMin = fAccessQuarry.getInt(te, fAccessQuarry.getIndex("xMin"));
            xMax = fAccessQuarry.getInt(te, fAccessQuarry.getIndex("xMax"));
            zMin = fAccessQuarry.getInt(te, fAccessQuarry.getIndex("zMin"));
            zMax = fAccessQuarry.getInt(te, fAccessQuarry.getIndex("zMax"));

            List<ChunkPos> chunks = Utils.getChunksInBox(xMin, zMin, xMax, zMax);

            for (ChunkPos chunk : chunks) {
                Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, chunk.getX(), chunk.getZ());
                if (town != null) {
                    if ((Boolean) town.getValue(FlagType.breakBlocks) && !town.hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.breakBlocks)) {
                        town.notifyEveryone(FlagType.breakBlocks.getLocalizedTownNotification());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<FlagType> getFlagTypeForTile(Class<? extends TileEntity> te) {
        List<FlagType> flags = new ArrayList<FlagType>();
        if (clsQuarry.isAssignableFrom(te))
            flags.add(FlagType.breakBlocks);
        return flags;
    }
}
