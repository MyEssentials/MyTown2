package mytown.entities.interfaces;

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

    boolean makePlotFromSelection(String plotName);

    void expandSelectionVert();

    void resetSelection();
}

