package mytown.interfaces;

/**
 * Interface for selecting plots.
 * 
 * @author AfterWind
 * 
 */
public interface IPlotSelector {

	boolean selectBlockForPlot(int dim, int x, int y, int z);

	boolean isFirstPlotSelectionActive();

	boolean isSecondPlotSelectionActive();

	boolean makePlotFromSelection();

	void expandSelectionVert();

	void resetSelection();
}
