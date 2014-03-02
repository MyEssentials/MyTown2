package mytown.entities;

/**
 * Defines a Town
 * @author Joe Goett
 */
public class Town extends TownBlockOwner{
	private int id;
	private String name;
	private int extraBlocks = 0;
	// TODO Add flags/permissions
	
	/**
	 * Creates a town with the given name
	 * @param name
	 */
	public Town(String name){
		this.name = name;
	}
	
	/**
	 * Used internally only!
	 * @param id
	 * @param name
	 */
	public Town(int id, String name){
		this(name);
		this.id = id;
	}

	/**
	 * Returns the ID of the town
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the name of the town
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the amount of extra blocks this town is given
	 * @return
	 */
	public int getExtraBlocks() {
		return extraBlocks;
	}
}