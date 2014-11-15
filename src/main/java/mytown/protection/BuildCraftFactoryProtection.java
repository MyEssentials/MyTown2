package mytown.protection;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import mytown.MyTown;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.Wild;
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

    MethodAccess mAccessPump, mAccessBuilder;
    FieldAccess fAccessBlockIndex, fAccessBox;

    @SuppressWarnings("unchecked")
    public BuildCraftFactoryProtection() {
        // We need reflection only for loading the mod
        try {
            clsTilePump = (Class<? extends TileEntity>)Class.forName("buildcraft.factory.TilePump");
            clsTileAbstractBuilder= (Class<? extends TileEntity>)Class.forName("buildcraft.core.builders.TileAbstractBuilder");
            Class clsBlockIndex = Class.forName("buildcraft.api.core.BlockIndex");
            Class clsBox = Class.forName("buildcraft.core.Box");

            mAccessPump = MethodAccess.get(clsTilePump);
            mAccessBuilder = MethodAccess.get(clsTileAbstractBuilder);

            fAccessBlockIndex = FieldAccess.get(clsBlockIndex);
            fAccessBox = FieldAccess.get(clsBox);

            trackedTileEntities.add(clsTilePump);
            trackedTileEntities.add(clsTileAbstractBuilder);
        } catch (Exception e) {
            MyTown.instance.log.error("Failed to get BuildCraft|Factory classes!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {

        if (clsTileAbstractBuilder.isAssignableFrom(te.getClass())) {
            // In case we add more tile entities to check
            Object box = null;
            try {
                box = clsTileAbstractBuilder.getMethod("getBox").invoke(te);
            } catch (Exception e) {
                MyTown.instance.log.info("Failed to get methods for a Buildcraft tile entity!");
                e.printStackTrace();
                return false;
            }

            List<ChunkPos> chunks = Utils.getChunksInBox(fAccessBox.getInt(box, fAccessBox.getIndex("xMin")), fAccessBox.getInt(box, fAccessBox.getIndex("zMin")), fAccessBox.getInt(box, fAccessBox.getIndex("xMax")), fAccessBox.getInt(box, fAccessBox.getIndex("zMax")));
            for (ChunkPos p : chunks) {
                TownBlock block = MyTownUniverse.getInstance().getBlocksMap().get(String.format(TownBlock.keyFormat, te.getWorldObj().provider.dimensionId, p.getX(), p.getZ()));
                if (block == null) {
                    if (!(Boolean) Wild.getInstance().getFlag(FlagType.breakBlocks).getValue()) {
                        MyTown.instance.log.info("A buildcraft machine at coordonates " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " in dimension " + te.getWorldObj().provider.dimensionId + " tried to bypass protection!");
                        return true;
                    }
                } else {
                    // Directly from the town, not checking per plot since it's quite the pain
                    boolean bcFlag = (Boolean) block.getTown().getValue(FlagType.breakBlocks);
                    if (!bcFlag) {
                        block.getTown().notifyEveryone(FlagType.breakBlocks.getLocalizedTownNotification());
                        MyTown.instance.log.info("A buildcraft machine at coordonates " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " in dimension " + te.getWorldObj().provider.dimensionId + " tried to bypass protection!");
                        return true;
                    }
                }
            }
        } else if (clsTilePump.isAssignableFrom(te.getClass())) {
            try {
                Method method = clsTilePump.getMethod("getNextIndexToPump", Boolean.class);
                method.setAccessible(true);
                // Invoke method: getting the next block to pump, but not removing it
                Object blockIndex = method.invoke(te, false);
                Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, fAccessBlockIndex.getInt(blockIndex, fAccessBlockIndex.getIndex("x")) >> 4, fAccessBlockIndex.getInt(blockIndex, fAccessBlockIndex.getIndex("z")) >> 4);
                if (town == null) {
                    if (!(Boolean) Wild.getInstance().getFlag(FlagType.pumps).getValue()) {
                        MyTown.instance.log.info("A buildcraft machine at coordonates " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " in dimension " + te.getWorldObj().provider.dimensionId + " tried to bypass protection!");
                        return true;
                    }
                } else {
                    if (!((Boolean) town.getValue(FlagType.pumps))) {
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
        if (clsTileAbstractBuilder.isAssignableFrom(te)) {
            list.add(FlagType.breakBlocks);
        } else if (clsTilePump.isAssignableFrom(te)) {
            list.add(FlagType.pumps);
        }
        return list;
    }
}
