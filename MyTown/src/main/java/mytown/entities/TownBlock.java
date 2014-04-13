package mytown.entities;

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

	// TODO Add flags/permissions

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
		key = String.format(keyFormat, dim, x, z);
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
}