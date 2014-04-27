package mytown.entities;

import java.util.ArrayList;
import java.util.List;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.Localization;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Defines a player
 * 
 * @author Joe Goett
 */
public class Resident {

	private String playerUUID;
	private boolean isOnline = false;
	private boolean isNPC = false;
	private boolean mapOn = false;
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
	 * Helper to send a message to Resident
	 * 
	 * @param msg
	 * @param args
	 */
	public void sendMessage(String msg, Object... args) {
		if (!isOnline() || getPlayer() == null) return;
		ChatUtils.sendChat(getPlayer(), msg, args);
	}

	/**
	 * Helper to send a localized message to Resident
	 * 
	 * @param msg
	 * @param local
	 * @param args
	 */
	public void sendLocalizedMessage(String msg, Localization local, Object... args) {
		if (!isOnline() || getPlayer() == null) return;
		ChatUtils.sendLocalizedChat(getPlayer(), local, msg, args);
	}

	/**
	 * Send a "map" of the Blocks directly around the player
	 */
	public void sendMap() {
		if (!isOnline() || getPlayer() == null) return;
		sendMap(getPlayer().dimension, getPlayer().chunkCoordX, getPlayer().chunkCoordZ);
	}

	/**
	 * Sends a "map" of the Blocks around cx, cz in dim
	 * 
	 * @param dim
	 * @param cx
	 * @param cz
	 */
	public void sendMap(int dim, int cx, int cz) {
		int heightRad = 4;
		int widthRad = 9;
		StringBuilder sb = new StringBuilder();
		String c;

		sb.append("---------- Town Map ----------");
		sb.setLength(0);
		for (int z = cz - heightRad; z <= cz + heightRad; z++) {
			sb.setLength(0);
			for (int x = cx - widthRad; x <= cx + widthRad; x++) {
				TownBlock b = MyTown.instance.datasource.getTownBlock(dim, x, z);

				boolean mid = z == cz && x == cx;
				boolean isTown = b != null && b.getTown() != null;
				boolean ownTown = isTown && isPartOfTown(b.getTown());

				if (mid) {
					c = ownTown ? "§a" : isTown ? "§c" : "§f";
				} else {
					c = ownTown ? "§2" : isTown ? "§4" : "§7";
				}

				c += isTown ? "O" : "_";

				sb.append(c);
			}
		}
		sendMessage(sb.toString());
	}

	public void setMapOn(boolean on) {
		mapOn = on;
	}

	public boolean isMapOn() {
		return mapOn;
	}

	// //////////////////////////////////////
	// Towns
	// //////////////////////////////////////
	private List<Town> towns = new ArrayList<Town>();
	private Town selectedTown = null;

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
		if (!isPartOfTown(town)) return; // TODO Log/Throw Exception?
		town.promoteResident(this, rank);
	}

	/**
	 * Returns the currently selected town, the first town, or null
	 * 
	 * @return
	 */
	public Town getSelectedTown() {
		if (selectedTown == null) {
			if (towns.isEmpty()) {
				return null;
			} else {
				return towns.get(0);
			}
		}
		return selectedTown;
	}

	/**
	 * Helper getTownRank(getSelectedTown())
	 * 
	 * @return
	 */
	public Rank getTownRank() {
		return getTownRank(getSelectedTown());
	}

	/**
	 * Helper setTownRank(getSelectedTown(), rank)
	 * 
	 * @param rank
	 */
	public void setTownRank(Rank rank) {
		setTownRank(getSelectedTown(), rank);
	}
}