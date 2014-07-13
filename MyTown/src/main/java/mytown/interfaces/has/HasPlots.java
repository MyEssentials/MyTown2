package mytown.interfaces.has;

import java.util.ArrayList;
import java.util.List;

import mytown.interfaces.ITownPlot;

public class HasPlots {
	protected List<ITownPlot> plots = new ArrayList<ITownPlot>();
	
	/**
	 * Adds the {@link ITownPlot}
	 * @param plot
	 */
	public void addPlot(ITownPlot plot) {
		plots.add(plot);
	}
	
	/**
	 * Removes the {@link ITownPlot}
	 * @param plot
	 */
	public void removePlot(ITownPlot plot) {
		plots.remove(plot);
	}
	
	/**
	 * Returns the list of {@link ITownPlot}
	 * @return
	 */
	public List<ITownPlot> getPlots() {
		return plots;
	}

	/**
	 * Gets the plot at the specified location
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public ITownPlot getPlotAtCoords(int x, int y, int z) {
		for (ITownPlot p : plots) {
			if (p.isBlockInsidePlot(x, y, z))
				return p;
		}
		return null;
	}
}