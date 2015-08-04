package mytown.api.container;

import com.google.common.collect.ImmutableList;
import mytown.entities.Resident;

import java.util.ArrayList;

public class ResidentContainer {

    private ArrayList<Resident> residents = new ArrayList<Resident>();

    public void addResident(Resident res) {
        residents.add(res);
    }

    /*
    public void removeResident(Resident res) {
        for (Iterator<Plot> it = plots.iterator(); it.hasNext(); ) {
            Plot plot = it.next();
            if (plot.hasOwner(res) && plot.getOwners().size() <= 1) {
                it.remove();
            }
        }
        residents.remove(res);
    }
    */


    public boolean hasResident(Resident res) {
        return residents.contains(res);
    }

    public boolean hasResident(String username) {
        for (Resident res : residents) {
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

    public ImmutableList<Resident> getResidents() {
        return ImmutableList.copyOf(residents);
    }
}
