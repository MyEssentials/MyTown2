package mytown.entities.interfaces;

import mytown.entities.BlockWhitelist;
import mytown.entities.flag.FlagType;

import java.util.List;

/**
 * Created by AfterWind on 9/4/2014.
 *
 */
public interface IHasBlockWhitelists {

    void addBlockWhitelist(BlockWhitelist bw);

    boolean hasBlockWhitelist(BlockWhitelist bw);
    boolean hasBlockWhitelist(int dim, int x, int y, int z, FlagType flagType, int plotID);

    void removeBlockWhitelist(BlockWhitelist bw);
    void removeBlockWhitelist(int dim, int x, int y, int z, FlagType flagType, int plotID);

    BlockWhitelist getBlockWhitelist(int dim, int x, int y, int z, FlagType flagType, int plotID);
    List<BlockWhitelist> getWhitelists();
}
