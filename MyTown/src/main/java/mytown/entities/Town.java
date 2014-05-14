package mytown.entities;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	// TODO Add flags/permissions

	/**
	 * Creates a town with the given name
	 * 
	 * @param name
	 */
	public Town(String name) {
		this.name = name;
		setInitialPermission();
	}

	public Town(String name, int extraBlocks) {
		this.name = name;
		this.extraBlocks = extraBlocks;
		setInitialPermission();
	}

	public void setInitialPermission() {
		ranks = new ArrayList<Rank>();
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
		for(Resident r : residents.keySet())
			if(r.getUUID().equals(UUID))
				return true;
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
	public Rank getRank(String name)
	{
		for(Rank r : ranks)
			if(r.getName().equals(name))
				return r;
		return null;
	}
	
	public void removeRank(Rank rank)
	{
		ranks.remove(rank);
	}
	
	/**
	 * Adds a rank
	 * @param rank
	 */
	public void addRank(Rank rank)
	{
		for(Rank r : ranks)
			if(r.getName() == rank.getName())
				return;
		ranks.add(rank);
	}
	
	/**
	 * Checks if the Rank exists in this town
	 * @param rank
	 * @return
	 */
	public boolean hasRank(Rank rank)
	{
		return this.ranks.contains(rank);
	}

	public boolean hasRankName(String name)
	{
		for(Rank r : ranks)
			if(r.getName().equals(name))
				return true;
		return false;
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
}