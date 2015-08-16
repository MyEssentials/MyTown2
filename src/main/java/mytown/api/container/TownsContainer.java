package mytown.api.container;

import mytown.entities.Town;

import java.util.ArrayList;

public class TownsContainer extends ArrayList<Town> {

    private Town mainTown;

    public boolean contains(String name) {
        for(Town town : this) {
            if(town.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void setMainTown(Town town) {
        if(contains(town)) {
            mainTown = town;
        }
    }

    public Town getMainTown() {
        return mainTown;
    }
}
