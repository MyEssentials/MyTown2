package mytown.api.container;

import mytown.entities.Plot;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.util.ColorUtils;

import java.util.HashMap;
import java.util.List;

public class ResidentRankMap extends HashMap<Resident, Rank> {

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
        for (Resident res : keySet()) {
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String formattedList = null;

        for (Entry<Resident, Rank> entry : entrySet()) {
            String toAdd = ColorUtils.colorPlayer + entry.getKey().getPlayerName() + ColorUtils.colorComma + " (" + entry.getValue().toString() + ColorUtils.colorComma + ")";
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
