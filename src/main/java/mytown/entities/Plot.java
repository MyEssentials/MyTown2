package mytown.entities;

// TODO Implement PlotType

import com.google.common.collect.ImmutableList;
import mytown.entities.flag.Flag;
import mytown.entities.interfaces.IHasFlags;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joe Goett
 */
public class Plot implements IHasFlags{
    private int dim, x1, y1, z1, x2, y2, z2;
    private Town town;
    private String key, name;

    public Plot(String name, Town town, int dim, int x1, int y1, int z1, int x2, int y2, int z2) {
        if (x1 > x2) {
            int aux = x2;
            x2 = x1;
            x1 = aux;
        }

        if (z1 > z2) {
            int aux = z2;
            z2 = z1;
            z1 = aux;
        }

        if (y1 > y2) {
            int aux = y2;
            y2 = y1;
            y1 = aux;
        }
        // Second parameter is always highest
        this.name = name;
        this.town = town;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.dim = dim;

        updateKey();
    }

    public int getDim() {
        return dim;
    }

    public int getStartX() {
        return x1;
    }

    public int getStartY() {
        return y1;
    }

    public int getStartZ() {
        return z1;
    }

    public String getStartCoordString() {
        return String.format("%s, %s, %s", x1, y1, z1);
    }

    public int getEndX() {
        return x2;
    }

    public int getEndY() {
        return y2;
    }

    public int getEndZ() {
        return z2;
    }

    public String getEndCoordString() {
        return String.format("%s, %s, %s", x2, y2, z2);
    }

    public int getStartChunkX() {
        return x1 >> 4;
    }

    public int getStartChunkZ() {
        return z1 >> 4;
    }

    public int getEndChunkX() {
        return x2 >> 4;
    }

    public int getEndChunkZ() {
        return z2 >> 4;
    }

    public Town getTown() {
        return town;
    }

    public String getKey() {
        return key;
    }

    private void updateKey() {
        key = String.format("%s;%s;%s;%s;%s;%s;%s", dim, x1, y1, z1, x2, y2, z2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks if the coords are within this plot and in the same dimension
     *
     * @param dim
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isCoordWithin(int dim, int x, int y, int z) { // TODO Is dim really needed?
        return dim == this.dim && (x > x1 && x < x2) && (y > y1 && y < y2) && (z > z1 && z < z2);
    }

    @Override
    public String toString() {
        return String.format("Plot: {Name: %s, Dim: %s, Start: [%s, %s, %s], End: [%s, %s, %s]}", name, dim, x1, y1, z1, x2, y2, z2);
    }

    /* ---- IHasFlags ----- */

    private List<Flag> flags = new ArrayList<Flag>();

    @Override
    public void addFlag(Flag flag) {
        flags.add(flag);
    }

    @Override
    public boolean hasFlag(String name) {
        for(Flag flag : flags)
            if(flag.getName().equals(name))
                return true;
        return false;
    }

    @Override
    public ImmutableList<Flag> getFlags() {
        return ImmutableList.copyOf(flags);
    }

    @Override
    public Flag getFlag(String name) {
        for(Flag flag : flags)
            if(flag.getName().equals(name))
                return flag;
        return null;
    }
}
