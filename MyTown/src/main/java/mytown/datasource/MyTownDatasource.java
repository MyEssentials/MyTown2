package mytown.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.Constants;
import mytown.core.Log;
import mytown.entities.Nation;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.TownPlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.Configuration;

// TODO Datasource pool?
// TODO Add logging
// TODO More error reporting
// TODO Link Residents to their Towns
// TODO Link Towns to their Nations
// TODO Load Plots
// TODO Link Residents to their Plots
// TODO Map of TownIds to their Towns?

/**
 * Abstract Datasource class. Extend to add more support
 * 
 * @author Joe Goett
 */
public abstract class MyTownDatasource {
	protected String configCat = "datasource";
	
	protected Log log;
	protected Map<String, Town> towns;
	protected Map<String, Resident> residents;
	protected Map<String, Nation> nations;
	protected Map<String, TownBlock> blocks;
	protected List<TownPlot> plots;

	/**
	 * Used for connecting to Databases. Returns if connection was successful
	 * 
	 * @return
	 */
	public abstract boolean connect() throws Exception;

	public void configure(Configuration config, Log log) {
		this.log = log;
		doConfig(config);
		towns = new Hashtable<String, Town>();
		residents = new Hashtable<String, Resident>();
		nations = new Hashtable<String, Nation>();
		blocks = new Hashtable<String, TownBlock>();
		plots = new ArrayList<TownPlot>(); // TODO: Use a List implementation that doesn't allow nulls, maybe?
	}

	/**
	 * Does implementation specific configuration
	 * 
	 * @param config
	 */
	protected abstract void doConfig(Configuration config);

	/**
	 * Saves everything to the Datasource
	 * 
	 * @throws Exception
	 */
	public abstract void save() throws Exception;

	/**
	 * Disconnects from the Datasource
	 * 
	 * @throws Exception
	 */
	public abstract void disconnect() throws Exception;

	// /////////////////////////////////////////////////////////////
	// Map Getters
	// /////////////////////////////////////////////////////////////

	/**
	 * Returns a Map of towns
	 * 
	 * @return
	 */
	public Map<String, Town> getTownsMap() {
		return towns;
	}

	/**
	 * Returns a Map of residents
	 * 
	 * @return
	 */
	public Map<String, Resident> getResidentsMap() {
		return residents;
	}

	/**
	 * Returns a Map of nations
	 * 
	 * @return
	 */
	public Map<String, Nation> getNationsMap() {
		return nations;
	}

	/**
	 * Returns a Map of the town blocks
	 * 
	 * @return
	 */
	public Map<String, TownBlock> getTownBlocksMap() {
		return blocks;
	}

	// /////////////////////////////////////////////////////////////
	// Collection Getters
	// /////////////////////////////////////////////////////////////

	/**
	 * Returns a Collection of the Towns
	 * 
	 * @return
	 */
	public Collection<Town> getTowns() {
		return towns.values();
	}

	/**
	 * Returns a Collection of the Residents
	 * 
	 * @return
	 */
	public Collection<Resident> getResidents() {
		return residents.values();
	}

	/**
	 * Returns a Collection of the Nations
	 * 
	 * @return
	 */
	public Collection<Nation> getNations() {
		return nations.values();
	}

	/**
	 * Returns a Collection of the TownBlocks
	 * 
	 * @return
	 */
	public Collection<TownBlock> getTownBlocks() {
		return blocks.values();
	}

	/**
	 * Returns a Collection of the TownPlots
	 * 
	 * @return
	 */
	public Collection<TownPlot> getTownPlots() {
		return plots;
	}

	// /////////////////////////////////////////////////////////////
	// Single Instance Getters
	// /////////////////////////////////////////////////////////////

	/**
	 * Gets a Town with the given name
	 * 
	 * @param name
	 * @return
	 */
	public Town getTown(String name) {
		return towns.get(name);
	}

	public Rank getRank(String rank, Town town)
	{
		for(Rank r : Constants.DEFAULT_RANKS) {
			if(r.parse(rank))
				return r;
		}
		for(Rank r : town.getAdditionalRanks()) {
			if(r.parse(rank))
				return r;
		}
		return null;
	}
	
	/**
	 * Gets a Resident with the given name
	 * 
	 * @param name
	 * @return
	 */
	public Resident getResident(String name) {
		return residents.get(name);
	}

	/**
	 * Gets a Nation with the given name
	 * 
	 * @param name
	 * @return
	 */
	public Nation getNation(String name) {
		return nations.get(name);
	}

	/**
	 * Gets a TownBlock with the given key, or null if it doesn't exist. Key Format: dimID;x;z
	 * 
	 * @param key
	 * @return
	 */
	public TownBlock getTownBlock(String key) {
		return blocks.get(key);
	}

