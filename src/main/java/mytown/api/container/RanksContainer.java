package mytown.api.container;

import myessentials.entities.Container;
import mytown.entities.Rank;

public class RanksContainer extends Container<Rank> {

    private Rank defaultRank;

    public boolean contains(String rankName) {
        for (Rank r : items) {
            if (r.getName().equals(rankName))
                return true;
        }
        return false;
    }

    public Rank get(String rankName) {
        for (Rank r : items) {
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
}
