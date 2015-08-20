package mytown.api.container;

import mytown.entities.Rank;
import myessentials.utils.ColorUtils;

import java.util.ArrayList;

public class RanksContainer extends ArrayList<Rank> {

    private Rank defaultRank;

    public boolean contains(String rankName) {
        for (Rank r : this) {
            if (r.getName().equals(rankName))
                return true;
        }
        return false;
    }

    public Rank get(String rankName) {
        for (Rank r : this) {
            if (r.getName().equals(rankName))
                return r;
        }
        return null;
    }

    public void setDefaultRank(Rank rank) {
        defaultRank = rank;
    }

    public Rank getDefaultRank() {
        return defaultRank;
    }

    @Override
    public String toString() {
        String res = null;
        for (Rank rank : this) {
            if (res == null) {
                res = rank.toString();
            } else {
                res += ColorUtils.colorComma + ", " + rank.toString();
            }
        }

        if (isEmpty()) {
            res = ColorUtils.colorEmpty + "NONE";
        }
        return res;
    }
}
