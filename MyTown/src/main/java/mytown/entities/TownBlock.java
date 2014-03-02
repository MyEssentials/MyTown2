package mytown.entities;

/**
 * Defines a Town block.
 * @author Joe Goett
 */
public class TownBlock {
	private int id;
	private int type;  // TODO Make type an enum or class instead
	private int dim;
	private int x;
	private int z;
	private int townID;
	private int playerID = -1;
	private float price = 0;
	// TODO Add flags/permissions
	
	/**
	 * Used internally only!
	 * @param id
	 * @param x
	 * @param z
	 * @param dim
	 */
	public TownBlock(int id, int x, int z, int dim){
		this(x, z, dim);
		this.id = id;
	}
	
	/**
	 * Creates a TownBlock with the given x, z, and dim
	 * @param x
	 * @param z
	 * @param dim
	 */
	public TownBlock(int x, int z, int dim){
		this.x = x;
		this.z = z;
		this.dim = dim;
	}

	/**
	 * Returns the id of the block. Used only when loading/saving!
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the type of block this is
	 * @return
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Sets the type of block this is
	 * @param type
	 */
	public void setType(int type){
		this.type = type;
	}

	/**
	 * Returns the dim the block resides in
	 * @return
	 */
	public int getDim() {
		return dim;
	}

	/**
	 * Returns the x position of the block
	 * @return
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the z position of the block
	 * @return
	 */
	public int getZ() {
		return z;
	}

	/**
	 * Returns the TownID associated with the block
	 * @return
	 */
	public int getTownID() {
		return townID;
	}

	/**
	 * Returns the PlayerID of the owner. (-1 means no owner)
	 * @return
	 */
	public int getPlayerID() {
		return playerID;
	}

	/**
	 * Sets the owner id of this block (-1 means no owner)
	 * @param playerID
	 */
	public void setPlayerID(int playerID){
		this.playerID = playerID;
	}
	
	/**
	 * Returns the price of the block
	 * @return
	 */
	public float getPrice() {
		return price;
	}

	/**
	 * Sets the price of the TownBlock
	 * @param price
	 */
	public void setPrice(float price){
		this.price = price;
	}

	/**
	 * Used to store the TownBlock in a Map for easy access
	 * @return
	 */
	public String getKey(){
		return dim + ";" + x + ";" + z;
	}
}