	/**
	 * Gets a TownBlock with the given dim, x, and z
	 * 
	 * @param dim
	 * @param x
	 * @param z
	 * @return
	 */
	public TownBlock getTownBlock(int dim, int x, int z) {
		return getTownBlock(dim + ";" + x + ";" + z);
	}

	// /////////////////////////////////////////////////////////////
	// Checkers?
	// /////////////////////////////////////////////////////////////
	
	/**
	 * Checks if a Town with the given name exists
	 * @param townName
	 * @return
	 */
	public boolean hasTown(String townName) {
		return towns.containsKey(townName);
	}
	
	/**
	 * Checks if the Resident with the given UUID exists
	 * @param residentUUID 1.6 is player name, 1.7> is player UUID
	 * @return
	 */
	public boolean hasResident(String residentUUID) {
		return residents.containsKey(residentUUID);
	}
	
	/**
	 * Checks if the Nation with the given name exists
	 * @param nationName
	 * @return
	 */
	public boolean hasNation(String nationName) {
		return nations.containsKey(nationName);
	}

	/**
	 * Checks if the TownBlock with the given key exists
	 * @param key
	 * @return
	 */
	public boolean hasTownBlock(String key) {
		return blocks.containsKey(key);
	}
	
	// /////////////////////////////////////////////////////////////
	// Loaders
	// /////////////////////////////////////////////////////////////

	/**
	 * Loads all the Residents into the Datasource
	 */
	public abstract void loadResidents() throws Exception;

	/**
	 * Loads all the Towns into the Datasource
	 */
	public abstract void loadTowns() throws Exception;

	/**
	 * Loads all the Nations into the Datasource
	 */
	public abstract void loadNations() throws Exception;

	/**
	 * Loads all TownBlocks for the given town into the Datasource
	 */
	public abstract void loadTownBlocks(Town town) throws Exception;

	/**
	 * Loads all TownPlots for the given town
	 * 
	 * @param town
	 * @throws Exception
	 */
	public abstract void loadTownPlots(Town town) throws Exception;

	// /////////////////////////////////////////////////////////////
	// Add Single Entity
	// /////////////////////////////////////////////////////////////

	/**
	 * Adds a Town to the Datasource
	 * 
	 * @param town
	 */
	public void addTown(Town town) throws Exception {
		log.info("Adding town %s", town.getName()); // TODO Remove later
		towns.put(town.getName(), town);
	}

	/**
	 * Adds a Resident to the Datasource
	 * 
	 * @param resident
	 */
	public void addResident(Resident resident) throws Exception {
		residents.put(resident.getUUID(), resident);
	}

	/**
	 * Adds a Nation to the Datasource
	 * 
	 * @param nation
	 */
	public void addNation(Nation nation) throws Exception {
		nations.put(nation.getName(), nation);
	}

	/**
	 * Adds a TownBlock to the Datasource
	 * 
	 * @param townBlock
	 */
	public void addTownBlock(TownBlock townBlock) throws Exception {
		blocks.put(townBlock.getKey(), townBlock);
	}

	/**
	 * Adds the TownPlot to the Datasource
	 * 
	 * @param plot
	 * @throws Exception
	 */
	public void addTownPlot(TownPlot plot) throws Exception {
		plots.add(plot);
	}

	// /////////////////////////////////////////////////////////////
	// Add Multiple Entities
	// /////////////////////////////////////////////////////////////

	/**
	 * Adds multiple Towns to the Datasource
	 * 
	 * @param towns
	 * @throws Exception
	 */
	public void addTowns(Town... towns) throws Exception {
		for (Town town : towns) {
			addTown(town);
		}
	}

	/**
	 * Adds multiple Residents to the Datasource
	 * 
	 * @param residents
	 * @throws Exception
	 */
	public void addResidents(Resident... residents) throws Exception {
		for (Resident res : residents) {
			addResident(res);
		}
	}

	/**
	 * Adds multiple Nations to the Datasource
	 * 
	 * @param nations
	 * @throws Exception
	 */
	public void addNations(Nation... nations) throws Exception {
		for (Nation nation : nations) {
			addNation(nation);
		}
	}

	/**
	 * Adds multiple TownBlocks to the Datasource
	 * 
	 * @param townBlocks
	 * @throws Exception
	 */
	public void addTownBlocks(TownBlock... townBlocks) throws Exception {
		for (TownBlock block : townBlocks) {
			addTownBlocks(block);
		}
	}

	/**
	 * Adds all the TownPlots
	 * 
	 * @param townPlots
	 * @throws Exception
	 */
	public void addTownPlots(TownPlot... townPlots) throws Exception {
		plots.addAll(plots);
	}

	// /////////////////////////////////////////////////////////////
	// Insert Single Entity
	// /////////////////////////////////////////////////////////////

	/**
	 * Adds a Town to the Datasource and executes a query
	 * 
	 * @param town
	 * @throws Exception
	 */
	public abstract void insertTown(Town town) throws Exception;

