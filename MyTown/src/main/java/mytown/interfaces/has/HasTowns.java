package mytown.interfaces.has;

import java.util.ArrayList;
import java.util.List;

import mytown.entities.town.Town;

public abstract class HasTowns {
	protected List<Town> towns = new ArrayList<Town>();
	
	/**
	 * Adds the {@link Town}
	 * @param town
	 */
	public void addTown(Town town) {
		towns.add(town);
	}
	
	/**
	 * Removes the {@link Town}
	 * @param town
	 */
	public boolean removeTown(Town town) {
		return towns.remove(town);
	}

	/**
	 * Returns the list of {@link Town}s
	 * @return
	 */
	public List<Town> getTowns() {
		return towns;
	}
	
	/**
	 * Returns whether this entity is part of the given {@link Town}
	 */
	public boolean isPartOfTown(Town town) {
		return towns.contains(town);
	}
}