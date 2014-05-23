package mytown.entities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.entities.flag.EnumFlagValue;
import mytown.entities.flag.TownFlag;
import mytown.interfaces.ITownFlag;
import mytown.interfaces.ITownPlot;

// TODO Add Comments

/**
 * Defines a Town
 * 
 * @author Joe Goett
 */
public class Town implements Comparable<Town> {
	
	private String name;
	private int extraBlocks = 0;
	private List<Rank> ranks;
	private List<ITownFlag> flags;
	private List<ITownPlot> townPlots;
	
	/**
	 * Creates a town with the given name
	 * 
	 * @param name
	 */
	public Town(String name) {
		this(name, 0);
	}

	public Town(String name, int extraBlocks) {
		this.name = name;
		this.extraBlocks = extraBlocks;
		
		ranks = new ArrayList<Rank>();
		townPlots = new ArrayList<ITownPlot>();
		
		this.initTownFlags();
	}

	public void initTownFlags() {
		flags = new ArrayList<ITownFlag>();
		
		flags.add(new TownFlag("mobs", "Controls mobs spawning", EnumFlagValue.True));
		flags.add(new TownFlag("accessLevel", "Access level for non-residents", EnumFlagValue.Enter));
		flags.add(new TownFlag("explosions", "Controls if explosions can occur", EnumFlagValue.True));
	}
	
	/**
	 * Returns the name of the town
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	// //////////////////////////////////////
	// Nations
	// //////////////////////////////////////
	private List<Nation> nations = new ArrayList<Nation>();

	public void addNations(List<Nation> nations) {
		this.nations.addAll(nations);
	}

	public void addNation(Nation nation) {
		nations.add(nation);
	}

	public void removeNation(Nation nation) {
		nations.remove(nation);
	}

	public boolean hasNation(Nation nation) {
		return nations.contains(nation);
	}

	public List<Nation> getNations() {
		return nations;
	}

	public void promoteTown(Nation nation, Nation.Rank rank) {
		if (!hasNation(nation)) return; // TODO Log/Throw Exception
		nation.setTownRank(this, rank);
	}

	public Nation.Rank getNationRank(Nation nation) {
		return nation.getTownRank(this);
	}

	// //////////////////////////////////////
	// Blocks
	// //////////////////////////////////////
	private List<TownBlock> townBlocks = new ArrayList<TownBlock>();

	/**
	 * Adds the given TownBlocks to this Town
	 * 
	 * @param townBlocks
	 */
	public void addTownBlocks(List<TownBlock> townBlocks) {
		this.townBlocks.addAll(townBlocks);
	}

	/**
	 * Add a TownBlocks to this Town
	 * 
	 * @param block
	 */
	public void addTownBlock(TownBlock block) {
		townBlocks.add(block);
	}

	/**
	 * Remove a TownBlocks from this Town
	 * 
	 * @param block
	 */
	public void removeTownBlock(TownBlock block) {
		townBlocks.remove(block);
	}

	/**
	 * Checks if this Town has the TownBlocks
	 * 
	 * @param block
	 * @return
	 */
	public boolean hasTownBlock(TownBlock block) {
		return townBlocks.contains(block);
	}

	/**
	 * Returns all TownBlocks this Town has
	 * 
	 * @return
	 */
	public List<TownBlock> getTownBlocks() {
		return townBlocks;
	}

	/**
	 * Returns the amount of extra blocks this town is given
	 * 
	 * @return
	 */
	public int getExtraBlocks() {
		return extraBlocks;
	}

	// //////////////////////////////////////
	// Residents
	// //////////////////////////////////////
	private Map<Resident, Rank> residents = new Hashtable<Resident, Rank>();

	/**
	 * Returns the Residents
	 * 
	 * @return
	 */
	public List<Resident> getResidents() {
		List<Resident> res = new ArrayList<Resident>();
		res.addAll(residents.keySet());
		return res;
	}

	/**
	 * Adds the Resident with Rank
	 * 
	 * @param resident
	 * @param rank
	 */
	public void addResident(Resident resident, Rank rank) {
		residents.put(resident, rank);
	}

	/**
	 * Removes the Resident
	 * 
	 * @param resident
	 */
	public void removeResident(Resident resident) {
		residents.remove(resident);
		resident.removeResidentFromTown(this);
	}

	/**
	 * Checks if the Resident is part of this Town
	 * 
	 * @param resident
	 * @return
	 */
	public boolean hasResident(Resident resident) {
		return residents.containsKey(resident);
	}

