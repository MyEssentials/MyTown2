package mytown.api.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import mytown.core.utils.Log;
import mytown.x_entities.Nation;
import mytown.x_entities.Rank;
import mytown.x_entities.Resident;
import mytown.x_entities.TownBlock;
import mytown.x_entities.town.AdminTown;
import mytown.x_entities.town.Town;
import mytown.interfaces.ITownFlag;
import mytown.interfaces.ITownPlot;
import mytown.util.Constants;
import net.minecraft.entity.player.EntityPlayer;

// TODO Datasource pool?
// TODO More/better error reporting and logging
// TODO Events?

/**
 * Abstract Datasource class. Extend to add more support
 * 
 * @author Joe Goett
 */
public abstract class MyTownDatasource {
	protected Log log;
	protected ConcurrentMap<String, Town> towns;
	protected ConcurrentMap<String, Resident> residents;
	protected ConcurrentMap<String, Nation> nations;
	protected ConcurrentMap<String, TownBlock> blocks;
	protected ConcurrentMap<String, Rank> ranks;
	protected ConcurrentMap<String, ITownPlot> plots;

	/**
	 * Used for connecting to Databases. Returns if connection was successful
	 * 
	 * @return
	 */
	public abstract boolean connect() throws Exception;

	public void configure(Log log) {
		this.log = log;
		towns = new ConcurrentHashMap<String, Town>();
		residents = new ConcurrentHashMap<String, Resident>();
		nations = new ConcurrentHashMap<String, Nation>();
		blocks = new ConcurrentHashMap<String, TownBlock>();
		ranks = new ConcurrentHashMap<String, Rank>();
		plots = new ConcurrentHashMap<String, ITownPlot>();
	}

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

	/**
	 * Returns a Map of ranks
	 * 
	 * @return
	 */
	public Map<String, Rank> getRanksMap() {
		return ranks;
	}

	/**
	 * Returns a Map of plots
	 * 
	 * @return
	 */
	public Map<String, ITownPlot> getPlotsMap() {
		return plots;
	}

	// /////////////////////////////////////////////////////////////
	// Collection Getters
	// /////////////////////////////////////////////////////////////