	/**
	 * Adds a Resident to the Datasource and executes a query
	 * 
	 * @param resident
	 * @throws Exception
	 */
	public abstract void insertResident(Resident resident) throws Exception;

	/**
	 * Adds a Nation to the Datasource and executes a query
	 * 
	 * @param nation
	 * @throws Exception
	 */
	public abstract void insertNation(Nation nation) throws Exception;

	/**
	 * Adds a TownBlock to the Datasource and executes a query
	 * 
	 * @param townBlock
	 * @throws Exception
	 */
	public abstract void insertTownBlock(TownBlock townBlock) throws Exception;

	/**
	 * Adds a TownPlot to the Datasource and executes a query
	 * 
	 * @param townPlot
	 * @throws Exception
	 */
	public abstract void insertTownPlot(TownPlot townPlot) throws Exception;

	// /////////////////////////////////////////////////////////////
	// Insert Multiple Entities
	// /////////////////////////////////////////////////////////////

	/**
	 * Adds multiple Towns to the Datasource and executes a query
	 * 
	 * @param towns
	 * @throws Exception
	 */
	public void insertTowns(Town... towns) throws Exception {
		for (Town town : towns) {
			insertTown(town);
		}
	}

	/**
	 * Adds multiple Residents to the Datasource and executes a query
	 * 
	 * @param residents
	 * @throws Exception
	 */
	public void insertResidents(Resident... residents) throws Exception {
		for (Resident res : residents) {
			insertResident(res);
		}
	}

	/**
	 * Adds multiple Nations to the Datasource and executes a query
	 * 
	 * @param nations
	 * @throws Exception
	 */
	public void insertNations(Nation... nations) throws Exception {
		for (Nation nation : nations) {
			insertNation(nation);
		}
	}

	/**
	 * Adds multiple TownBlocks to the Datasource and executes a query
	 * 
	 * @param townBlocks
	 * @throws Exception
	 */
	public void insertTownBlocks(TownBlock... townBlocks) throws Exception {
		for (TownBlock block : townBlocks) {
			insertTownBlock(block);
		}
	}

	/**
	 * Adds multiple TownPlots to the Datasource and executes a query
	 * 
	 * @param plots
	 * @throws Exception
	 */
	public void insertTownPlots(TownPlot... plots) throws Exception {
		for (TownPlot plot : plots) {
			insertTownPlot(plot);
		}
	}

	// /////////////////////////////////////////////////////////////
	// Remove Single Entity
	// /////////////////////////////////////////////////////////////

	/**
	 * Removes a Town from the Datasource
	 * 
	 * @param town
	 */
	public boolean removeTown(Town town) {
		return towns.remove(town.getName()) != null;
	}

	/**
	 * Removes a Resident from the Datasource
	 * 
	 * @param resident
	 */
	public boolean removeResident(Resident resident) {
		return residents.remove(resident.getUUID()) != null;
	}

	/**
	 * Removes a Nation from the Datasource
	 * 
	 * @param nation
	 */
	public boolean removeNation(Nation nation) {
		return nations.remove(nation.getName()) != null;
	}

	/**
	 * Removes a TownBlock from the Datasource
	 * 
	 * @param townBlock
	 */
	public boolean removeTownBlock(TownBlock townBlock) {
		return blocks.remove(townBlock.getKey()) != null;
	}

	/**
	 * Removes the TownPlot from the Datasource
	 * 
	 * @param plot
	 * @return
	 */
	public boolean removeTownPlot(TownPlot plot) {
		return plots.remove(plot);
	}

	// /////////////////////////////////////////////////////////////
	// Remove Multiple Entities
	// /////////////////////////////////////////////////////////////

	/**
	 * Removes the Towns from the Datasource
	 * 
	 * @param towns
	 */
	public void removeTowns(Town... towns) {
		for (Town town : towns) {
			removeTown(town);
		}
	}

	/**
	 * Removes the Residents from the Datasource
	 * 
	 * @param residents
	 */
	public void removeResidents(Resident... residents) {
		for (Resident res : residents) {
			removeResident(res);
		}
	}

	/**
	 * Removes the Nations from the Datasource
	 * 
	 * @param nations
	 */
	public void removeNations(Nation... nations) {
		for (Nation nation : nations) {
			removeNation(nation);
		}
	}

	/**
	 * Removes the TownBlocks from the Datasource
	 * 
	 * @param blocks
	 */
	public void removeTownBlocks(TownBlock... blocks) {
		for (TownBlock block : blocks) {
			removeTownBlock(block);
		}
	}

	/**
	 * Removes the TownPlots from the Datasource
	 * 
	 * @param plots
	 */
	public void removeTownPlots(TownPlot... plots) {
		for (TownPlot plot : plots) {
			removeTownPlot(plot);
		}
	}

