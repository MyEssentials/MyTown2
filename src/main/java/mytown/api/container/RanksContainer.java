package mytown.api.container;

import mytown.entities.Rank;
import myessentials.utils.ColorUtils;

import java.util.ArrayList;

public class RanksContainer extends ArrayList<Rank> {

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

    public Rank get(Rank.Type type) {
        if(!type.unique) {
            throw new RuntimeException("The rank you are trying to get is not unique!");
        }

        for(Rank rank : this) {
            if(rank.getType() == type) {
                return rank;
            }
        }
        return null;
    }

    public Rank getMayorRank() {
        for(Rank rank : this) {
            if(rank.getType() == Rank.Type.MAYOR) {
                return rank;
            }
        }
        return null;
    }

    public Rank getDefaultRank() {
        for(Rank rank : this) {
            if(rank.getType() == Rank.Type.DEFAULT) {
                return rank;
            }
        }
        return null;
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
