package mytown.protection;

import buildcraft.transport.BlockGenericPipe;
import cpw.mods.fml.common.registry.GameRegistry;
import mytown.MyTown;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.mod.BuildCraftTrasportationProxy;
import mytown.util.BlockPos;
import mytown.util.Utils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;

import java.util.List;

/**
 * Created by AfterWind on 9/21/2014.
 * Protection for pipes for buildcraft
 */
public class BuildCraftTransportProtection extends Protection {

    Class<? extends TileEntity> clsTileGenericPipe;

    @SuppressWarnings("unchecked")
    public BuildCraftTransportProtection() {
        try {
            clsTileGenericPipe = (Class<? extends TileEntity>)Class.forName("buildcraft.transport.TileGenericPipe");

            trackedTileEntities.add(clsTileGenericPipe);
        } catch (ClassNotFoundException e) {
            MyTown.instance.log.error("Failed to load bc-transport classes!");
            e.printStackTrace();
        }

        activatedBlocks.add(GameRegistry.findBlock(BuildCraftTrasportationProxy.MOD_ID, "tile.pipeBlock"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {

        TownBlock bl = getDatasource().getBlock(te.getWorldObj().provider.dimensionId, te.xCoord >> 4, te.zCoord >> 4);
        if(clsTileGenericPipe.isAssignableFrom(te.getClass()) && bl == null) {
            List<BlockPos> blocksNearby = Utils.getBlockAndPositionNearby(new BlockPos(te.xCoord, te.yCoord, te.zCoord, te.getWorldObj().provider.dimensionId));
            for (BlockPos bn : blocksNearby) {
                Town town = Utils.getTownAtPosition(bn.dim, bn.x >> 4, bn.z >> 4);
                TileEntity nearbyTile = DimensionManager.getWorld(bn.dim).getTileEntity(bn.x, bn.y, bn.z);
                if (nearbyTile != null && clsTileGenericPipe.isAssignableFrom(nearbyTile.getClass())) {
                    boolean bcFlowFlag = (Boolean)town.getValueAtCoords(bn.dim, bn.x, bn.y, bn.z, FlagType.bcPipeFlow);
                    if (!bcFlowFlag) {
                        town.notifyEveryone(FlagType.bcPipeFlow.getLocalizedProtectionDenial());
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
