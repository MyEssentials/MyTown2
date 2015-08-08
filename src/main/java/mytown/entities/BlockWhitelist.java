package mytown.entities;

import mytown.entities.flag.FlagType;

/**
 * Stores coords and flagname to give whitelist for that block on the flag specified
 */
public class BlockWhitelist {

    private final int dim;
    private final int x;
    private final int y;
    private final int z;
    private boolean isDeleted;
    /**
     * Database id used to retain this object in.
     */
    private int dbID;
    private FlagType flagType;

    public BlockWhitelist(int dim, int x, int y, int z, FlagType flagType) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.flagType = flagType;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public FlagType getFlagType() {
        return this.flagType;
    }

    public void setDbID(int id) {
        this.dbID = id;
    }

    public int getDbID() {
        return this.dbID;
    }

    public int getDim() {
        return dim;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
}
