package mytown.entities;

import java.util.ArrayList;
import java.util.List;

import mytown.core.ChatUtils;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Defines a player
 * 
 * @author Joe Goett
 */
public class Resident {
	public enum Rank {
		Outsider, Resident, Assistant, Mayor;

		/**
		 * Gets the rank based on [O, R, A, M]
		 */
		public static Rank parse(String rank) {
			for (Rank type : values()) {
				if (type.toString().toLowerCase().startsWith(rank.toLowerCase())) {
					return type;
				}
			}
			return Rank.Outsider;
		}

		@Override
		public String toString() {
			return super.toString().substring(0, 2);
		}
	}

	private String playerUUID;
	private boolean isOnline = false;
	private boolean isNPC = false;
	private EntityPlayer player = null;

	/**
	 * Creates a Player with the given name
	 * 
	 * @param name
	 */
	public Resident(String name) {
		playerUUID = name;
	}

	/**
	 * Returns the UUID (Name atm) of the player
	 * 
	 * @return
	 */
	public String getUUID() {
		return playerUUID;
	}

	/**
	 * Returns if the player is online or not
	 * 
	 * @return
	 */
	public boolean isOnline() {
		return isOnline;
	}

	/**
	 * Sets the online status of the player
	 * 
	 * @param online
	 */
	public void setOnline(boolean online) {
		isOnline = online;
	}

	/**
	 * Makes this Player an NPC
	 */
	public void setNPC() {
		isNPC = true;
	}

	/**
	 * Returns if this Player is an NPC
	 * 
	 * @return
	 */
	public boolean isNPC() {
		return isNPC;
	}

	/**
	 * Returns the EntityPlayer, or null if offline
	 * 
	 * @return
	 */
	public EntityPlayer getPlayer() {
		return player;
	}

	/**
	 * Sets the EntityPlayer
	 * 
	 * @param player
	 */
	public void setPlayer(EntityPlayer player) {
		this.player = player;
	}

	/**
	 * Helper to send chat message to Resident
	 * 
	 * @param msg
	 * @param args
	 */
	public void sendMessage(String msg, Object... args) {
		if (!isOnline() || getPlayer() == null)
			return;
		ChatUtils.sendChat(getPlayer(), msg, args);
	}

	// //////////////////////////////////////
	// Towns
	// //////////////////////////////////////
	private List<Town> towns = new ArrayList<Town>();

	/**
	 * Adds a Town
	 * 
	 * @param town
	 */
	public void addTown(Town town) {
		towns.add(town);
	}

	/**
	 * Checks if this Resident is part of the Town
	 * 
	 * @param town
	 * @return
	 */
	public boolean isPartOfTown(Town town) {
		return towns.contains(town);
	}

	/**
	 * Returns a Collection of Towns this Resident is part of
	 * 
	 * @return
	 */
	public List<Town> getTowns() {
		return towns;
	}

	/**
	 * Returns the Rank of the Resident at the given town
	 * 
	 * @param town
	 * @return
	 */
	public Rank getTownRank(Town town) {
		return town.getResidentRank(this);
	}

	/**
	 * Sets the Rank of this Resident in the Town
	 * 
	 * @param town
	 * @param rank
	 */
	public void setTownRank(Town town, Rank rank) {
		if (!isPartOfTown(town))
			return; // TODO Log/Throw Exception?
		town.promoteResident(this, rank);
	}
}