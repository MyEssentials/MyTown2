package mytown.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a player
 * @author Joe Goett
 */
public class Resident extends TownBlockOwner{
	public enum Rank{
		Outsider, Resident, Assistant, Mayor;

		/**
		 * Gets the rank based on [R, A, M]
		 */
		public static Rank parse(String rank) {
			for (Rank type : values()) {
				if (type.toString().toLowerCase().startsWith(rank.toLowerCase())) {
					return type;
				}
			}
			return Rank.Resident;
		}
	}
	
	private int id;
	private String name;
	private boolean isOnline = false;
	private boolean isNPC = false;
	private Map<Town, Rank> towns = new HashMap<Town, Rank>();

	/**
	 * Creates a Player with the given name
	 * @param name
	 */
	public Resident(String name){
		this.name = name;
	}
	
	/**
	 * Used internally only!
	 * @param id
	 * @param name
	 */
	public Resident(int id, String name){
		this(name);
		this.id = id;
	}
	
	/**
	 * Returns the id of the player
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the name of the player
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns if the player is online or not
	 * @return
	 */
	public boolean isOnline(){
		return isOnline;
	}

	/**
	 * Sets the online status of the player
	 * @param online
	 */
	public void setOnline(boolean online){
		isOnline = online;
	}

	/**
	 * Makes this Player an NPC
	 */
	public void setNPC(){
		isNPC = true;
	}
	
	/**
	 * Returns if this Player is an NPC
	 * @return
	 */
	public boolean isNPC(){
		return isNPC;
	}

	/**
	 * Returns a Collection of Towns this Resident is part of
	 * @return
	 */
	public Collection<Town> getTowns(){
		return towns.keySet();
	}
	
	/**
	 * Returns a Map of all Towns and the players rank in them
	 * @return
	 */
	public Map<Town, Rank> getTownsAndRanks(){
		return towns;
	}
	
	/**
	 * Returns the Rank of the Resident at the given town
	 * @param town
	 * @return
	 */
	public Rank getRank(Town town){
		if (!towns.containsKey(town)) return Rank.Outsider;  // Resident is an outsider!
		return towns.get(town);
	}
}