package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Town;

/**
 * Represents an object that can hold Towns
 */
public interface IHasTowns {

    void addTown(Town town);

    void removeTown(Town town);

    boolean hasTown(Town town);

    ImmutableList<Town> getTowns();
}
