package mytown.api.container;

import com.google.common.collect.ImmutableList;
import mytown.entities.Rank;
import mytown.entities.Resident;

import java.util.ArrayList;

public class RanksContainer {

    private ArrayList<Rank> ranks = new ArrayList<Rank>();
    private Rank defaultRank;

    public RanksContainer(Rank defaultRank) {
        this.defaultRank = defaultRank;
    }

    public void addRank(Rank rank) {
        ranks.add(rank);
    }

    public void removeRank(Rank rank) {
        ranks.remove(rank);
    }

    public boolean hasRank(Rank rank) {
        return ranks.contains(rank);
    }

    public boolean hasRank(String rankName) {
        for (Rank r : ranks) {
            if (r.getName().equals(rankName))
                return true;
        }
        return false;
    }

    public Rank getRank(String rankName) {
        for (Rank r : ranks) {
            if (r.getName().equals(rankName))
                return r;
        }
        return null;
    }

    /*
    public boolean promoteResident(Resident res, Rank rank) {
        if (hasResident(res) && hasRank(rank)) {
            residents.remove(res);
            residents.put(res, rank);
            return true;
        }
        return false;
    }
    */

    public void setDefaultRank(Rank rank) {
        defaultRank = rank;
    }

    public Rank getDefaultRank() {
        return defaultRank;
    }

    public ImmutableList<Rank> getRanks() {
        return ImmutableList.copyOf(ranks);
    }
}
