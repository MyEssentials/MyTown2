package mytown.db;

import java.util.Collection;
import java.util.Map;

import mytown.entities.Nation;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;

/**
 * Abstract Datasource class. Extend to add more database support
 * @author Joe Goett
 */
public abstract class MyTownDatasource {
	protected Map<String, Town> towns;
	protected Map<String, Resident> residents;
	protected Map<String, Nation> nations;
	protected Map<String, TownBlock> blocks;
	
	///////////////////////////////////////////////////////////////
	// Collection Getters
	///////////////////////////////////////////////////////////////
	
	/**
	 * Returns a Collection of the towns
	 * @return
	 */
	public Collection<Town> getTowns(){
		return towns.values();
	}
	
	/**
	 * Returns a Collection of the residents
	 * @return
	 */
	public Collection<Resident> getResidents(){
		return residents.values();
	}
	
	/**
	 * Returns a Collection of the nations
	 * @return
	 */
	public Collection<Nation> getNations(){
		return nations.values();
	}
	
	/**
	 * Returns a Collection of the town blocks
	 * @return
	 */
	public Collection<TownBlock> getTownBlocks(){
		return blocks.values();
	}
	
	///////////////////////////////////////////////////////////////
	// Map Getters
	///////////////////////////////////////////////////////////////
	
	/**
	 * Returns a Map of towns
	 * @return
	 */
	public Map<String, Town> getTownsMap(){
		return towns;
	}
	
	/**
	 * Returns a Map of residents
	 * @return
	 */
	public Map<String, Resident> getResidentsMap(){
		return residents;
	}
	
	/**
	 * Returns a Map of nations
	 * @return
	 */
	public Map<String, Nation> getNationsMap(){
		return nations;
	}

	/**
	 * Returns a Map of the town blocks
	 * @return
	 */
	public Map<String, TownBlock> getTownBlocksMap(){
		return blocks;
	}

	///////////////////////////////////////////////////////////////
	// Single Instance Getters
	///////////////////////////////////////////////////////////////
	
	/**
	 * Gets a Town with the given name
	 * @param name
	 * @return
	 */
	public Town getTown(String name){
		return towns.get(name);
	}
	
	/**
	 * Gets a Resident with the given name
	 * @param name
	 * @return
	 */
	public Resident getResident(String name){
		return residents.get(name);
	}
	
	/**
	 * Gets a Nation with the given name
	 * @param name
	 * @return
	 */
	public Nation getNation(String name){
		return nations.get(name);
	}
	
	/**
	 * Gets a TownBlock with the given key
	 * Key Format: dimID;x;z
	 * @param key
	 * @return
	 */
	public TownBlock getTownBlock(String key){
		return blocks.get(key);
	}
	
	/**
	 * Gets a TownBlock with the given dim, x, and z
	 * @param dim
	 * @param x
	 * @param z
	 * @return
	 */
	public TownBlock getTownBlock(int dim, int x, int z){
		return getTownBlock(dim + ";" + x + ";" + z);
	}
	
	///////////////////////////////////////////////////////////////
	// Loaders
	///////////////////////////////////////////////////////////////
	
	/**
	 * Loads all the Residents into the Datasource
	 */
	public abstract void loadResidents();

	/**
	 * Loads all the Towns into the Datasource
	 */
	public abstract void loadTowns();
	
	/**
	 * Loads all the Nations into the Datasource
	 */
	public abstract void loadNations();
	
	/**
	 * Loads all TownBlocks into the Datasource
	 */
	public abstract void loadAllTownBlocks();
	
	///////////////////////////////////////////////////////////////
	// Savers?
	///////////////////////////////////////////////////////////////
	// TODO: Add methods to save stuff?
	
	///////////////////////////////////////////////////////////////
	// Add Single Entity
	///////////////////////////////////////////////////////////////
	
	/**
	 * Adds a Town to the Datasource
	 * @param town
	 */
	public abstract void addTown(Town town);

	/**
	 * Adds a Resident to the Datasource
	 * @param resident
	 */
	public abstract void addResident(Resident resident);

	/**
	 * Adds a Nation to the Datasource
	 * @param nation
	 */
	public abstract void addNation(Nation nation);
	
	/**
	 * Adds a TownBlock to the Datasource
	 * @param townBlock
	 */
	public abstract void addTownBlock(TownBlock townBlock);
	
	///////////////////////////////////////////////////////////////
	// Remove Single Entity
	///////////////////////////////////////////////////////////////
	
	/**
	 * Removes a Town from the Datasource
	 * @param town
	 */
	public abstract void removeTown(Town town);

	/**
	 * Removes a Resident from the Datasource
	 * @param resident
	 */
	public abstract void removeResident(Resident resident);

	/**
	 * Removes a Nation from the Datasource
	 * @param nation
	 */
	public abstract void removeNation(Nation nation);
	
	/**
	 * Removes a TownBlock from the Datasource
	 * @param townBlock
	 */
	public abstract void removeTownBlock(TownBlock townBlock);
}