package mytown.util;

/**
 * Helper class for storing position of an entity
 */
public class EntityPos {
    public final int dim;
    public final double x;
    public final double y;
    public final double z;

    public EntityPos(double x, double y, double z, int dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }
}