	// /////////////////////////////////////////////////////////////
	// Update Single Entity
	// /////////////////////////////////////////////////////////////

	/**
	 * Updates the Town
	 * 
	 * @param town
	 * @throws Exception
	 */
	public abstract void updateTown(Town town) throws Exception;

	/**
	 * Updates the Resident
	 * 
	 * @param resident
	 * @throws Exception
	 */
	public abstract void updateResident(Resident resident) throws Exception;

	/**
	 * Updates the Nation
	 * 
	 * @param nation
	 * @throws Exception
	 */
	public abstract void updateNation(Nation nation) throws Exception;

	/**
	 * Updates the TownBlock
	 * 
	 * @param block
	 * @throws Exception
	 */
	public abstract void updateTownBlock(TownBlock block) throws Exception;

	/**
	 * Updates the TownPlot
	 * 
	 * @param plot
	 * @throws Exception
	 */
	public abstract void updateTownPlot(TownPlot plot) throws Exception;

	// /////////////////////////////////////////////////////////////
	// Update Multiple Entities
	// /////////////////////////////////////////////////////////////

	/**
	 * Updates all the given Towns
	 * 
	 * @param towns
	 * @throws Exception
	 */
	public void updateTowns(Town... towns) throws Exception {
		for (Town town : towns) {
			updateTown(town);
		}
	}

	/**
	 * Updates all the given Residents
	 * 
	 * @param residents
	 * @throws Exception
	 */
	public void updateResidents(Resident... residents) throws Exception {
		for (Resident res : residents) {
			updateResident(res);
		}
	}

	/**
	 * Updates all the given Nations
	 * 
	 * @param nations
	 * @throws Exception
	 */
	public void updateNations(Nation... nations) throws Exception {
		for (Nation nation : nations) {
			updateNation(nation);
		}
	}

	/**
	 * Updates all the given TownBlocks
	 * 
	 * @param blocks
	 * @throws Exception
	 */
	public void updateTownBlocks(TownBlock... blocks) throws Exception {
		for (TownBlock block : blocks) {
			updateTownBlock(block);
		}
	}

	/**
	 * Updates all the given TownPlots
	 * 
	 * @param plots
	 * @throws Exception
	 */
	public void updateTownPlots(TownPlot... plots) throws Exception {
		for (TownPlot plot : plots) {
			updateTownPlot(plot);
		}
	}

	// /////////////////////////////////////////////////////////////
	// Linkages
	// /////////////////////////////////////////////////////////////
	
	/**
	 * Loads all stored links between Residents and Towns
	 * @throws Exception
	 */
	public abstract void loadResidentToTownLinks() throws Exception;

	/**
	 * Loads all stored links between Towns and Nations
	 * @throws Exception
	 */
	public abstract void loadTownToNationLinks() throws Exception;
	
	/**
	 * Links the Resident with the given Rank to the Town
	 * 
	 * @param resident
	 * @param town
	 * @param rank
	 */
	public abstract void linkResidentToTown(Resident resident, Town town, Rank rank) throws Exception;

	/**
	 * Links the Resident to the Town with a Rank of Resident
	 * 
	 * @param resident
	 * @param town
	 */
	public void linkResidentToTown(Resident resident, Town town) throws Exception {
		linkResidentToTown(resident, town, Constants.DEFAULT_RANKS[1]);
	}

	/**
	 * Links the Town to the Nation
	 * 
	 * @param town
	 * @param nation
	 */
	public abstract void linkTownToNation(Town town, Nation nation, Nation.Rank rank) throws Exception;

	/**
	 * Links the Town to the Nation with the Rank of Town
	 * 
	 * @param town
	 * @param nation
	 * @throws Exception
	 */
	public void linkTownToNation(Town town, Nation nation) throws Exception {
		linkTownToNation(town, nation, Nation.Rank.Town);
	}

	// /////////////////////////////////////////////////////////////
	// Extras
	// /////////////////////////////////////////////////////////////

	public abstract void dump() throws Exception;

	// /////////////////////////////////////////////////////////////
	// Unknown Group/Helpers?			  TODO Change/Move Later? //
	// /////////////////////////////////////////////////////////////
	
	/**
	 * Gets or makes a new Resident from the playerName
	 * @param playerName
	 * @return
	 * @throws Exception
	 */
	public Resident getOrMakeResident(String playerName) throws Exception {
		Resident res = residents.get(playerName);
		if (res == null) {
			res = new Resident(playerName);
			insertResident(res);
		}
		return res;
	}
	
	/**
	 * Gets or makes a new Resident from the EntityPlayer
	 * @param player
	 * @return
	 * @throws Exception
	 */
	public Resident getOrMakeResident(EntityPlayer player) throws Exception {
		return getOrMakeResident(player.getCommandSenderName());
	}
	
}