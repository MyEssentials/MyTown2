package mytown.entities.town;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.entities.Nation;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
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
	
	protected String name;
	protected int extraBlocks = 0;
	protected List<Rank> ranks;
	protected List<ITownFlag> flags;
	protected List<ITownPlot> townPlots;
	protected List<Nation> nations = new ArrayList<Nation>();
	protected List<TownBlock> townBlocks = new ArrayList<TownBlock>();
	protected Map<Resident, Rank> residents = new Hashtable<Resident, Rank>();
	
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

	protected void initTownFlags() {
		flags = new ArrayList<ITownFlag>();
		
		flags.add(new TownFlag("mobs", "Controls mobs spawning", true));
		flags.add(new TownFlag("breakBlocks", "Controls whether or not non-residents can break blocks", false));
		flags.add(new TownFlag("explosions", "Controls if explosions can occur", true));
		flags.add(new TownFlag("accessBlocks", "Controls whether or not non-residents can access(right click) blocks", false));
		flags.add(new TownFlag("enter", "Controls whether or not a non-resident can enter the town", true));
		flags.add(new TownFlag("pickup", "Controls whether or not a non-resident can pick up items", true));
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
	

	public boolean addNations(List<Nation> nations) {
		return this.nations.addAll(nations);
	}

	public boolean addNation(Nation nation) {
		return nations.add(nation);
	}

	public boolean removeNation(Nation nation) {
		return nations.remove(nation);
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
	

	/**
	 * Adds the given TownBlocks to this Town
	 * 
	 * @param townBlocks
	 */
	public void addTownBlocks(List<TownBlock> townBlocks) {
		// Just to make the singular version easily overridable 
		for(TownBlock block : townBlocks) {
			addTownBlock(block);
		}
	}

	/**
	 * Add a TownBlocks to this Town
	 * 
	 * @param block
	 */
	public boolean addTownBlock(TownBlock block) {
		return townBlocks.add(block);
	}

	/**
	 * Remove a TownBlocks from this Town
	 * 
	 * @param block
	 */
	public boolean removeTownBlock(TownBlock block) {
		return townBlocks.remove(block);
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

	/**
	 * Gets the rank of the specified resident
	 * 
	 * @param resident
	 * @return
	 */
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

	/**
	 * Removes a rank
	 * 
	 * @param rank
	 */
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

	/**
	 * Checks if a rank has the name specified
	 * 
	 * @param name
	 * @return
	 */
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
	
	/**
	 * Gets all the flags for the specified block. Returns town's flags if no plot is found.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public List<ITownFlag> getFlagsForBlockCoords(int x, int y, int z) {
		ITownPlot plot = getPlotAtCoords(x, y, z);
		if(plot == null) return this.getFlags();
		return plot.getFlags();
	}
	
	/**
	 * Gets the flag on the specified coordinates. Returns town's flag if no plot is found.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param flagName
	 * @return
	 */
	public ITownFlag getFlagAtCoords(int x, int y, int z, String flagName) {
		ITownPlot plot = getPlotAtCoords(x, y, z);
		if(plot == null) return this.getFlag(flagName);
		return plot.getFlag(flagName);
	}
	
	// //////////////////////////////////////
	// Plots
	// //////////////////////////////////////
	/**
	 * Adds an ITownPlot to this block
	 * 
	 * @param plot
	 */
	public boolean addTownPlot(ITownPlot plot) {
		return this.townPlots.add(plot);
	}
	
	/**
	 * Removes an ITownPlot from this block
	 * 
	 * @param plot
	 * @return
	 */
	public boolean removeTownPlot(ITownPlot plot) {
		return this.townPlots.remove(plot);
	}
	
	/**
	 * Gets a list of all plots in the town
	 * 
	 * @return
	 */
	public List<ITownPlot> getTownPlots() {
		return this.townPlots;
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
	 * Gets the character that represents the type of this town. Only used in datasource
	 * 
	 * @return
	 */
	public String getType() {
		if(this instanceof AdminTown)
			return "A";
		return "T";
	}

	

}