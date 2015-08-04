package mytown.api.container;

import myessentials.entities.Container;
import mytown.entities.Resident;

public class ResidentsContainer extends Container<Resident> {

    /*
    public void remove(Resident res) {
        for (Iterator<Plot> it = plots.iterator(); it.hasNext(); ) {
            Plot plot = it.next();
            if (plot.hasOwner(res) && plot.getOwners().size() <= 1) {
                it.remove();
            }
        }
        residents.remove(res);
    }
    */


    public boolean contains(String username) {
        for (Resident res : items) {
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
    }


    /*
    public Rank getResidentRank(Resident res) {
        return residents.get(res);
    }

    public void setResidentRank(Resident res, Rank rank) {
        if (residents.containsKey(res)) {
            residents.put(res, rank);
        }
    }
    */
}
