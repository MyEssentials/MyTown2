package mytown.util;

/**
 * Created by AfterWind on 9/15/2014.
 * Class which stores information about a chunk
 */
public class ChunkPos {
    private final int x;
    private final int z;

    public ChunkPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
