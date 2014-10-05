package mytown.protection;

import buildcraft.builders.TileAbstractBuilder;
import buildcraft.core.BlockIndex;
import buildcraft.core.Box;
import buildcraft.factory.TilePump;
import mytown.MyTown;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.Wild;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.util.ChunkPos;
import mytown.util.Utils;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 9/15/2014.
 * All buildcraft protection
 */
public class BuildCraftFactoryProtection extends Protection {

    Class<? extends TileEntity> clsTileAbstractBuilder;
    Class<? extends TileEntity> clsTilePump;

    public BuildCraftFactoryProtection() {
        // We need reflection only for loading the mod
        clsTileAbstractBuilder = TileAbstractBuilder.class;
        clsTilePump = TilePump.class;

        trackedTileEntities.add(clsTilePump);
        trackedTileEntities.add(clsTileAbstractBuilder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {

        if(clsTileAbstractBuilder.isAssignableFrom(te.getClass())) {
            // In case we add more tile entities to check
            Box box = null;
            try {
                box = (Box) clsTileAbstractBuilder.getMethod("getBox").invoke(te);
            } catch (Exception e) {
                MyTown.instance.log.info("Failed to get methods for a Buildcraft tile entity!");
                e.printStackTrace();
                return false;
            }

            List<ChunkPos> chunks = Utils.getChunksInBox(box.xMin, box.zMin, box.xMax, box.zMax);
            for(ChunkPos p : chunks) {
                TownBlock block = MyTownUniverse.getInstance().getBlocksMap().get(String.format(TownBlock.keyFormat, te.getWorldObj().provider.dimensionId, p.getX(), p.getZ()));
                if(block == null) {
                    if(!(Boolean) Wild.getInstance().getFlag(FlagType.breakBlocks).getValue()) {
                        MyTown.instance.log.info("A buildcraft machine at coordonates " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " in dimension " + te.getWorldObj().provider.dimensionId + " tried to bypass protection!");
                        return true;
                    }
                } else {
                    // Directly from the town, not checking per plot since it's quite the pain
                    boolean bcFlag = (Boolean)block.getTown().getValue(FlagType.breakBlocks);
                    if(!bcFlag) {
                        block.getTown().notifyEveryone(FlagType.breakBlocks.getLocalizedTownNotification());
                        MyTown.instance.log.info("A buildcraft machine at coordonates " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " in dimension " + te.getWorldObj().provider.dimensionId + " tried to bypass protection!");
                        return true;
                    }
                }
            }
        } else if(clsTilePump.isAssignableFrom(te.getClass())) {
            try {
                Method method = clsTilePump.getMethod("getNextIndexToPump", Boolean.class);
                method.setAccessible(true);
                // Invoke method: getting the next block to pump, but not removing it
                BlockIndex blockIndex = (BlockIndex)method.invoke(te, false);
                Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, blockIndex.x >> 4, blockIndex.z >> 4);
                if(town == null) {
                    if(!(Boolean)Wild.getInstance().getFlag(FlagType.pumps).getValue()) {
                        MyTown.instance.log.info("A buildcraft machine at coordonates " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " in dimension " + te.getWorldObj().provider.dimensionId + " tried to bypass protection!");
                        return true;
                    }
                } else {
                    if (!((Boolean)town.getValue(FlagType.pumps))) {
                        MyTown.instance.log.info("A buildcraft machine at coordonates " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " in dimension " + te.getWorldObj().provider.dimensionId + " tried to bypass protection!");
                        town.notifyEveryone(FlagType.pumps.getLocalizedTownNotification());
                        return true;
                    }
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
        if(clsTileAbstractBuilder.isAssignableFrom(te)) {
            list.add(FlagType.breakBlocks);
        } else if(clsTilePump.isAssignableFrom(te)) {
            list.add(FlagType.pumps);
        }
        return list;
    }
}
