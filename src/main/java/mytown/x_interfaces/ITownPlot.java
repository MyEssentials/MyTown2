package mytown.x_interfaces;

import mytown.x_entities.Resident;
import mytown.x_entities.TownBlock;
import mytown.x_entities.town.Town;

import java.util.List;

/**
 * Interface for plots.
 *
 * @author AfterWind
 */
public interface ITownPlot {

    Town getTown();

    Resident getOwner();

    boolean setOwner(Resident owner);

    int getDim();

    String getName();

    boolean setName(String name);

    int getStartX();

    int getStartZ();

    int getStartY();

    int getEndX();

    int getEndZ();

    int getEndY();

    // Not sure if needed...
    int getStartChunkX();

    int getStartChunkZ();

    int getEndChunkX();

    int getEndChunkZ();

    String getKey();

    void updateKey();

    boolean isBlockInsidePlot(int x, int y, int z);

    boolean addFlag(ITownFlag flag);

    boolean removeFlag(String flagName);

    ITownFlag getFlag(String flagName);

    List<ITownFlag> getFlags();

    List<TownBlock> getEncompasingBlocks();
}