	/**
	 * Checks if the Resident is part of this Town
	 * 
	 * @param resident
	 * @return
	 */

	public boolean hasResident(String UUID) {
		for (Resident r : residents.keySet())
			if (r.getUUID().equals(UUID)) return true;
		return false;
	}

	/**
	 * Promotes the Resident to the Rank
	 * 
	 * @param resident
	 * @param rank
	 */
	public void promoteResident(Resident resident, Rank rank) {
		if (!hasResident(resident)) return; // TODO Log/Throw Exception
		addResident(resident, rank);
	}

	public Rank getResidentRank(Resident resident) {
		if (hasResident(resident)) {
			return residents.get(resident);
		} else {
			return getRank("Outsider");
		}
	}

	// //////////////////////////////////////
	// Ranks
	// //////////////////////////////////////

	/**
	 * Gets all ranks
	 * 
	 * @return
	 */
	public List<Rank> getRanks() {
		return this.ranks;
	}

	/**
	 * Gets a rank
	 * 
	 * @param name
	 * @return
	 */
	public Rank getRank(String name) {
		for (Rank r : ranks)
			if (r.getName().equals(name)) return r;
		return null;
	}

	public void removeRank(Rank rank) {
		ranks.remove(rank);
	}

	/**
	 * Adds a rank
	 * 
	 * @param rank
	 */
	public void addRank(Rank rank) {
		for (Rank r : ranks)
			if (r.getName() == rank.getName()) return;
		ranks.add(rank);
	}

	/**
	 * Checks if the Rank exists in this town
	 * 
	 * @param rank
	 * @return
	 */
	public boolean hasRank(Rank rank) {
		return this.ranks.contains(rank);
	}

	public boolean hasRankName(String name) {
		for (Rank r : ranks)
			if (r.getName().equals(name)) return true;
		return false;
	}

	// //////////////////////////////////////
	// Flags
	// //////////////////////////////////////
	
	/**
	 * Adds a TownFlag to the towns default flags
	 * 
	 * @param flag
	 */
	public void addFlag(ITownFlag flag) {
		flags.add(flag);
	}
	
	/**
	 * Gets a TownFlag from the towns default flags
	 * 
	 * @param name
	 * @return
	 */
	public ITownFlag getFlag(String name) {
		for(ITownFlag flag : flags) {
			if(flag.getName().equals(name))
				return flag;
		}
		return null;
	}
	
	/**
	 * Returns a list of all the default flags
	 * 
	 * @return
	 */
	public List<ITownFlag> getFlags() {
		return this.flags;
	}
	
	// //////////////////////////////////////
	// Helper?
	// //////////////////////////////////////
	@Override
	public String toString() {
		return getName() + "[# of residents: " + getResidents().size() + ", # of extra blocks: " + extraBlocks + "]";
	}

	@Override
	public int compareTo(Town t) { // TODO Flesh this out more for ranking
									// towns?
		int thisNumberOfResidents = residents.size(), thatNumberOfResidents = t.getResidents().size();
		if (thisNumberOfResidents > thatNumberOfResidents) {
			return -1;
		} else if (thisNumberOfResidents == thatNumberOfResidents) {
			return 0;
		} else if (thisNumberOfResidents < thatNumberOfResidents) {
			return 1;
		}

		return -1;
	}

	
	/**
	 * Adds a TownPlot to this block
	 * 
	 * @param plot
	 */
	public void addTownPlot(ITownPlot plot) {
		this.townPlots.add(plot);
	}
	
	/**
	 * Removes a TownPlot from this block
	 * 
	 * @param plot
	 * @return
	 */
	public boolean removeTownPlot(ITownPlot plot) {
		return this.townPlots.remove(plot);
	}
	
	public List<ITownPlot> getTownPlots() {
		return this.townPlots;
	}
	/**
	 * Gets all the flags for the specified block
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public List<ITownFlag> getFlagsForBlockCoords(int x, int y, int z) {
		ITownPlot plot = getPlotAtCoords(x, y, z);
		if(plot == null) return null;
		return plot.getFlags();
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
		for(ITownPlot p : townPlots) {
			if(p.isBlockInsidePlot(x, y, z)) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * Gets the value of a flag on the specified coordinates. Returns null if no plot or no flag is found.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param flagName
	 * @return
	 */
	public EnumFlagValue getFlagValueAtCoords(int x, int y, int z, String flagName) {
		ITownPlot plot = getPlotAtCoords(x, y, z);
		if(plot == null) return null;
		ITownFlag flag = plot.getFlag(flagName);
		if(flag == null) return null;
		return flag.getValue();
	}
}