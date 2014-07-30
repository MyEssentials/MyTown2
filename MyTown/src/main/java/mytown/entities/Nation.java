package mytown.entities;

import mytown.entities.interfaces.IHasTowns;

import java.util.Collection;

/**
 * @author Joe Goett
 */
public class Nation implements IHasTowns, Comparable<Nation> {
    private String name;

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

    /* ----- IHasTowns ----- */ // TODO Add Towns to Nation
    public void addTown(Town town) {
    }

    public void removeTown(Town town) {
    }

    public boolean hasTown(Town town) {
        return false;
    }

    public Collection<Town> getTowns() {
        return null;
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
}
