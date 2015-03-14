package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.TownBlock;

/**
 * @author Joe Goett
 */
public interface IHasBlocks {
    public void addBlock(TownBlock block);

    public void removeBlock(TownBlock block);

    public boolean hasBlock(TownBlock block);

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

    public int getExtraBlocks();

    public void setExtraBlocks(int extra);

    public int getMaxBlocks();
}
