package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Resident;

/**
 * Represents an object that can hold Residents
 */
public interface IHasResidents {

    void addResident(Resident res);

    void removeResident(Resident res);

    boolean hasResident(Resident res);

    ImmutableList<Resident> getResidents();
}
