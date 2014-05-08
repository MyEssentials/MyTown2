package mytown.entities;

import java.util.List;

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
	 * @param town null only if it's a default rank
	 */
	public Rank(String name, List<String> permissions, Town town) {
		this.name = name;
		this.permissions = permissions;
		this.town = town;
		updateKey();
	}

	/**
	 * Gets the name of the Rank
	 * @return
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Gets all the permission of the Rank
	 * @return
	 */
	public List<String> getPermissions()
	{
		return this.permissions;
	}
	
	/**
	 * Gets all permissions in one long String with format "permission permission perm..."
	 * Used for saving into Database
	 * 
	 * @return
	 */
	
	public String getPermissionsWithFormat()
	{
		String temp = null;
		for(String s : permissions)
			if(temp == null)
				temp = s;
			else
				temp += " " + s;
		return temp;
	}
	
	/**
	 * 
	 * Gets the key of this rank 
	 * 
	 * @return
	 */
	public String getKey()
	{
		return this.key;
	}
	
	public boolean updateKey()
	{
		if(key != null && key == this.getTown().getName() + ":" + this.getName())
			return false;
		if(town == null)
			this.key = this.getName();
		else
			this.key = this.getTown().getName() + ":" + this.getName();
		return true;
	}
	
	/**
	 * Gets the Town in which this Rank is found
	 * 
	 * @return
	 */
	public Town getTown()
	{
		return this.town;
	}
	
	/**
	 * Checks if the given perm String matches any this Rank has
	 * 
	 * @param perm
	 * @return
	 */
	public boolean hasPermission(String perm) {
		if (permissions == null) return false;
		for (String p : permissions) {
			if (p == perm) return true;
		}
		return false;
	}
}