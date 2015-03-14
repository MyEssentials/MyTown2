package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.Resident;

/**
 * @author Joe Goett
 * Represents an object that can hold Residents
 */
public interface IHasResidents {

    public void addResident(Resident res);

    public void removeResident(Resident res);

    public boolean hasResident(Resident res);

    public ImmutableList<Resident> getResidents();
}