	/**
	 * Returns a Collection of the Towns
	 * 
	 * @return
	 */
	public Collection<Town> getTowns(boolean adminTownsIncluded) {
		if (adminTownsIncluded)
			return towns.values();
		Collection<Town> temp = new ArrayList<Town>();
		for (Town t : towns.values()) {
			if (!(t instanceof AdminTown)) {
				temp.add(t);
			}
		}
		return temp;
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
	 * Returns a Collection of the Ranks
	 * 
	 * @return
	 */
	public Collection<Rank> getRanks() {
		return ranks.values();
	}

	/**
	 * Returns a Collection of the Plots
	 * 
	 * @return
	 */
	public Collection<ITownPlot> getPlots() {
		return plots.values();
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
	 * Gets a townblock at coordinates
	 * 
	 * @param x
	 * @param z
	 * @param inChunkCoords
	 *            true if x and z are in chunk coordinates, false otherwise
	 * @return
	 */
	public TownBlock getTownBlock(int dim, int x, int z, boolean inChunkCoords) {
		String key;
		if (inChunkCoords) {
			key = String.format(TownBlock.keyFormat, dim, x, z);
		} else {
			key = String.format(TownBlock.keyFormat, dim, x >> 4, z >> 4);
		}

		return getTownBlock(key);
	}

	/**
	 * Gets a Rank from the Town specified
	 * 
	 * @param rank
	 * @return
	 */
	public Rank getRank(String rank, Town town) {
		if (town == null)
			return null;
		if (ranks.get(town.getName() + ";" + rank) == null)
			return null;
		return ranks.get(town.getName() + ";" + rank);
	}

	/**
	 * 
	 * Gets a Rank
	 * 
	 * @param key
	 *            should look like this: TownName;Rank
	 * @return
	 */
	public Rank getRank(String key) {
		return ranks.get(key);
	}

	/**
	 * Gets a Plot
	 * 
	 * @param key
	 * 
	 * @return
	 */
	public ITownPlot getPlot(String key) {
		return plots.get(key);
	}

	// /////////////////////////////////////////////////////////////
	// Checkers?
	// /////////////////////////////////////////////////////////////

	/**
	 * Checks if a Town with the given name exists
	 * 
	 * @param townName
	 * @return
	 */
	public boolean hasTown(String townName) {
		return towns.containsKey(townName);
	}

	/**
	 * Checks if the Resident with the given UUID exists
	 * 
	 * @param residentUUID
	 *            1.6 is player name, 1.7> is player UUID
	 * @return
	 */
	public boolean hasResident(String residentUUID) {
		return residents.containsKey(residentUUID);
	}

	/**
	 * Checks if the Nation with the given name exists
	 * 
	 * @param nationName
	 * @return
	 */
	public boolean hasNation(String nationName) {
		return nations.containsKey(nationName);
	}

	/**
	 * Checks if the TownBlock with the given key exists
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasTownBlock(String key) {
		return blocks.containsKey(key);
	}

	/**
	 * Checks if the TownBlock with the given coords and dim exists
	 * 
	 * @param dim
	 * @param x
	 * @param z
	 * @param inChunkCoords
	 *            true if x and z are in chunk coordinates, false otherwise
	 * @return
	 */
	public boolean hasTownBlock(int dim, int x, int z, boolean inChunkCoords) {
		return hasTownBlock(dim, x, z, inChunkCoords, null);
	}

	/**
	 * Checks if the TownBlock with the given coords and dim at the town specified exists
	 * 
	 * @param dim
	 * @param x
	 * @param z
	 * @param inChunkCoords
	 *            true if x and z are in chunk coordinates, false otherwise
	 * @return
	 */
	public boolean hasTownBlock(int dim, int x, int z, boolean inChunkCoords, Town town) {
		String key;
		if (inChunkCoords) {
			key = String.format(TownBlock.keyFormat, dim, x, z);
		} else {
			key = String.format(TownBlock.keyFormat, dim, x >> 4, z >> 4);
		}

		TownBlock tb = getTownBlock(key);
		if (town != null && tb != null && tb.getTown() == town)
			return true;

		return hasTownBlock(key);
	}

	/**
	 * Checks if the Rank with the given key exists in the Datasource
	 * 
	 * @param rankName
	 * @return
	 */
	public boolean hasRank(String key) {
		return ranks.containsKey(key);
	}

	/**
	 * Checks if the Plot with the given key exists in the Datasource
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasPlot(String key) {
		return plots.containsKey(key);
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
	 * Loads all Ranks into the Datasource
	 */
	public abstract void loadRanks() throws Exception;

	/**
	 * Loads all Plots into the Datasource
	 */
	public abstract void loadPlots() throws Exception;

	/**
	 * Loads all flags for plots
	 */
	public abstract void loadPlotFlags() throws Exception;

	/**
	 * Loads all flags for towns
	 */
	public abstract void loadTownFlags() throws Exception;

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
	 * Adds a Rank to the Datasource and executes a query
	 * 
	 * @param town
	 * @param rank
	 * @throws Exception
	 */
	public abstract void insertRank(Rank rank) throws Exception;

	/**
	 * Adds a TownPlot to the Datasource and executes a query
	 * 
	 * @param plot
	 * @throws Exception
	 */
	public abstract void insertPlot(ITownPlot plot) throws Exception;

	/**
	 * Adds a flag to the plot and executes a query
	 * 
	 * @param plot
	 * @param flag
	 * @throws Exception
	 */
	public abstract void insertPlotFlag(ITownPlot plot, ITownFlag flag) throws Exception;

	/**
	 * Adds a flag to the town and executes a query
	 * 
	 * @param town
	 * @param flag
	 * @throws Exception
	 */
	public abstract void insertTownFlag(Town town, ITownFlag flag) throws Exception;

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
	 * Adds multiple Ranks to the Datasource and executes a query
	 * 
	 * @param town
	 * @param ranks
	 * @throws Exception
	 */
	public void insertRanks(Rank... ranks) throws Exception {
		for (Rank r : ranks) {
			insertRank(r);
		}
	}

	/**
	 * Adds multiple TownPlots to the Datasource and executes a query
	 * 
	 * @param plots
	 * @throws Exception
	 */
	public void insertPlots(ITownPlot... plots) throws Exception {
		for (ITownPlot plot : plots) {
			insertPlot(plot);
		}
	}

	/**
	 * Adds multiple flags to the plot and executes a query
	 * 
	 * @param plot
	 * @param flags
	 * @throws Exception
	 */
	public void insertPlotFlags(ITownPlot plot, ITownFlag... flags) throws Exception {
		for (ITownFlag flag : flags) {
			insertPlotFlag(plot, flag);
		}
	}

	/**
	 * Adds multiple flags to the town and executes a query
	 * 
	 * @param town
	 * @param flags
	 * @throws Exception
	 */
	public void insertTownFlags(Town town, ITownFlag... flags) throws Exception {
		for (ITownFlag flag : flags) {
			insertTownFlag(town, flag);
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
	 * Updates the Rank
	 * 
	 * @param town
	 * @param rank
	 * @throws Exception
	 */
	public abstract void updateRank(Rank rank) throws Exception;

	/**
	 * Updates the plot
	 * 
	 * @param plot
	 * @throws Exception
	 */
	public abstract void updatePlot(ITownPlot plot) throws Exception;

	/**
	 * Updates a flag
	 * 
	 * @param plot
	 * @param flag
	 * @throws Exception
	 */
	public abstract void updatePlotFlag(ITownFlag flag) throws Exception;

	/**
	 * Updates a flag
	 * 
	 * @param town
	 * @param flag
	 * @throws Exception
	 */
	public abstract void updateTownFlag(ITownFlag flag) throws Exception;

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
	 * Updates all the given Ranks
	 * 
	 * @param town
	 * @param ranks
	 * @throws Exception
	 */
	public void updateRanks(Rank... ranks) throws Exception {
		for (Rank r : ranks) {
			updateRank(r);
		}
	}

	/**
	 * Updates all the given Plots
	 * 
	 * @param plots
	 * @throws Exception
	 */
	public void updatePlots(ITownPlot... plots) throws Exception {
		for (ITownPlot plot : plots) {
			updatePlot(plot);
		}
	}

	/**
	 * Updates all the given flags
	 * 
	 * @param plot
	 * @param flags
	 * @throws Exception
	 */
	public void updatePlotFlags(ITownFlag... flags) throws Exception {
		for (ITownFlag flag : flags) {
			updatePlotFlag(flag);
		}
	}

	/**
	 * Updates all the given flags
	 * 
	 * @param town
	 * @param flags
	 * @throws Exception
	 */
	public void updateTownFlags(ITownFlag... flags) throws Exception {
		for (ITownFlag flag : flags) {
			updateTownFlag(flag);
		}
	}

	// /////////////////////////////////////////////////////////////
	// Delete Single Entities
	// /////////////////////////////////////////////////////////////

	/**
	 * Deletes the town from Datasource and executes a query
	 * 
	 * @param town
	 * @return
	 */
	public abstract boolean deleteTown(Town town) throws Exception;

	/**
	 * Deletes the nation from Datasource and executes a query
	 * 
	 * @param town
	 * @return
	 */
	public abstract boolean deleteNation(Nation nation) throws Exception;

	/**
	 * Deletes the townblock from Datasource and executes a query
	 * 
	 * @param town
	 * @return
	 */
	public abstract boolean deleteTownBlock(TownBlock townBlock) throws Exception;

	/**
	 * Deletes the resident from Datasource and executes a query
	 * 
	 * @param town
	 * @return
	 */
	public abstract boolean deleteResident(Resident resident) throws Exception;

	/**
	 * Deletes a rank from Datasource and executes a query
	 * 
	 * @param rank
	 * @return
	 * @throws Exception
	 */
	public abstract boolean deleteRank(Rank rank) throws Exception;

	/**
	 * Deletes a plot from Datasource and executes a query
	 * 
	 * @param plot
	 * @return
	 * @throws Exception
	 */
	public abstract boolean deletePlot(ITownPlot plot) throws Exception;

	/**
	 * Deletes all of the specified flag and executes a query
	 * 
	 * @param flag
	 * @return
	 * @throws Exception
	 */
	public abstract boolean deletePlotFlag(ITownFlag flag) throws Exception;

	/**
	 * Deletes all of the specified flag and executes a query
	 * 
	 * @param flag
	 * @return
	 * @throws Exception
	 */
	public abstract boolean deleteTownFlag(ITownFlag flag) throws Exception;

	// /////////////////////////////////////////////////////////////
	// Delete Multiple Entities
	// /////////////////////////////////////////////////////////////

	/**
	 * Deletes the towns from Datasource and executes a query
	 * 
	 * @param town
	 * @return
	 */
	public void deleteTowns(Town... towns) throws Exception {
		for (Town t : towns) {
			deleteTown(t);
		}
	}

	/**
	 * Deletes the nations from Datasource and executes a query
	 * 
	 * @param town
	 * @return
	 */
	public void deleteNations(Nation... nations) throws Exception {
		for (Nation n : nations) {
			deleteNation(n);
		}
	}

	/**
	 * Deletes the townblocks from Datasource and executes a query
	 * 
	 * @param townBlocks
	 * @return
	 */
	public void deleteTownBlocks(TownBlock... townBlocks) throws Exception {
		for (TownBlock b : townBlocks) {
			deleteTownBlock(b);
		}
	}

	/**
	 * Deletes the residents from Datasource and executes a query
	 * 
	 * @param town
	 * @return
	 */
	public void deleteResidents(Resident... residents) throws Exception {
		for (Resident r : residents) {
			deleteResident(r);
		}
	}

	/**
	 * Deletes the ranks from Datasource and executes a query
	 * 
	 * @param town
	 * @param ranks
	 * @throws Exception
	 */
	public void deleteRanks(Rank... ranks) throws Exception {
		for (Rank r : ranks) {
			deleteRank(r);
		}
	}

	/**
	 * Deletes the plots from Datasource and executes a query
	 * 
	 * @param plots
	 * @throws Exception
	 */
	public void deletePlots(ITownPlot... plots) throws Exception {
		for (ITownPlot plot : plots) {
			deletePlot(plot);
		}
	}

	/**
	 * Deletes the flags from the specified plot and executes a query
	 * 
	 * @param plot
	 * @param flags
	 * @throws Exception
	 */
	public void deletePlotFlags(ITownFlag... flags) throws Exception {
		for (ITownFlag flag : flags) {
			deletePlotFlag(flag);
		}
	}

	/**
	 * Deletes the flags from the specified town and executes a query
	 * 
	 * @param town
	 * @param flags
	 * @throws Exception
	 */
	public void deleteTownFlags(ITownFlag... flags) throws Exception {
		for (ITownFlag flag : flags) {
			deleteTownFlag(flag);
		}
	}

	// /////////////////////////////////////////////////////////////
	// Linkages
	// /////////////////////////////////////////////////////////////

	/**
	 * Loads all stored links between Residents and Towns
	 * 
	 * @throws Exception
	 */
	public abstract void loadResidentToTownLinks() throws Exception;

	/**
	 * Loads all stored links between Towns and Nations
	 * 
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
		Rank rank = new Rank("Resident", Constants.DEFAULT_RANK_VALUES.get("Resident"), town);
		linkResidentToTown(resident, town, rank);
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

	/**
	 * Removes link of a Resident to the specified Town
	 * 
	 * @param resident
	 * @param town
	 * @throws Exception
	 */
	public abstract void unlinkResidentFromTown(Resident resident, Town town) throws Exception;

	/**
	 * Removes link of a Town to the specified Nation
	 * 
	 * @param town
	 * @param nation
	 * @throws Exception
	 */
	public abstract void unlinkTownFromNation(Town town, Nation nation) throws Exception;

	/**
	 * Updates the link of a resident to the town
	 * 
	 * @param resident
	 * @param town
	 * @throws Exception
	 */
	public abstract void updateLinkResidentToTown(Resident resident, Town town) throws Exception;

	/**
	 * Updates the link of a town to the nation
	 * 
	 * @param town
	 * @param nation
	 * @throws Exception
	 */
	public abstract void updateLinkTownToNation(Town town, Nation nation) throws Exception;

	// /////////////////////////////////////////////////////////////
	// Extras
	// /////////////////////////////////////////////////////////////

	public abstract void dump() throws Exception;

	// /////////////////////////////////////////////////////////////
	// Unknown Group/Helpers? TODO Change/Move Later?
	// /////////////////////////////////////////////////////////////

	/**
	 * Gets or makes a new Resident from the playerName
	 * 
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
	 * 
	 * @param player
	 * @return
	 * @throws Exception
	 */
	public Resident getOrMakeResident(EntityPlayer player) throws Exception {
		return getOrMakeResident(player.getCommandSenderName());
	}

	// ***** Everything below here is for internal use only! *****

	// /////////////////////////////////////////////////////////////
	// Add Single Entity - Internal Only
	// /////////////////////////////////////////////////////////////

	/**
	 * Adds a Town to the Datasource
	 * 
	 * @param town
	 */
	protected void addTown(Town town) throws Exception {
		log.info("Adding town %s", town.getName()); // TODO Remove later
		towns.put(town.getName(), town);
	}

	/**
	 * Adds a Resident to the Datasource
	 * 
	 * @param resident
	 */
	protected void addResident(Resident resident) throws Exception {
		residents.put(resident.getUUID(), resident);
	}

	/**
	 * Adds a Nation to the Datasource
	 * 
	 * @param nation
	 */
	protected void addNation(Nation nation) throws Exception {
		nations.put(nation.getName(), nation);
	}

	/**
	 * Adds a TownBlock to the Datasource
	 * 
	 * @param townBlock
	 */
	protected void addTownBlock(TownBlock townBlock) throws Exception {
		blocks.put(townBlock.getKey(), townBlock);
	}

	/**
	 * 
	 * Adds a Rank to the Datasource
	 * 
	 * @param rank
	 * @throws Exception
	 */
	protected void addRank(Rank rank) throws Exception {
		ranks.put(rank.getKey(), rank);
	}

	/**
	 * Adds a Plot to the Datasource
	 * 
	 * @param plot
	 * @throws Exception
	 */
	protected void addPlot(ITownPlot plot) throws Exception {
		plots.put(plot.getKey(), plot);
	}

	// /////////////////////////////////////////////////////////////
	// Add Multiple Entities - Internal Only
	// /////////////////////////////////////////////////////////////

	/**
	 * Adds multiple Towns to the towns Map
	 * 
	 * @param towns
	 * @throws Exception
	 */
	protected void addTowns(Town... towns) throws Exception {
		for (Town town : towns) {
			addTown(town);
		}
	}

	/**
	 * Adds multiple Residents to the residents Map
	 * 
	 * @param residents
	 * @throws Exception
	 */
	protected void addResidents(Resident... residents) throws Exception {
		for (Resident res : residents) {
			addResident(res);
		}
	}

	/**
	 * Adds multiple Nations to the nations Map
	 * 
	 * @param nations
	 * @throws Exception
	 */
	protected void addNations(Nation... nations) throws Exception {
		for (Nation nation : nations) {
			addNation(nation);
		}
	}

	/**
	 * Adds multiple TownBlocks to the blocks Map
	 * 
	 * @param townBlocks
	 * @throws Exception
	 */
	protected void addTownBlocks(TownBlock... townBlocks) throws Exception {
		for (TownBlock block : townBlocks) {
			addTownBlocks(block);
		}
	}

	/**
	 * Adds multiple Ranks to the ranks Map
	 * 
	 * @param ranks
	 * @throws Exception
	 */
	protected void addRanks(Rank... ranks) throws Exception {
		for (Rank r : ranks) {
			addRank(r);
		}
	}

	/**
	 * Add multiple Plots to the plots Map
	 * 
	 * @param plots
	 * @throws Exception
	 */
	protected void addPlots(ITownPlot... plots) throws Exception {
		for (ITownPlot plot : plots) {
			addPlot(plot);
		}
	}

	// /////////////////////////////////////////////////////////////
	// Remove Single Entity - Internal Only
	// /////////////////////////////////////////////////////////////

	/**
	 * Removes a Town from the Datasource
	 * 
	 * @param town
	 */
	protected boolean removeTown(Town town) {
		for (Nation n : town.getNations()) {
			n.removeTown(town);
		}
		for (Resident r : town.getResidents()) {
			r.removeTown(town);
		}
		for (TownBlock block : town.getTownBlocks()) {
			removeTownBlock(block);
		}
		return towns.remove(town.getName()) != null;
	}

	/**
	 * Removes a Resident from the Datasource
	 * 
	 * @param resident
	 */
	protected boolean removeResident(Resident resident) {
		for (Town t : resident.getTowns()) {
			t.removeResident(resident);
		}
		return residents.remove(resident.getUUID()) != null;
	}

	/**
	 * Removes a Nation from the Datasource
	 * 
	 * @param nation
	 */
	protected boolean removeNation(Nation nation) {
		for (Town t : nation.getTowns()) {
			t.removeNation(nation);
		}
		return nations.remove(nation.getName()) != null;
	}

	/**
	 * Removes a TownBlock from the Datasource
	 * 
	 * @param townBlock
	 */
	protected boolean removeTownBlock(TownBlock townBlock) {
		townBlock.getTown().removeTownBlock(townBlock);
		return blocks.remove(townBlock.getKey()) != null;
	}

	/**
	 * Removes a Rank from the Datasource
	 * 
	 * @param rank
	 * @return
	 */
	protected boolean removeRank(Rank rank) {
		for (Town t : towns.values())
			if (t.getRanks().contains(rank)) {
				t.removeRank(rank);
			}
		return ranks.remove(rank.getTown().getName() + ":" + rank.getName()) != null;
	}

	/**
	 * Removes a TownPlot from the Datasource
	 * 
	 * @param plot
	 * @return
	 */
	protected boolean removePlot(ITownPlot plot) {
		return plots.remove(plot.getKey(), plot);
	}

	// /////////////////////////////////////////////////////////////
	// Remove Multiple Entities - Internal Only
	// /////////////////////////////////////////////////////////////

	/**
	 * Removes the Towns from the towns Map
	 * 
	 * Should not be used outside the Datasource!
	 * 
	 * @param towns
	 */
	protected void removeTowns(Town... towns) {
		for (Town town : towns) {
			removeTown(town);
		}
	}

	/**
	 * Removes multiple Ranks from the ranks Map
	 * 
	 * Should not be used outside the Datasource!
	 * 
	 * @param ranks
	 */
	protected void removeRanks(Rank... ranks) {
		for (Rank r : ranks) {
			removeRank(r);
		}
	}

	/**
	 * Removes the Residents from the Datasource
	 * 
	 * @param residents
	 */
	protected void removeResidents(Resident... residents) {
		for (Resident res : residents) {
			removeResident(res);
		}
	}

	/**
	 * Removes the Nations from the Datasource
	 * 
	 * @param nations
	 */
	protected void removeNations(Nation... nations) {
		for (Nation nation : nations) {
			removeNation(nation);
		}
	}

	/**
	 * Removes the TownBlocks from the Datasource
	 * 
	 * @param blocks
	 */
	protected void removeTownBlocks(TownBlock... blocks) {
		for (TownBlock block : blocks) {
			removeTownBlock(block);
		}
	}

	/**
	 * Removes the TownPlots from the Datasource
	 * 
	 * @param plots
	 */
	protected void removePlots(ITownPlot... plots) {
		for (ITownPlot plot : plots) {
			removePlot(plot);
		}
	}
}