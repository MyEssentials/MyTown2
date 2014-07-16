package mytown.entities;

import java.util.List;

import com.google.common.base.Joiner;

import mytown.entities.town.Town;
import net.minecraft.util.EnumChatFormatting;

public class Rank {

	private String key;
	private String name;
	private List<String> permissions; // Lists > Arrays
	private Town town;

	/**
	 * Constructor for Rank
	 * 
	 * @param name
	 * @param permissions
	 * @param town
	 *            null only if it's a default rank
	 */
	public Rank(String name, List<String> permissions, Town town) {
		this.name = name;
		this.permissions = permissions;
		this.town = town;
		updateKey();
	}

	public boolean addPermission(String perm) {
		if (hasPermission(perm))
			return false;
		return permissions.add(perm);
	}

	public boolean removePermission(String perm) {
		return permissions.remove(perm);
	}

	/**
	 * Gets the name of the Rank
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets all the permission of the Rank
	 * 
	 * @return
	 */
	public List<String> getPermissions() {
		return permissions;
	}

	/**
	 * Gets all permissions in one long String with format "permission permission perm..." Used for saving into Database
	 * 
	 * @return
	 */
	public String getPermissionsWithFormat() {
		return Joiner.on(" ").join(permissions);
	}

	/**
	 * 
	 * Gets the key of this rank
	 * 
	 * @return
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Updates the key. Only called in the constructor and when updating in the database. DO NOT CALL ELSEWHERE!
	 * 
	 * @return
	 */
	public void updateKey() {
		key = String.format("%s;%s", town.getName(), name);
	}

	/**
	 * Gets the Town in which this Rank is found
	 * 
	 * @return
	 */
	public Town getTown() {
		return town;
	}

	/**
	 * Checks if the given perm String matches any this Rank has
	 * 
	 * @param perm
	 * @return
	 */
	public boolean hasPermission(String perm) {
		if (permissions == null)
			return false;
		return permissions.contains(perm);
	}

	@Override
	public String toString() {
		return EnumChatFormatting.GREEN + name;
	}
}