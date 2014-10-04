package mytown.api.interfaces;

import mytown.entities.Plot;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;

/**
 * Created by AfterWind on 9/4/2014.
 * If somebody comes up with a better name please change xD
 */
public interface IBlockWhitelister {
    boolean startBlockSelection(FlagType flagType, String townName, boolean inPlot);
}
