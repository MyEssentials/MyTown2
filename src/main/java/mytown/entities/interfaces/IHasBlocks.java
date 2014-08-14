package mytown.entities.interfaces;

import com.google.common.collect.ImmutableCollection;
import mytown.entities.Block;

import java.util.Collection;

/**
 * @author Joe Goett
 */
public interface IHasBlocks {
    /**
     * Adds the Block to this entity
     *
     * @param block
     */
    public void addBlock(Block block);

    /**
     * Remove the Block from this entity
     *
     * @param block
     */
    public void removeBlock(Block block);

    /**
     * Checks if this entity has the Block
     *
     * @param block
     * @return
     */
    public boolean hasBlock(Block block);

    /**
     * Returns the Collection of Blocks
     *
     * @return
     */
    public ImmutableCollection<Block> getBlocks();

    /**
     * Returns the Block in the dim at the given chunk coords
     *
     * @param dim
     * @param x
     * @param z
     * @return
     */
    public Block getBlockAtCoords(int dim, int x, int z);
}
