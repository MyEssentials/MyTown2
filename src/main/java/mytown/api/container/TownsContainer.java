package mytown.api.container;

import mytown.entities.Rank;
import mytown.entities.Town;
import myessentials.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class TownsContainer extends ArrayList<Town> {

    private Town mainTown;

    @Override
    public boolean add(Town town) {
        if(mainTown == null) {
            mainTown = town;
        }
        return super.add(town);
    }

    public Town get(String name) {
        for(Town town : this) {
            if(town.getName().equals(name)) {
                return town;
            }
        }
        return null;
    }

    public void remove(String name) {
        for(Iterator<Town> it = iterator(); it.hasNext(); ) {
            Town town = it.next();
            if(town.getName().equals(name)) {
                it.remove();
            }
        }
    }

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
        if(!contains(mainTown) || mainTown == null) {
            if(size() == 0) {
                return null;
            } else {
                mainTown = get(0);
            }
        }

        return mainTown;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean colorMainTown) {
        String formattedList = null;
        for(Town town : this) {
            String mayorName = town.residentsMap.getMayor() != null ? ColorUtils.colorPlayer + town.residentsMap.getMayor().getPlayerName()
                    : ColorUtils.colorAdmin + "SERVER ADMINS";
            String toAdd = ((colorMainTown && town == mainTown) ? ColorUtils.colorSelectedTown : ColorUtils.colorTown) + town.getName() + ":" + ColorUtils.colorComma +
                    " { " + Rank.Type.MAYOR.color + "Mayor: " + mayorName + ColorUtils.colorComma + " }";
            if(formattedList == null) {
                formattedList = toAdd;
            } else {
                formattedList += "\\n" + toAdd;
            }
        }
        return formattedList;
    }
}
