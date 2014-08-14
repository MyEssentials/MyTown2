package mytown.entities.interfaces;

import com.google.common.collect.ImmutableCollection;
import mytown.entities.Town;

import java.util.Collection;

/**
 * @author Joe Goett
 */
public interface IHasTowns {
    /**
     * Adds the Town to this entity
     *
     * @param town
     */
    public void addTown(Town town);

    /**
     * Removes the Town from this entity
     *
     * @param town
     */
    public void removeTown(Town town);

    /**
     * Checks if this entity has this Town
     *
     * @param town
     * @return
     */
    public boolean hasTown(Town town);

    /**
     * Returns the Collection of Towns
     *
     * @return
     */
    public ImmutableCollection<Town> getTowns();
}
