package mytown.api.interfaces;

import mytown.entities.Plot;

/**
 * Interface for selecting plots.
 *
 * @author AfterWind
 */
public interface IPlotSelector {

    void startPlotSelection();

    boolean selectBlockForPlot(int dim, int x, int y, int z);

    boolean isFirstPlotSelectionActive();

    boolean isSecondPlotSelectionActive();

    Plot makePlotFromSelection(String plotName);

    void expandSelectionVert();

    void resetSelection(boolean resetBlocks);
}

