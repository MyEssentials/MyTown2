package mytown.interfaces;

import java.util.List;
import java.util.Map;

import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.town.Town;

public interface ITownPlot {
	
	Town getTown();
	Resident getOwner();
	
	int getDim();
	
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
	
	ITownFlag getFlag(String flagName);
	
	List<ITownFlag> getFlags();
	List<TownBlock> getEncompasingBlocks();
}
