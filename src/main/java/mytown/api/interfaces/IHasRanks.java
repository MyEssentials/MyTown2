package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Rank;
import mytown.entities.Resident;

/**
 * Created by joe on 7/18/14.
 * Represents an object that can hold Ranks
 */
public interface IHasRanks {

    public void addRank(Rank rank);

    public void removeRank(Rank rank);

    public boolean hasRank(Rank rank);

    public boolean hasRankName(String rankName);

    public Rank getRank(String rankName);

    public void setDefaultRank(Rank rank);

    public Rank getDefaultRank();

    public boolean promoteResident(Resident res, Rank rank);

    public ImmutableList<Rank> getRanks();
}
