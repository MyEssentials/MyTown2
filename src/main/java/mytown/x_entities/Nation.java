package mytown.x_entities;

import mytown.x_entities.town.Town;
import net.minecraft.util.EnumChatFormatting;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Defines a Nation
 *
 * @author Joe Goett
 */
public class Nation {
    public enum Rank {
        None, Town, Capital;

        /**
         * Gets the rank based on [N, T, C]
         */
        public static Rank parse(String rank) {
            for (Rank type : Rank.values()) {
                if (type.toString().toLowerCase().startsWith(rank.toLowerCase()))
                    return type;
            }
            return Rank.None;
        }

        @Override
        public String toString() {
            return super.toString().substring(0, 1);
        }
    }

    private String name;
    private int extraBlocksPerTown;

    /**
     * Creates a Nation with the given name and extraBlocksPerTown
     *
     * @param name
     * @param extraBlocksPerTown
     */
    public Nation(String name, int extraBlocksPerTown) {
        this.name = name;
        this.extraBlocksPerTown = extraBlocksPerTown;
    }

    /**
     * Creates a Nation with the given name
     *
     * @param name
     */
    public Nation(String name) {
        this(name, 0);
    }

    /**
     * Returns the name of the nation
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the Nation
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the number of extra blocks each town receives
     *
     * @return
     */
    public int getExtraBlocksPerTown() {
        return extraBlocksPerTown;
    }

    /**
     * Sets the number of extra blocks each town gets per new town
     *
     * @param extra
     */
    public void setExtraBlocksPerTown(int extra) {
        extraBlocksPerTown = extra;
    }

    // //////////////////////////////////////
    // Towns
    // //////////////////////////////////////
    private Map<Town, Rank> towns = new WeakHashMap<Town, Rank>();

    /**
     * Returns the towns associated with this Nation
     *
     * @return
     */
    public Set<Town> getTowns() {
        return towns.keySet();
    }

    /**
     * Returns the Rank of the Town
     *
     * @param town
     * @return
     */
    public Nation.Rank getTownRank(Town town) {
        if (hasTown(town))
            return towns.get(town);
        else
            return Rank.None;
    }

    /**
     * Adds a Town with the given Rank
     *
     * @param town
     * @param rank
     */
    public void addTown(Town town, Rank rank) {
        if (town == null) throw new NullPointerException("Town can not be null");
        if (rank == null) throw new NullPointerException("Rank can not be null");
        towns.put(town, rank);
    }

    /**
     * Checks if the Town is part of this Nation
     *
     * @param town
     * @return
     */
    public boolean hasTown(Town town) {
        return towns.containsKey(town);
    }

    /**
     * Promotes a Town to the given Rank
     *
     * @param town
     * @param rank
     */
    public void setTownRank(Town town, Rank rank) {
        if (!hasTown(town))
            return; // TODO Log/Throw Exception
        addTown(town, rank);
    }

    /**
     * Removes the given Town
     *
     * @param town
     */
    public void removeTown(Town town) {
        towns.remove(town);
    }

    @Override
    public String toString() {
        String temp = EnumChatFormatting.RED + name;
        return temp;
    }
}