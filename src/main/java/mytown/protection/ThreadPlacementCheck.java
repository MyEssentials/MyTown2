package mytown.protection;

import mytown.entities.Resident;
import mytown.util.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Created by AfterWind on 3/22/2015.
 * A thread which checks if there's TileEntity on given block.
 * If there isn't found one in 1 second it exits.
 */
public class ThreadPlacementCheck extends Thread {
    public static int timeOutInMS = 1000;

    private Resident res;
    private BlockPos position;

    public ThreadPlacementCheck(Resident res, int x, int y, int z, int dim) {
        super();
        this.res = res;
        this.position = new BlockPos(x, y, z, dim);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        World world = DimensionManager.getWorld(position.dim);
        TileEntity te = null;
        while(te == null) {
            if(System.currentTimeMillis() - startTime >= timeOutInMS) {
                ProtectionUtils.placementThreadTimeout();
                return;
            }
            te = world.getTileEntity(position.x, position.y, position.z);
        }
        ProtectionUtils.addTileEntity(te, res);
    }
}
