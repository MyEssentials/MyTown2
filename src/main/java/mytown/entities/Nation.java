package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.api.interfaces.ITownsContainer;

import java.util.HashMap;
import java.util.Map;

public class Nation implements ITownsContainer, Comparable<Nation> {
    private String name;
    private Map<Town, Rank> towns = new HashMap<Town, Rank>();

    public Nation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Nation: {Name: %s}", name);
    }

    /* ----- IHasTowns ----- */



    @Override
    public void addTown(Town town) {
        towns.put(town, Rank.TOWN);
    }

    @Override
    public void removeTown(Town town) {
        towns.remove(town);
    }

    @Override
    public boolean hasTown(Town town) {
        return towns.containsKey(town);
    }

    @Override
    public ImmutableList<Town> getTowns() {
        return ImmutableList.copyOf(towns.keySet());
    }

    /**
     * Sets the given Town to the Rank
     */
    public void promoteTown(Town town, Rank rank) {
        towns.put(town, rank);
    }

    /**
     * Returns the Rank of the given town
     */
    public Rank getTownRank(Town town) {
        if (towns.containsKey(town))
            return towns.get(town);
        return Rank.NONE;
    }

    /* ----- Comparable ----- */

    @Override
    public int compareTo(Nation n) { // TODO Flesh this out some more?
        int thisNumberOfTowns = getTowns().size(),
                thatNumberOfTowns = n.getTowns().size();
        if (thisNumberOfTowns > thatNumberOfTowns)
            return -1;
        else if (thisNumberOfTowns == thatNumberOfTowns)
            return 0;
        else if (thisNumberOfTowns < thatNumberOfTowns)
            return 1;

        return -1;
    }

    /* ----- Others ----- */

    /**
     * Defines the Rank of a Town in the Nation
     */
    public enum Rank { // TODO Change this?
        NONE, TOWN, CAPITAL;

        /**
         * Gets the rank based on [N, T, C]
         */
        public static Rank parse(String rank) {
            for (Rank type : Rank.values()) {
                if (type.toString().toLowerCase().startsWith(rank.toLowerCase()))
                    return type;
            }
            return Rank.NONE;
        }

        @Override
        public String toString() {
            return super.toString().substring(0, 1);
        }
    }
}
