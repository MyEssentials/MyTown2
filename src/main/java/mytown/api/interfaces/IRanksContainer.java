package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Rank;
import mytown.entities.Resident;

/**
 * Represents an object that can hold Ranks
 */
public interface IRanksContainer {

    void addRank(Rank rank);

    void removeRank(Rank rank);

    boolean hasRank(Rank rank);

    boolean hasRankName(String rankName);

    Rank getRank(String rankName);

    void setDefaultRank(Rank rank);

    Rank getDefaultRank();

    boolean promoteResident(Resident res, Rank rank);

    ImmutableList<Rank> getRanks();
}
