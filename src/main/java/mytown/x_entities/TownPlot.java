package mytown.x_entities;

import java.util.ArrayList;
import java.util.List;

import mytown.x_entities.town.Town;
import mytown.interfaces.ITownFlag;
import mytown.interfaces.ITownPlot;
import net.minecraft.util.EnumChatFormatting;

public class TownPlot implements ITownPlot {
	// TODO: Read from config
	public static int minX = 5;
	public static int minY = 4;
	public static int minZ = 5;

	protected int x1, z1, y1, x2, z2, y2;
	protected int dim;
	protected String name;

	protected String key;

	protected Town town;
	protected Resident owner;

	protected int chunkX1, chunkZ1, chunkX2, chunkZ2;

	protected List<TownBlock> townBlocks;

	protected List<ITownFlag> plotFlags;

	public TownPlot(int dim, int x1, int y1, int z1, int x2, int y2, int z2, Town town, Resident owner) {
		this(dim, x1, y1, z1, x2, y2, z2, town, owner, "NoName");
	}

	public TownPlot(int dim, int x1, int y1, int z1, int x2, int y2, int z2, Town town, Resident owner, String name) {
		if (x1 > x2) {
			int aux = x2;
			x2 = x1;
			x1 = aux;
		}

		if (z1 > z2) {
			int aux = z2;
			z2 = z1;
			z1 = aux;
		}

		if (y1 > y2) {
			int aux = y2;
			y2 = y1;
			y1 = aux;
		}

		// Second parameter is always highest

		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
		this.dim = dim;

		this.name = name;
		this.town = town;
		this.owner = owner;

		initializeFlags();
		updateKey();
	}

	/**
	 * Gets the unique key for the plot
	 * 
	 * @return
	 */
	@Override
	public String getKey() {
		return key;
	}

	/**
	 * Updates the key. Only called in the constructor and when updating in database. DO NOT CALL ELSEWHERE!
	 */
	@Override
	public void updateKey() {
		key = String.format("%s;%s;%s;%s;%s;%s;%s", dim, x1, y1, z1, x2, y2, z2);
	}

	private void initializeFlags() {
		if (town.getFlags() != null) {
			plotFlags = town.getFlags();
		} else {
			plotFlags = new ArrayList<ITownFlag>();
		}

	}

	// temp
	public void verify() {
		if (x1 > x2) {
			int aux = x2;
			x2 = x1;
			x1 = aux;
		}
		if (z1 > z2) {
			int aux = z2;
			z2 = z1;
			z1 = aux;
		}

		int chunkX = x1 / 16;
		int chunkZ = z1 / 16;

		for (int X = chunkX; X <= x2 / 16; X++) {
			for (int Z = chunkZ; Z <= z2 / 16; Z++) {
				// if(!DatasourceProxy.getDatasource().hasTownBlock(String.format(TownBlock.keyFormat, chunkX, chunkZ, dim)))

			}
		}
	}

	@Override
	public Town getTown() {
		return town;
	}

	@Override
	public Resident getOwner() {
		return owner;
	}

	@Override
	public int getStartX() {
		return x1;
	}

	@Override
	public int getStartZ() {
		return z1;
	}

	@Override
	public int getStartY() {
		return y1;
	}

	@Override
	public int getEndX() {
		return x2;
	}

	@Override
	public int getEndZ() {
		return z2;
	}

	@Override
	public int getEndY() {
		return y2;
	}

	@Override
	public int getStartChunkX() {
		return x1 / 16;
	}

	@Override
	public int getStartChunkZ() {
		return z1 / 16;
	}

	@Override
	public int getEndChunkX() {
		return x1 / 16;
	}

	@Override
	public int getEndChunkZ() {
		return z1 / 16;
	}

	@Override
	public List<ITownFlag> getFlags() {
		return plotFlags;
	}

	@Override
	public List<TownBlock> getEncompasingBlocks() {
		return townBlocks;
	}

	@Override
	public boolean isBlockInsidePlot(int x, int y, int z) {
		return x1 <= x && x <= x2 && y1 <= y && y <= y2 && z1 <= z && z <= z2;
	}

	@Override
	public ITownFlag getFlag(String flagName) {
		for (ITownFlag flag : plotFlags) {
			if (flag.getName().equals(flagName))
				return flag;
		}
		return null;
	}

	@Override
	public int getDim() {
		return dim;
	}

	@Override
	public boolean addFlag(ITownFlag flag) {
		return plotFlags.add(flag);
	}

	@Override
	public boolean removeFlag(String flagName) {
		return plotFlags.remove(getFlag(flagName));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean setName(String name) {
		this.name = name;
		return true;
	}

	@Override
	public boolean setOwner(Resident owner) {
		// Checking if the new owner is part of the town which this plot resides in
		if (!owner.getTowns().contains(town))
			return false;

		this.owner = owner;
		return true;
	}

	@Override
	public String toString() {
		return String.format(EnumChatFormatting.GREEN + " %s\n" + EnumChatFormatting.GRAY + "From: " + EnumChatFormatting.WHITE + "[" + EnumChatFormatting.GREEN + "%s" + EnumChatFormatting.WHITE + "," + EnumChatFormatting.GREEN + " %s" + EnumChatFormatting.WHITE + "," + EnumChatFormatting.GREEN + " %s" + EnumChatFormatting.WHITE + "]" + EnumChatFormatting.GRAY + " to " + EnumChatFormatting.WHITE + "[" + EnumChatFormatting.GREEN + "%s" + EnumChatFormatting.WHITE + "," + EnumChatFormatting.GREEN + " %s" + EnumChatFormatting.WHITE + "," + EnumChatFormatting.GREEN + " %s" + EnumChatFormatting.WHITE + "] " + EnumChatFormatting.GRAY + "\nOwner: " + EnumChatFormatting.WHITE + "%s ", name, x1, y1, z1, x2, y2, z2, owner.getUUID());
	}
}