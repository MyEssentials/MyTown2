package mytown.interfaces;

import java.util.List;

import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.town.Town;

/**
 * Interface for plots.
 * 
 * @author AfterWind
 * 
 */
public interface ITownPlot {

	Town getTown();

    List<Resident> getOwners();
	boolean addOwner(Resident owner);
    boolean removeOwner(Resident owner);

    List<Resident> getWhitelistedResidents();
    boolean addToWhitelist(Resident resident);
    boolean removeFromWhitelist(Resident resident);


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
