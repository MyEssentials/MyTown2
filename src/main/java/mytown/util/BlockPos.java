package mytown.util;

/**
 * Helper class for storing position of a block
 */
public class BlockPos {
    public final int dim;
    public final int x;
    public final int y;
    public final int z;

    public BlockPos(int x, int y, int z, int dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }
}
