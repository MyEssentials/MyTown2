package mytown.entities;

import java.util.ArrayList;
import java.util.List;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.Localization;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.util.Point;

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
	
	private Point lastChunkVisited;
	private int lastDimensionVisited;
	
	private List<Town> invitationForms = null; 
	private EntityPlayer player = null;

	/**
	 * Creates a Player with the given name
	 * 
	 * @param name
	 */
	public Resident(String name) {
		playerUUID = name;
		invitationForms = new ArrayList<Town>();
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
		
		if(player != null)
		{
			lastChunkVisited = new Point(player.chunkCoordX, player.chunkCoordZ);
			lastDimensionVisited = player.dimension;
		}
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
	public void sendLocalizedMessage(Localization local, String msg, Object... args) {
		if (!isOnline() || getPlayer() == null) return;
		ChatUtils.sendLocalizedChat(getPlayer(), local, msg, args);
	}
	
	public void setMapOn(boolean on) {
		mapOn = on;
	}

	public boolean isMapOn() {
		return mapOn;
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
	
	public void checkLocation(int oldChunkX, int oldChunkZ, int newChunkX, int newChunkZ)
	{
		if(oldChunkX != newChunkX || oldChunkZ != newChunkZ && player != null)
		{
			int dim = player.dimension;
			
			TownBlock tb1 = MyTown.instance.datasource.getTownBlock(dim, oldChunkX, oldChunkZ);
			TownBlock tb2 = MyTown.instance.datasource.getTownBlock(dim, newChunkX, newChunkZ);
			
			if(tb1 == null && tb2 != null || tb1 != null && tb2 != null && !tb1.getTown().getName().equals(tb2.getTown().getName()))
				sendLocalizedMessage(MyTown.instance.local, "mytown.notification.enter.town", tb2.getTown().getName());
			else if(tb1 != null && tb2 == null)
				sendLocalizedMessage(MyTown.instance.local, "mytown.notification.enter.wild");
			
			System.out.println("From position: " + oldChunkX + ", " + oldChunkZ + " to position " + newChunkX + ", " + newChunkZ);
		}
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
	/**
	 * Removes resident from town. Called when resident is removed from a town.
	 * 
	 * @param town
	 * @return
	 */
	public boolean removeResidentFromTown(Town town)
	{
		if(towns.contains(town))
			return towns.remove(town);
		return false;
	}
	/**
	 * Sets the primary town of this resident.
	 * 
	 * @param town
	 * @return False if the resident isn't part of the town given. True if process succeeded.
	 */
	public boolean setSelectedTown(Town town)
	{
		if(!towns.contains(town))
			return false;
		this.selectedTown = town;
		return true;
	}
	
	/**
	 * Confirms a form that has been sent to the player
	 * 
	 * @param accepted
	 * @param townName
	 */
	public void confirmForm(boolean accepted, String townName)
	{
		if(invitationForms.size() != 0)
		{
			if(accepted)
			{
				try {
					MyTown.instance.datasource.linkResidentToTown(this, getTownFromInvitations(townName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.invitationForms.remove(getTownFromInvitations(townName));
		}
	}
	
	/**
	 * Returns town from a string. Returns null if no town was found with the specified name.
	 * 
	 * @param name
	 * @return
	 */
	protected Town getTownFromInvitations(String name)
	{
		for(Town t : invitationForms)
			if(t.getName().equals(name))
				return t;
		return null;
	}
	
	/**
	 * Gets the towns that this player has been invited in.
	 * 
	 * @return
	 */
	public List<Town> getInvitations()
	{
		return this.invitationForms;
	}
	
	/**
	 * Gets last chunk visited by resident.
	 * 
	 * @return
	 */
	public Point getLastChunkVisited()
	{
		return this.lastChunkVisited;
	}
	
	public void updateLocation()
	{

	}
	
	
}