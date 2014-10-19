package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.TownBlock;

/**
 * @author Joe Goett
 */
public interface IHasBlocks {
    /**
     * Adds the Block to this entity
     *
     * @param block
     */
    public void addBlock(TownBlock block);

    /**
     * Remove the Block from this entity
     *
     * @param block
     */
    public void removeBlock(TownBlock block);

    /**
     * Checks if this entity has the Block
     *
     * @param block
     * @return
     */
    public boolean hasBlock(TownBlock block);

    /**
     * Returns the Collection of Blocks
     *
     * @return
     */
    public ImmutableList<TownBlock> getBlocks();

    /**
     * Returns the Block in the dim at the given chunk coords
     *
     * @param dim
     * @param x
     * @param z
     * @return
     */
    public TownBlock getBlockAtCoords(int dim, int x, int z);

    /**
     * Returns the number of extra blocks that it can have. Default should be 0.
     *
     * @return
     */
    public int getExtraBlocks();

    /**
     * Sets the number of extra blocks it can have.
     * @param extra
     */
    public void setExtraBlocks(int extra);

    /**
     * Returns the maximum amount of blocks that it can have
     *
     * @return
     */
    public int getMaxBlocks();

    /**
     * Returns if it can accept any more town blocks
     *
     * @return
     */
    public boolean hasMaxAmountOfBlocks();
}
