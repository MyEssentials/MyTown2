package mytown.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines an object that owns TownBlocks
 * @author Joe Goett
 */
public class TownBlockOwner {
	protected List<TownBlock> townBlocks = new ArrayList<TownBlock>();
	
	/**
	 * Sets the blocks owned by this object
	 * @param townBlocks
	 */
	public void setTownBlocks(List<TownBlock> townBlocks){
		this.townBlocks = townBlocks;
	}
	
	/**
	 * Add a block to this object
	 * @param block
	 */
	public void addTownBlock(TownBlock block){
		townBlocks.add(block);
	}
	
	/**
	 * Remove a block from this object
	 * @param block
	 */
	public void removeTownBlock(TownBlock block){
		townBlocks.remove(block);
	}
	
	/**
	 * Checks if this object has the block
	 * @param block
	 * @return
	 */
	public boolean hasTownBlock(TownBlock block){
		return townBlocks.contains(block);
	}
	
	/**
	 * Returns all blocks this object has
	 * @return
	 */
	public List<TownBlock> getTownBlocks(){
		return townBlocks;
	}
}