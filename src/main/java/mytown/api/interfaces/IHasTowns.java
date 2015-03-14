package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Town;

/**
 * @author Joe Goett
 * Represents an object that can hold Towns
 */
public interface IHasTowns {

    public void addTown(Town town);

    public void removeTown(Town town);

    public boolean hasTown(Town town);

    public ImmutableList<Town> getTowns();
}
