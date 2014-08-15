package mytown.datasource;

import com.google.common.collect.ImmutableMap;
import mytown.entities.*;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author Joe Goett
 */
public class MyTownUniverse { // TODO Allow migrating between different Datasources
    protected Map<String, Resident> residents = new Hashtable<String, Resident>();
    protected Map<String, Town> towns = new Hashtable<String, Town>();
    protected Map<String, Nation> nations = new Hashtable<String, Nation>();
    protected Map<String, Block> blocks = new Hashtable<String, Block>();
    protected Map<String, Plot> plots = new Hashtable<String, Plot>();
    protected Map<String, Rank> ranks = new Hashtable<String, Rank>();

    /**
     * Returns an ImmutableMap of Residents
     *
     * @return ImmutableMap of Residents
     */
    public final ImmutableMap<String, Resident> getResidentsMap() {
        return ImmutableMap.copyOf(residents);
    }

    /**
     * Returns an ImmutableMap of Towns
     *
     * @return ImmutableMap of Towns
     */
    public final ImmutableMap<String, Town> getTownsMap() {
        return ImmutableMap.copyOf(towns);
    }

    /**
     * Returns an ImmutableMap of Nations
     *
     * @return ImmutableMap of Nations
     */
    public final ImmutableMap<String, Nation> getNationsMap() {
        return ImmutableMap.copyOf(nations);
    }

    /**
     * Returns an ImmutableMap of Blocks
     *
     * @return ImmutableMap of Blocks
     */
    public final ImmutableMap<String, Block> getBlocksMap() {
        return ImmutableMap.copyOf(blocks);
    }

    /**
     * Returns an ImmutableMap of Plots
     *
     * @return ImmutableMap of Plots
     */
    public final ImmutableMap<String, Plot> getPlotsMap() {
        return ImmutableMap.copyOf(plots);
    }

    /**
     * Returns an ImmutableMap of Ranks
     *
     * @return ImmutableMap of Ranks
     */
    public final ImmutableMap<String, Rank> getRanksMap() {
        return ImmutableMap.copyOf(ranks);
    }
}
