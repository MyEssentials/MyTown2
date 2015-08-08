package mytown.api.container;

import myessentials.entities.Container;
import mytown.entities.Town;

public class TownsContainer extends Container<Town> {

    private Town mainTown;

    public boolean contains(String name) {
        for(Town town : items) {
            if(town.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void setMainTown(Town town) {
        if(items.contains(town)) {
            mainTown = town;
        }
    }

    public Town getMainTown() {
        return mainTown;
    }
}
