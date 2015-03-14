package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Plot;

/**
 * @author Joe Goett
 * Represents an object that can hold Plots
 */
public interface IHasPlots {

    public void addPlot(Plot plot);

    public void removePlot(Plot plot);

    public boolean hasPlot(Plot plot);

    public ImmutableList<Plot> getPlots();

    /**
     * Returns the Plot in the dimension, and at the given coords (non-chunk coords)
     */
    public Plot getPlotAtCoords(int dim, int x, int y, int z);
}
