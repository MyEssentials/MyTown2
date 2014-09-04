package mytown.entities.interfaces;

import mytown.entities.Plot;

/**
 * Created by AfterWind on 9/4/2014.
 * If somebody comes up with a better name please change xD
 */
public interface IBlockWhitelister {

    boolean startSelection(String flagName, boolean remove, boolean inPlot);
    boolean select(int dim, int x, int y, int z, boolean remove, boolean inPlot);
}
