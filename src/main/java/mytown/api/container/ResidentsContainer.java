package mytown.api.container;

import mytown.entities.Resident;

import java.util.ArrayList;

public class ResidentsContainer extends ArrayList<Resident> {

    public void remove(Resident res) {
        /*
        for (Iterator<Plot> it = res.getCurrentTown().plotsContainer.asList().iterator(); it.hasNext(); ) {
            Plot plot = it.next();
            if (plot.ownersContainer.contains(res) && plot.ownersContainer.size() <= 1) {
                it.remove();
            }
        }
        */
        super.remove(res);
    }

    public boolean contains(String username) {
        for (Resident res : this) {
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
