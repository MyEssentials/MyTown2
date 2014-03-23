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
public class Town {
	private int id;
	private String name;
	private int extraBlocks = 0;

	// TODO Add flags/permissions

	/**
	 * Creates a town with the given name
	 * 
	 * @param name
	 */
	public Town(String name) {
		this.name = name;
	}

	/**
	 * Used internally only!
	 * 
	 * @param id
	 * @param name
	 */
	public Town(int id, String name, int extraBlocks) {
		this(name);
		this.id = id;
		this.extraBlocks = extraBlocks;
	}

	/**
	 * Returns the ID of the town
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id. Used internally only!
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
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
		if (!hasNation(nation))
			return; // TODO Log/Throw Exception
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
	private Map<Resident, Resident.Rank> residents = new Hashtable<Resident, Resident.Rank>();

	/**
	 * Returns the Residents
	 * 
	 * @return
	 */
	public Set<Resident> getResidents() {
		return residents.keySet();
	}

	/**
	 * Adds the Resident with Rank
	 * 
	 * @param resident
	 * @param rank
	 */
	public void addResident(Resident resident, Resident.Rank rank) {
		residents.put(resident, rank);
	}

	/**
	 * Removes the Resident
	 * 
	 * @param resident
	 */
	public void removeResident(Resident resident) {
		residents.remove(resident);
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
	 * Promotes the Resident to the Rank
	 * 
	 * @param resident
	 * @param rank
	 */
	public void promoteResident(Resident resident, Resident.Rank rank) {
		if (!hasResident(resident))
			return; // TODO Log/Throw Exception
		addResident(resident, rank);
	}

	public Resident.Rank getResidentRank(Resident resident) {
		if (hasResident(resident)) {
			return residents.get(resident);
		} else {
			return Resident.Rank.Outsider;
		}
	}
}