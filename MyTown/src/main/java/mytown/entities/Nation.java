package mytown.entities;

import java.util.List;

/**
 * Defines a Nation
 * @author Joe Goett
 */
public class Nation {
	private int id;
	private String name;
	private int extraBlocksPerTown;
	private List<Town> towns;
	
	/**
	 * Creates a Nation with the given name and extraBlocksPerTown
	 * @param name
	 * @param extraBlocksPerTown
	 */
	public Nation(String name, int extraBlocksPerTown){
		this.name = name;
		this.extraBlocksPerTown = extraBlocksPerTown;
	}

	/**
	 * Creates a Nation with the given name
	 * @param name
	 */
	public Nation(String name){
		this(name ,0);
	}
	
	/**
	 * Used internally only!
	 * @param id
	 * @param name
	 * @param extraBlocksPerTown
	 */
	public Nation(int id, String name, int extraBlocksPerTown){
		this(name, extraBlocksPerTown);
		this.id = id;
	}
	
	/**
	 * Returns the id of the Nation
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the name of the nation
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the Nation
	 * @param name
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Returns the number of extra blocks each town receives
	 * @return
	 */
	public int getExtraBlocksPerTown() {
		return extraBlocksPerTown;
	}

	/**
	 * Sets the number of extra blocks each town gets per new town
	 * @param extra
	 */
	public void setExtraBlocksPerTown(int extra){
		extraBlocksPerTown = extra;
	}
	
	/**
	 * Returns the towns associated with this Nation
	 * @return
	 */
	public List<Town> getTowns() {
		return towns;
	}
}