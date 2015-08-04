package mytown.api.container.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.TownBlock;

public interface ITownBlocksContainer {
    void addBlock(TownBlock block);

    void removeBlock(TownBlock block);

    boolean hasBlock(TownBlock block);

    ImmutableList<TownBlock> getBlocks();

    /**
     * Returns the Block in the dim at the given chunk coords
     */
    TownBlock getBlockAtCoords(int dim, int x, int z);

    int getExtraBlocks();

    void setExtraBlocks(int extra);

    int getMaxBlocks();

    int getMaxFarClaims();

    void setMaxFarClaims(int maxFarClaims);
}
