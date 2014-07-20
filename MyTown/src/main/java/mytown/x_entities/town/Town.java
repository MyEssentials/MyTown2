package mytown.x_entities.town;

import java.util.*;

import mytown.api.datasource.MyTownDatasource;
import mytown.x_entities.Nation;
import mytown.x_entities.Rank;
import mytown.x_entities.Resident;
import mytown.x_entities.TownBlock;
import mytown.interfaces.ITownFlag;
import mytown.interfaces.ITownPlot;
import mytown.proxies.DatasourceProxy;
import net.minecraft.util.EnumChatFormatting;

// TODO Add Comments

/**
 * Defines a Town
 * 
 * @author Joe Goett
 */
public class Town implements Comparable<Town> {

	protected String name;
	protected int extraBlocks = 0;

	protected double spawnX, spawnY, spawnZ;
	protected int spawnDim;

	// TODO: Always have town spawn?...
	protected boolean hasSpawn;

	protected List<Rank> ranks;
	protected List<ITownFlag> flags;
	protected List<Nation> nations;
	protected List<TownBlock> townBlocks;
	protected Map<Resident, Rank> residents;

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
		flags = new ArrayList<ITownFlag>();
		nations = new ArrayList<Nation>();
		townBlocks = new ArrayList<TownBlock>();
		residents = new WeakHashMap<Resident, Rank>();
	}

	/**
	 * Returns the name of the town
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	// ///////////////////////////////////////
	// Spawn coords
	// ///////////////////////////////////////

	public double getSpawnX() {
		return spawnX;
	}

	public double getSpawnY() {
		return spawnY;
	}

	public double getSpawnZ() {
		return spawnZ;
	}

	public int getSpawnDim() {
		return spawnDim;
	}

	public void setSpawn(double x, double y, double z, int dim) {
		spawnDim = dim;
		spawnX = x;
		spawnY = y;
		spawnZ = z;
		hasSpawn = true;
	}

	/**
	 * Sets whether or not the town has a spawn or not
	 * 
	 * @param state
	 */
	public void setSpawnState(boolean state) {
		hasSpawn = state;
	}

	public boolean hasSpawn() {
		return hasSpawn;
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

	/**
	 * Adds the given TownBlocks to this Town
	 * 
	 * @param townBlocks
	 */
	public void addTownBlocks(List<TownBlock> townBlocks) {
		// Just to make the singular version easily overridable
		for (TownBlock block : townBlocks) {
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
        if (resident == null) throw new NullPointerException("Resident can not be null");
        if (rank == null) throw new NullPointerException("Rank can not be null");
		residents.put(resident, rank);
	}

	/**
	 * Removes the Resident
	 * 
	 * @param resident
	 */
	public void removeResident(Resident resident) {
		residents.remove(resident);
		resident.removeTown(this);
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
	 * @param UUID
	 * @return
	 */
	public boolean hasResident(String UUID) {
		for (Resident r : residents.keySet())
			if (r.getUUID().equals(UUID))
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
		if (!hasResident(resident))
			return; // TODO Log/Throw Exception
		addResident(resident, rank);
	}

	/**
	 * Gets the rank of the specified resident
	 * 
	 * @param resident
	 * @return
	 */
	public Rank getResidentRank(Resident resident) {
		if (hasResident(resident))
			return residents.get(resident);
		else
			return getRank("Outsider");
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
		return ranks;
	}

	/**
	 * Gets a rank
	 * 
	 * @param name
	 * @return
	 */
	public Rank getRank(String name) {
		for (Rank r : ranks)
			if (r.getName().equals(name))
				return r;
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
			if (r.getName() == rank.getName())
				return;
		ranks.add(rank);
	}

	/**
	 * Checks if the Rank exists in this town
	 * 
	 * @param rank
	 * @return
	 */
	public boolean hasRank(Rank rank) {
		return ranks.contains(rank);
	}

	/**
	 * Checks if a rank has the name specified
	 * 
	 * @param name
	 * @return
	 */
	public boolean hasRankName(String name) {
		for (Rank r : ranks)
			if (r.getName().equals(name))
				return true;
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
		for (ITownFlag flag : flags) {
			if (flag.getName().equals(name))
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
		return flags;
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
		if (plot == null)
			return getFlags();
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
		if (plot == null)
			return getFlag(flagName);
		return plot.getFlag(flagName);
	}

    // //////////////////////////////////////
    // Plots
    // //////////////////////////////////////
    protected List<ITownPlot> plots = new ArrayList<ITownPlot>();

    /**
     * Adds the {@link ITownPlot}
     * @param plot
     */
    public void addPlot(ITownPlot plot) {
        plots.add(plot);
    }

    /**
     * Removes the {@link ITownPlot}
     * @param plot
     */
    public void removePlot(ITownPlot plot) {
        plots.remove(plot);
    }

    /**
     * Returns the list of {@link ITownPlot}
     * @return
     */
    public List<ITownPlot> getPlots() {
        return plots;
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
        for (ITownPlot p : plots) {
            if (p.isBlockInsidePlot(x, y, z))
                return p;
        }
        return null;
    }

	// //////////////////////////////////////
	// Helper?
	// //////////////////////////////////////

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns whether or not the block specified by X and Z coords (Block Coords NOT Chunk Coords) is int this Town Note: Param Y is not needed since TownBlocks are Y independent
	 * 
	 * @param x
	 * @param z
	 * @param dim
	 * @return
	 */
	public boolean isBlockInTown(int x, int z, int dim) {
		for (TownBlock block : townBlocks)
			if (block.isBlockInChunk(x, z, dim))
				return true;
		return false;
	}

	/**
	 * Gets the info for the town
	 * 
	 * TODO Make everything use localization?
	 * 
	 * @return
	 */
	public String[] getInfo() {
		String temp[] = new String[3];
		temp[0] = EnumChatFormatting.BLUE + " ---------- " + name + EnumChatFormatting.GREEN + " (" + EnumChatFormatting.WHITE + "R:" + residents.size() + EnumChatFormatting.GREEN + " | " + EnumChatFormatting.WHITE + "B:" + townBlocks.size() + EnumChatFormatting.GREEN + " | " + EnumChatFormatting.WHITE + "P:" + plots.size() + EnumChatFormatting.GREEN + ")" + EnumChatFormatting.BLUE + " ----------" + '\n' + EnumChatFormatting.GRAY;

		for (Resident res : residents.keySet()) {
			if (temp[1] == null) {
				temp[1] = "";
			} else {
				temp[1] += EnumChatFormatting.GRAY + ", ";
			}
			temp[1] += String.format("%s" + EnumChatFormatting.GRAY + "(%s" + EnumChatFormatting.GRAY + ")", res, residents.get(res));
		}

		temp[1] += "\n" + EnumChatFormatting.GRAY;

		for (Rank rank : ranks) {
			if (temp[2] == null) {
				temp[2] = "";
			} else {
				temp[2] += ", ";
			}
			temp[2] += rank;
		}

		temp[2] += "\n" + EnumChatFormatting.GRAY;
		return temp;
	}

	@Override
	public int compareTo(Town t) { // TODO Flesh this out more for ranking towns?
		int thisNumberOfResidents = residents.size(), thatNumberOfResidents = t.getResidents().size();
		if (thisNumberOfResidents > thatNumberOfResidents)
			return -1;
		else if (thisNumberOfResidents == thatNumberOfResidents)
			return 0;
		else if (thisNumberOfResidents < thatNumberOfResidents)
			return 1;

		return -1;
	}

	/**
	 * Gets the character that represents the type of this town. Only used in datasource
	 * 
	 * @return
	 */
	public String getType() {
		if (this instanceof AdminTown)
			return "A";
		return "T";
	}

	protected MyTownDatasource getDatasource() {
		return DatasourceProxy.getDatasource();
	}

}