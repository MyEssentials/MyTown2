package mytown.api.container;

import mytown.entities.BlockWhitelist;
import mytown.entities.flag.FlagType;

import java.util.ArrayList;
import java.util.Iterator;

public class BlockWhitelistsContainer extends ArrayList<BlockWhitelist> {

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
