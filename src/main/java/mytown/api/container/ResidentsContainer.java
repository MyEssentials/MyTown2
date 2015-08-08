package mytown.api.container;

import myessentials.entities.Container;
import mytown.entities.Plot;
import mytown.entities.Resident;

import java.util.Iterator;

public class ResidentsContainer extends Container<Resident> {

    @Override
    public void remove(Resident res) {
        /*
        for (Iterator<Plot> it = res.getCurrentTown().plotsContainer.asList().iterator(); it.hasNext(); ) {
            Plot plot = it.next();
            if (plot.ownersContainer.contains(res) && plot.ownersContainer.size() <= 1) {
                it.remove();
            }
        }
        */
        items.remove(res);
    }

    public boolean contains(String username) {
        for (Resident res : items) {
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
