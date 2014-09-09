package mytown.entities.interfaces;

import mytown.entities.Plot;
import mytown.entities.Town;

/**
 * Created by AfterWind on 9/4/2014.
 * If somebody comes up with a better name please change xD
 */
public interface IBlockWhitelister {
    boolean startBlockSelection(String flagName, String townName, boolean inPlot);
}
