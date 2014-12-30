package mytown.protection;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import mytown.MyTown;
import mytown.datasource.MyTownUniverse;
import mytown.entities.TownBlock;
import mytown.entities.Wild;
import mytown.entities.flag.FlagType;
import mytown.util.ChunkPos;
import mytown.util.MyTownUtils;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 9/15/2014.
 * All buildcraft protection
 */
public class BuildCraftFactoryProtection extends Protection {

    Class<? extends TileEntity> clsTileAbstractBuilder;
    Class<? extends TileEntity> clsTilePump;

    MethodAccess mAccessPump, mAccessIBoxProvider;
    FieldAccess fAccessBlockIndex, fAccessBox;

    @SuppressWarnings("unchecked")
    public BuildCraftFactoryProtection() {
        try {
            clsTilePump = (Class<? extends TileEntity>)Class.forName("buildcraft.factory.TilePump");
            clsTileAbstractBuilder= (Class<? extends TileEntity>)Class.forName("buildcraft.core.builders.TileAbstractBuilder");
            Class clsBlockIndex = Class.forName("buildcraft.api.core.BlockIndex");
            Class clsBox = Class.forName("buildcraft.core.Box");
            Class clsIBoxProvider = Class.forName("buildcraft.core.IBoxProvider");

            mAccessPump = MethodAccess.get(clsTilePump);
            mAccessIBoxProvider = MethodAccess.get(clsIBoxProvider);

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
            Object box;
            try {
                box = mAccessIBoxProvider.invoke(te, "getBox");
            } catch (Exception e) {
                MyTown.instance.log.info("Failed to get methods for a Buildcraft tile entity!");
                e.printStackTrace();
                return false;
            }
            //MyTown.instance.log.info("Checking builder...");
            int xMin = fAccessBox.getInt(box, fAccessBox.getIndex("xMin"));
            int zMin = fAccessBox.getInt(box, fAccessBox.getIndex("zMin"));
            int xMax = fAccessBox.getInt(box, fAccessBox.getIndex("xMax"));
            int zMax = fAccessBox.getInt(box, fAccessBox.getIndex("zMax"));

            List<ChunkPos> chunks = MyTownUtils.getChunksInBox(xMin, zMin, xMax, zMax);
            //MyTown.instance.log.info("Got box: " + xMin + ", " + zMin + " : " + xMax + ", " + zMax);
            for (ChunkPos p : chunks) {
                TownBlock block = MyTownUniverse.getInstance().getBlocksMap().get(String.format(TownBlock.keyFormat, te.getWorldObj().provider.dimensionId, p.getX(), p.getZ()));
                if (block == null) {
                    if (!(Boolean) Wild.getInstance().getFlag(FlagType.modifyBlocks).getValue()) {
                        MyTown.instance.log.info("A buildcraft machine at coordonates " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " in dimension " + te.getWorldObj().provider.dimensionId + " tried to bypass protection!");
                        return true;
                    }
                } else {
                    // Directly from the town, not checking per plot since it's quite the pain
                    boolean bcFlag = (Boolean) block.getTown().getValue(FlagType.modifyBlocks);
                    if (!bcFlag) {
                        block.getTown().notifyEveryone(FlagType.modifyBlocks.getLocalizedTownNotification());
                        MyTown.instance.log.info("A buildcraft machine at coordonates " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " in dimension " + te.getWorldObj().provider.dimensionId + " tried to bypass protection!");
                        return true;
                    }
                }
            }
        } else if (clsTilePump.isAssignableFrom(te.getClass())) {
            /*
            try {
                Method method = clsTilePump.getDeclaredMethod("getNextIndexToPump", Boolean.class);
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
            */
        }
        return false;
    }

    @Override
    public List<FlagType> getFlagTypeForTile(Class<? extends TileEntity> te) {
        List<FlagType> list = new ArrayList<FlagType>();
        if (clsTileAbstractBuilder.isAssignableFrom(te)) {
            list.add(FlagType.modifyBlocks);
        } else if (clsTilePump.isAssignableFrom(te)) {
            list.add(FlagType.pumps);
        }
        return list;
    }
}
