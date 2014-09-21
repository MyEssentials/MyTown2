package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.api.interfaces.IHasPlots;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joe Goett
 */
public class Block implements IHasPlots {
    public static final String keyFormat = "%s;%s;%s";

    private int dim, x, z;
    private Town town;
    private String key;

    public Block(int dim, int x, int z, Town town) {
        this.dim = dim;
        this.x = x;
        this.z = z;
        this.town = town;
        updateKey();
    }

    public int getDim() {
        return dim;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String getCoordString() {
        return String.format("%s, %s", x, z);
    }

    public Town getTown() {
        return town;
    }

    public String getKey() {
        return key;
    }

    private void updateKey() {
        key = String.format(keyFormat, dim, x, z);
    }

    @Override
    public String toString() {
        return String.format("Block: {Dim: %s, X: %s, Z: %s, Town: %s, Plots: %s}", dim, x, z, town.getName(), getPlots().size());
    }

    /* ----- IHasPlots -----
    This helps improve performance by allowing us to get Plots directly from the Block.
    Since Blocks are just chunks, its much quicker to grab it and return a Collection of Plots the Block has.
    */

    private List<Plot> plots = new ArrayList<Plot>();

    @Override
    public void addPlot(Plot plot) {
        if (x >= plot.getStartChunkX() && x <= plot.getEndChunkX() && z >= plot.getStartChunkZ() && z <= plot.getEndChunkZ()) { // TODO Not really sure if this will work. Need to test!
            plots.add(plot);
        }
    }

    @Override
    public void removePlot(Plot plot) {
        plots.remove(plot);
    }

    @Override
    public boolean hasPlot(Plot plot) {
        return plots.contains(plot);
    }

    @Override
    public ImmutableList<Plot> getPlots() {
        return ImmutableList.copyOf(plots);
    }

    @Override
    public Plot getPlotAtCoords(int dim, int x, int y, int z) {
        for (Plot plot : plots) {
            if (plot.isCoordWithin(dim, x, y, z)) {
                return plot;
            }
        }
        return null;
    }

    /* ----- Helpers ----- */

    /**
     * Checks if the point is inside this Block
     *
     * @param dim
     * @param x
     * @param z
     * @return
     */
    public boolean isPointIn(int dim, float x, float z) {
        return isChunkIn(dim, ((int) x) >> 4, ((int) z) >> 4);
    }

    /**
     * Checks if the chunk is this Block
     *
     * @param dim
     * @param cx
     * @param cz
     * @return
     */
    public boolean isChunkIn(int dim, int cx, int cz) {
        return (dim == this.dim && cx == x && cz == z);
    }
}
