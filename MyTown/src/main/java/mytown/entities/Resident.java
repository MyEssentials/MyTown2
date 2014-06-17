package mytown.entities;

import java.util.ArrayList;
import java.util.List;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.Localization;
import mytown.entities.town.AdminTown;
import mytown.entities.town.Town;
import mytown.interfaces.IPlotSelector;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet53BlockChange;

/**
 * Defines a player
 * 
 * @author Joe Goett
 */
public class Resident implements IPlotSelector {
	private String playerUUID;
	private boolean isOnline = false;
	private boolean isNPC = false;
	private boolean mapOn = false;
	private List<Town> invitationForms = null; 
	private EntityPlayer player = null;
	private int lastChunkZ, lastChunkX;
	private int lastDim;
	
	private int selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2, selectionDim;
	private Town selectionTown;
	private boolean firstSelectionActive = false, secondSelectionActive = false;

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

		if (player != null) {
			lastChunkZ = player.chunkCoordX;
			lastChunkZ = player.chunkCoordZ;
			lastDim = player.dimension;
		}
	}

	/**
	 * Helper to send a message to Resident
	 * 
	 * @param msg
	 * @param args
	 */
	public void sendMessage(String msg, Object... args) {
		if (!isOnline() || getPlayer() == null)
			return;
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
		if (!isOnline() || getPlayer() == null)
			return;
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
		if (!isOnline() || getPlayer() == null)
			return;
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
				TownBlock b = DatasourceProxy.getDatasource().getTownBlock(dim, x, z, true);

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
	
	
	public void checkLocation(int oldChunkX, int oldChunkZ, int newChunkX, int newChunkZ, int dimension) {
		if (oldChunkX != newChunkX || oldChunkZ != newChunkZ && player != null) {
			TownBlock oldTownBlock, newTownBlock;
			
			oldTownBlock = MyTown.getDatasource().getTownBlock(lastDim, oldChunkX, oldChunkZ, true);
			newTownBlock = MyTown.getDatasource().getTownBlock(player.dimension, newChunkX, newChunkZ, true);

			if (oldTownBlock == null && newTownBlock != null || oldTownBlock != null && newTownBlock != null && !oldTownBlock.getTown().getName().equals(newTownBlock.getTown().getName())) {
				if(this.isPartOfTown(newTownBlock.getTown()))
					sendLocalizedMessage(MyTown.getLocal(), "mytown.notification.enter.ownTown", newTownBlock.getTown().getName());
				else
					sendLocalizedMessage(MyTown.getLocal(), "mytown.notification.enter.town", newTownBlock.getTown().getName());
			} else if (oldTownBlock != null && newTownBlock == null) {
				sendLocalizedMessage(MyTown.getLocal(), "mytown.notification.enter.wild");
			}

			lastDim = player.dimension;
			this.lastChunkX = newChunkX;
			this.lastChunkZ = newChunkZ;
		}
	}

	public void checkLocation(int newChunkX, int newChunkZ, int dimension) {
		checkLocation(this.lastChunkX, this.lastChunkZ, newChunkX, newChunkZ, dimension);
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
		if (!isPartOfTown(town))
			return; // TODO Log/Throw Exception?
		town.promoteResident(this, rank);
	}

	/**
	 * Returns the currently selected town, the first town, or null
	 * 
	 * @return
	 */
	public Town getSelectedTown() {
		if (selectedTown == null) {
			if (towns.isEmpty())
				return null;
			else
				return towns.get(0);
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
	public boolean removeResidentFromTown(Town town) {
		if (towns.contains(town))
			return towns.remove(town);
		return false;
	}

	/**
	 * Sets the primary town of this resident.
	 * 
	 * @param town
	 * @return False if the resident isn't part of the town given. True if process succeeded.
	 */
	public boolean setSelectedTown(Town town) {
		if (!towns.contains(town))
			return false;
		selectedTown = town;
		return true;
	}

	/**
	 * Confirms a form that has been sent to the player
	 * 
	 * @param accepted
	 * @param townName
	 */
	public void confirmForm(boolean accepted, String townName) {
		if (invitationForms.size() != 0) {
			Town town = getTownFromInvitations(townName);
			if(town == null) return;
			if (accepted) {
				try {
					
					sendLocalizedMessage(LocalizationProxy.getLocalization(), "mytown.notification.town.invited.accept", town.getName());
					for(Resident res : town.getResidents())
						res.sendLocalizedMessage(LocalizationProxy.getLocalization(), "mytown.notification.town.joined", this.getUUID(), town.getName());
					DatasourceProxy.getDatasource().linkResidentToTown(this, town, town.getRank("Resident"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sendLocalizedMessage(LocalizationProxy.getLocalization(), "mytown.notification.town.invited.refuse", town.getName());
			}
			invitationForms.remove(getTownFromInvitations(townName));
		}
	}

	/**
	 * Returns town from a string. Returns null if no town was found with the specified name.
	 * 
	 * @param name
	 * @return
	 */
	protected Town getTownFromInvitations(String name) {
		for (Town t : invitationForms)
			if (t.getName().equals(name))
				return t;
		return null;
	}

	/**
	 * Gets the towns that this player has been invited in.
	 * 
	 * @return
	 */
	public List<Town> getInvitations() {
		return invitationForms;
	}
	
	/**
	 * Adds an invitation to the resident
	 * 
	 * @param town
	 * @return
	 */
	public boolean addInvitation(Town town) {
		return this.invitationForms.add(town);
	}
	
	
	
	////////////////////////////////////////
	// PLOT SELECTION
	////////////////////////////////////////
	
	// Mostly a workaround, might be changed
	
	@Override
	public boolean selectBlockForPlot (int dim, int x, int y, int z) {
		TownBlock tb = DatasourceProxy.getDatasource().getTownBlock(dim, x, z, false);
		if(firstSelectionActive && this.selectionDim != dim) return false;
		if(tb == null || tb.getTown() != getSelectedTown() && !firstSelectionActive || tb.getTown() != selectionTown && firstSelectionActive) return false;
		if(!firstSelectionActive) {
			this.secondSelectionActive = false;
			this.selectionDim = dim;
			this.selectionX1 = x;
			this.selectionY1 = y;
			this.selectionZ1 = z;
			this.selectionTown = tb.getTown();
			this.firstSelectionActive = true;
			
			Packet53BlockChange packet = new Packet53BlockChange(x, y, z, player.worldObj);
			
			packet.type = Block.blockRedstone.blockID;
			
			((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(packet);
			
		} else {
			this.selectionX2 = x;
			this.selectionY2 = y;
			this.selectionZ2 = z;
			this.secondSelectionActive = true;
		}
		return true;
	}
	
	@Override
	public boolean isFirstPlotSelectionActive() {
		return this.firstSelectionActive;
	}
	
	@Override
	public boolean isSecondPlotSelectionActive() {
		return this.secondSelectionActive;
	}
	
	@Override
	public boolean makePlotFromSelection() {
		
		// TODO: Check everything separately or throw exceptions?
		
		if(!secondSelectionActive || !firstSelectionActive || (Math.abs(selectionX1 - selectionX2) < TownPlot.minX || Math.abs(selectionY1 - selectionY2) < TownPlot.minY || Math.abs(selectionZ1 - selectionZ2) < TownPlot.minZ) && !(selectedTown instanceof AdminTown)) {
			System.out.println("In calculations");
			resetSelection();
			return false;
		}
		
		int x1 = selectionX1, x2 = selectionX2, y1 = selectionY1, y2 = selectionY2, z1 = selectionZ1, z2 = selectionZ2;
		
		if(x2 < x1) {
			int aux = x1;
			x1 = x2;
			x2 = aux;
		}
		if(y2 < y1) {
			int aux = y1;
			y1 = y2;
			y2 = aux;
		}
		if(z2 < z1) {
			int aux = z1;
			z1 = z2;
			z2 = aux;
		}
		
		int lastX = 1000000, lastZ = 1000000;
		for(int i = x1; i <= x2; i++) {
			for(int j = z1; j <= z2; j++) {
				if(i >> 4 != lastX || j >> 4 != lastZ) {
					lastX = i >> 4;
					lastZ = j >> 4;
					if(!DatasourceProxy.getDatasource().hasTownBlock(selectionDim, lastX, lastZ, true, selectionTown)) {
						resetSelection();
						return false;
					}
				}
				
				for(int k = y1; k <= y2; k++) {
					if(selectionTown.getPlotAtCoords(i, k, j) != null) {
						resetSelection();
						return false;
					}
				}
			}
		}
		
		TownPlot plot = new TownPlot(selectionDim, selectionX1, selectionY1, selectionZ1, selectionX2, selectionY2, selectionZ2, selectionTown, this);
		try {
			DatasourceProxy.getDatasource().insertPlot(plot);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		resetSelection();
		return true;
	}
	
	@Override
	public void expandSelectionVert() {
		this.selectionY1 = 0;
		this.selectionY2 = player.worldObj.getActualHeight();
	}
	
	@Override
	public void resetSelection() {
		this.firstSelectionActive = false;
		this.secondSelectionActive = false;
	}
	
	
}