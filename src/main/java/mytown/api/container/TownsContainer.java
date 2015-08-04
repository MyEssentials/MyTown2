package mytown.api.container;

import com.google.common.collect.ImmutableList;
import mytown.entities.Town;

import java.util.ArrayList;

public class TownsContainer {

    private ArrayList<Town> towns = new ArrayList<Town>();

    public void addTown(Town town) {
        towns.add(town);
    }

    public void removeTown(Town town) {
        towns.remove(town);
    }

    public boolean hasTown(Town town) {
        return towns.contains(town);
    }

    public boolean hasTown(String name) {
        for(Town town : towns) {
            if(town.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public ImmutableList<Town> getTowns() {
        return ImmutableList.copyOf(towns);
    }
}
