package mytown.api.container.interfaces;

import mytown.entities.BlockWhitelist;
import mytown.entities.flag.FlagType;

import java.util.List;

/**
 * Represents an objects that can hold BlockWhitelists
 */
public interface IBlockWhitelistsContainer {
    void addBlockWhitelist(BlockWhitelist bw);

    boolean hasBlockWhitelist(BlockWhitelist bw);

    /**
     * Returns if there is a BlockWhitelist at the given location with given flagType.
     */
    boolean hasBlockWhitelist(int dim, int x, int y, int z, FlagType flagType);

    void removeBlockWhitelist(BlockWhitelist bw);

    /**
     * Removes BlockWhitelist if there is one at the given location with given flagType.
     */
    void removeBlockWhitelist(int dim, int x, int y, int z, FlagType flagType);

    /**
     * Returns BlockWhitelist if there is one at the given location with given flagType.
     */
    BlockWhitelist getBlockWhitelist(int dim, int x, int y, int z, FlagType flagType);

    List<BlockWhitelist> getWhitelists();
}
