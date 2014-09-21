package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Rank;
import mytown.entities.Resident;

/**
 * Created by joe on 7/18/14.
 */
public interface IHasRanks {
    /**
     * Adds the Rank to this entity
     *
     * @param rank
     */
    public void addRank(Rank rank);

    /**
     * Removes the Rank from this entity
     *
     * @param rank
     */
    public void removeRank(Rank rank);

    /**
     * Checks if this entity has the Rank
     *
     * @param rank
     * @return
     */
    public boolean hasRank(Rank rank);

    /**
     * Checks if the rank with the name specified exists
     *
     * @param rankName
     * @return
     */
    public boolean hasRankName(String rankName);

    /**
     * Gets the rank with the name specified
     *
     * @param rankName
     * @return
     */
    public Rank getRank(String rankName);

    /**
     * Sets the default Rank for this entity
     *
     * @param rank
     */
    public void setDefaultRank(Rank rank);

    /**
     * Returns the default Rank for this entity
     *
     * @return
     */
    public Rank getDefaultRank();

    /**
     * Promotes the resident to the rank specified
     *
     * @param res
     * @param rank
     * @return
     */
    public boolean promoteResident(Resident res, Rank rank);

    /**
     * Returns the Collection of Ranks
     *
     * @return
     */
    public ImmutableList<Rank> getRanks();
}
