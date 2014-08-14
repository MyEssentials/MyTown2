package mytown.entities.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Rank;

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
     * Returns the Collection of Ranks
     *
     * @return
     */
    public ImmutableList<Rank> getRanks();
}
