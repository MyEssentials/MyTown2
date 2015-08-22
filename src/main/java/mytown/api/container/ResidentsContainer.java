package mytown.api.container;

import mytown.entities.Resident;
import myessentials.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class ResidentsContainer extends ArrayList<Resident> {

    public Resident get(UUID uuid) {
        for (Resident res : this) {
            if (res.getUUID().equals(uuid)) {
                return res;
            }
        }
        return null;
    }

    public Resident get(String username) {
        for (Resident res : this) {
            if (res.getPlayerName().equals(username)) {
                return res;
            }
        }
        return null;
    }

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

    public void remove(UUID uuid) {
        for(Iterator<Resident> it = iterator(); it.hasNext();) {
            Resident res = it.next();
            if(res.getUUID().equals(uuid)) {
                it.remove();
            }
        }
    }

    public boolean contains(String username) {
        for (Resident res : this) {
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(UUID uuid) {
        for (Resident res : this) {
            if (res.getUUID().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String formattedList = null;

        for (Resident res : this) {
            String toAdd = ColorUtils.colorPlayer + res.getPlayerName();
            if (formattedList == null) {
                formattedList = toAdd;
            } else {
                formattedList += ColorUtils.colorComma + ", " + toAdd;
            }
        }
        if (isEmpty()) {
            formattedList = ColorUtils.colorEmpty + "NONE";
        }
        return formattedList;
    }
}
