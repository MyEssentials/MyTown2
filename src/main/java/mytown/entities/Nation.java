package mytown.entities;

import mytown.api.container.TownsContainer;

public class Nation implements Comparable<Nation> {
    private String name;

    public final TownsContainer townsContainer = new TownsContainer();

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

    /* ----- Comparable ----- */

    @Override
    public int compareTo(Nation n) { // TODO Flesh this out some more?
        int thisNumberOfTowns = townsContainer.size(),
                thatNumberOfTowns = n.townsContainer.size();
        if (thisNumberOfTowns > thatNumberOfTowns)
            return -1;
        else if (thisNumberOfTowns == thatNumberOfTowns)
            return 0;
        else if (thisNumberOfTowns < thatNumberOfTowns)
            return 1;

        return -1;
    }
}
