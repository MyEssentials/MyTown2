package mytown.protection.thread;

import mytown.entities.Resident;
import mytown.protection.ProtectionUtils;
import mytown.core.entities.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * A thread which checks if there's TileEntity on given block.
 * If there isn't found one in 1 second it exits.
 */
public class ThreadPlacementCheck extends Thread {
    private static final int TIMEOUT_IN_MS = 1000;

    private final Resident res;
    private final BlockPos position;

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
            if(System.currentTimeMillis() - startTime >= TIMEOUT_IN_MS) {
                ProtectionUtils.placementThreadTimeout();
                return;
            }
            te = world.getTileEntity(position.x, position.y, position.z);
        }
        ProtectionUtils.addTileEntity(te, res);
    }
}
