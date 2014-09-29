package mytown.protection;

import mytown.MyTown;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.protection.Protection;
import mytown.util.ChunkPos;
import mytown.util.Utils;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 9/22/2014.
 * ExtraUtilities Protection
 */
public class ExtraUtilitiesProtection extends Protection {

    private Class<? extends TileEntity> clsTileEnderQuarry;
    private Class<? extends TileEntity> clsTileEnderPump;

    @SuppressWarnings("unchecked")
    public ExtraUtilitiesProtection() {
        try {
            clsTileEnderQuarry = (Class<? extends TileEntity>)Class.forName("com.rwtema.extrautils.tileentity.enderquarry.TileEntityEnderQuarry");
            clsTileEnderPump = (Class<? extends TileEntity>)Class.forName("com.rwtema.extrautils.tileentity.TileEntityEnderThermicLavaPump");

            trackedTileEntities.add(clsTileEnderPump);
            trackedTileEntities.add(clsTileEnderQuarry);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            MyTown.instance.log.error("Failed to load ExtraUtilities classes!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {
        if(clsTileEnderQuarry.isAssignableFrom(te.getClass())) {
            // Ender Quarry
            try {
                Field minX = clsTileEnderQuarry.getDeclaredField("min_x");
                minX.setAccessible(true);
                Field minZ = clsTileEnderQuarry.getDeclaredField("min_z");
                minZ.setAccessible(true);
                Field maxX = clsTileEnderQuarry.getDeclaredField("max_x");
                maxX.setAccessible(true);
                Field maxZ = clsTileEnderQuarry.getDeclaredField("max_z");
                maxZ.setAccessible(true);

                List<ChunkPos> chunks = Utils.getChunksInBox(minX.getInt(te), minZ.getInt(te), maxX.getInt(te), maxZ.getInt(te));
                for(ChunkPos cp : chunks) {
                    Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, cp.getX(), cp.getZ());
                    if(town != null) {
                        if(!((Flag<Boolean>)town.getFlag(FlagType.breakBlocks)).getValue()) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(clsTileEnderPump.isAssignableFrom(te.getClass())) {
            try {
                Field chunkX = clsTileEnderPump.getDeclaredField("chunk_x");
                chunkX.setAccessible(true);
                Field chunkZ = clsTileEnderPump.getDeclaredField("chunk_z");
                chunkZ.setAccessible(true);

                Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, chunkX.getInt(te), chunkZ.getInt(te));
                if(town != null && !((Flag<Boolean>)town.getFlag(FlagType.pumps)).getValue()) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public List<FlagType> getFlagTypeForTile(Class<? extends TileEntity> te) {
        List<FlagType> list = new ArrayList<FlagType>();
        if(clsTileEnderQuarry.isAssignableFrom(te))
            list.add(FlagType.breakBlocks);
        else if(clsTileEnderPump.isAssignableFrom(te))
            list.add(FlagType.pumps);
        return list;
    }
}
