package mytown.entities.interfaces;

import mytown.entities.Resident;

import java.util.Collection;

/**
 * @author Joe Goett
 */
public interface IHasResidents {
    /**
     * Adds the Resident to this entity
     * @param res
     */
    public void addResident(Resident res);

    /**
     * Removes the Resident from this entity
     * @param res
     */
    public void removeResident(Resident res);

    /**
     * Checks if this entity has the Resident
     * @param res
     * @return
     */
    public boolean hasResident(Resident res);

    /**
     * Returns the Collection of Residents
     * @return
     */
    public Collection<Resident> getResidents();
}
