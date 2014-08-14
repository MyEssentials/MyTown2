package mytown.entities.interfaces;

import com.google.common.collect.ImmutableCollection;
import mytown.entities.Plot;

import java.util.Collection;

/**
 * @author Joe Goett
 */
public interface IHasPlots {
    /**
     * Adds the Plot to this entity
     *
     * @param plot
     */
    public void addPlot(Plot plot);

    /**
     * Removes this Plot from this entity
     *
     * @param plot
     */
    public void removePlot(Plot plot);

    /**
     * Checks if this entity has the Plot
     *
     * @param plot
     * @return
     */
    public boolean hasPlot(Plot plot);

    /**
     * Returns the Collection of Plots
     *
     * @return
     */
    public ImmutableCollection<Plot> getPlots();

    /**
     * Returns the Plot in the dimension, and at the given coords (non-chunk coords)
     *
     * @param dim
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Plot getPlotAtCoord(int dim, int x, int y, int z);
}
