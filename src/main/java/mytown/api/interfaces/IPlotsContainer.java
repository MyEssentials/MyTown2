package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Plot;

/**
 * Represents an object that can hold Plots
 */
public interface IPlotsContainer {

    void addPlot(Plot plot);

    void removePlot(Plot plot);

    boolean hasPlot(Plot plot);

    ImmutableList<Plot> getPlots();

    /**
     * Returns the Plot in the dimension, and at the given coords (non-chunk coords)
     */
    Plot getPlotAtCoords(int dim, int x, int y, int z);

    Plot getPlot(String name);
}
