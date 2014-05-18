package mytown.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mytown.entities.flag.EnumFlagValue;
import mytown.interfaces.ITownFlag;
import mytown.interfaces.ITownPlot;

/**
 * Defines what a Town block is. A single chunk that belongs to a single town.
 * 
 * @author Joe Goett
 */
public class TownBlock {
	public static String keyFormat = "%s;%s;%s";

	private int id;
	private int dim;
	private int x, z;
	private Town town;
	private String key;

	private List<ITownPlot> townPlots;
	
	/**
	 * Used internally only!
	 * 
	 * @param id
	 * @param x
	 * @param z
	 * @param dim
	 */
	public TownBlock(int id, Town town, int x, int z, int dim) {
		this(town, x, z, dim);
		this.id = id;
	}

	/**
	 * Creates a TownBlock with the given x, z, and dim
	 * 
	 * @param x
	 * @param z
	 * @param dim
	 */
	public TownBlock(Town town, int x, int z, int dim) {
		this.x = x;
		this.z = z;
		this.dim = dim;
		this.town = town;
		this.key = String.format(keyFormat, dim, x, z);
		this.townPlots = new ArrayList<ITownPlot>();
	}

	/**
	 * Returns the id of the block. Used only when loading/saving!
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * Used internally only
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the dim the block resides in
	 * 
	 * @return
	 */
	public int getDim() {
		return dim;
	}

	/**
	 * Returns the x position of the block
	 * 
	 * @return
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the z position of the block
	 * 
	 * @return
	 */
	public int getZ() {
		return z;
	}

	/**
	 * Returns the Town that this TownBlock belongs to
	 * 
	 * @return
	 */
	public Town getTown() {
		return town;
	}

	/**
	 * Used to store the TownBlock in a Map for easy access
	 * 
	 * @return
	 */
	public String getKey() {
		return key;
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
	
	/**
	 * Gets all the flags for the specified block
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	
	public Map<String, ITownFlag> getFlagMapForBlockCoords(int x, int y, int z) {
		ITownPlot plot = null;
		for(ITownPlot p : townPlots) {
			if(p.isBlockInsidePlot(x, y, z))
				plot = p;
		}
		if(plot == null)
			return null;
		return plot.getTownFlags();
	}
	
	/**
	 * Gets the value of a flag on the specified block
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param flagKey
	 * @return
	 */
	public EnumFlagValue getValueForFlagOnBlock(int x, int y, int z, String flagKey) {
		Map<String, ITownFlag> map = getFlagMapForBlockCoords(x, y, z);
		if(map == null) return null;
		ITownFlag flag = map.get(flagKey);
		if(flag == null) return null;
		return flag.getValue();
	}
}