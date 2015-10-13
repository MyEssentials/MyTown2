package mytown.entities;


import mytown.entities.flag.FlagType;

import java.util.ArrayList;
import java.util.Iterator;

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

    public static class Container extends ArrayList<BlockWhitelist> {

        public boolean contains(int dim, int x, int y, int z, FlagType flagType) {
            for (BlockWhitelist bw : this) {
                if (bw.getDim() == dim && bw.getX() == x && bw.getY() == y && bw.getZ() == z && bw.getFlagType().equals(flagType)) {
                    return true;
                }
            }
            return false;
        }

        public void remove(int dim, int x, int y, int z, FlagType flagType) {
            for (Iterator<BlockWhitelist> it = iterator(); it.hasNext(); ) {
                BlockWhitelist bw = it.next();
                if (bw.getDim() == dim && bw.getX() == x && bw.getY() == y && bw.getZ() == z && bw.getFlagType().equals(flagType)) {
                    it.remove();
                }
            }
        }

        public BlockWhitelist get(int dim, int x, int y, int z, FlagType flagType) {
            for (BlockWhitelist bw : this) {
                if (bw.getDim() == dim && bw.getX() == x && bw.getY() == y && bw.getZ() == z && bw.getFlagType().equals(flagType)) {
                    return bw;
                }
            }
            return null;
        }

        public void add(int dim, int x, int y, int z, FlagType flagType) {
            add(new BlockWhitelist(dim, x, y, z, flagType));
        }
    }
}
