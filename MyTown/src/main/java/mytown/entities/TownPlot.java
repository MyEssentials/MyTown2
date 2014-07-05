package mytown.entities;

import java.util.ArrayList;
import java.util.List;

import mytown.entities.town.Town;
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
	protected List<Resident> owners;
    protected List<Resident> whitelist;

	protected int chunkX1, chunkZ1, chunkX2, chunkZ2;

	protected List<TownBlock> townBlocks;
	protected List<ITownFlag> plotFlags;

	public TownPlot(int dim, int x1, int y1, int z1, int x2, int y2, int z2, Town town) {
		this(dim, x1, y1, z1, x2, y2, z2, town, "NoName");
	}

	public TownPlot(int dim, int x1, int y1, int z1, int x2, int y2, int z2, Town town, String name) {
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

        owners = new ArrayList<Resident>();
        whitelist = new ArrayList<Resident>();

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

	@Override
	public Town getTown() {
		return town;
	}

	@Override
	public List<Resident> getOwners() { return owners; }

    @Override
    public boolean addOwner(Resident owner) {
        // Checking if the new owner is part of the town which this plot resides in
        if (!owner.getTowns().contains(town))
            return false;

        return owners.add(owner);
    }


    @Override
    public boolean removeOwner(Resident owner) {
        return owners.remove(owner);
    }

    @Override
    public List<Resident> getWhitelistedResidents() {
        return whitelist;
    }

    @Override
    public boolean addToWhitelist(Resident resident) {
        if(!resident.getTowns().contains(town))
            return false;
        return whitelist.add(resident);
    }

    @Override
    public boolean removeFromWhitelist(Resident resident) {
        return whitelist.remove(resident);
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
	public String toString() {
		String stringOwners = "";
        for(Resident res : owners) {
            if(stringOwners.equals(""))
                stringOwners += res.getUUID();
            else
                stringOwners += ", " + res.getUUID();
        }
        return String.format(EnumChatFormatting.GREEN + " %s\n" + EnumChatFormatting.GRAY + "From: " + EnumChatFormatting.WHITE + "[" + EnumChatFormatting.GREEN + "%s" + EnumChatFormatting.WHITE + "," + EnumChatFormatting.GREEN + " %s" + EnumChatFormatting.WHITE + "," + EnumChatFormatting.GREEN + " %s" + EnumChatFormatting.WHITE + "]" + EnumChatFormatting.GRAY + " to " + EnumChatFormatting.WHITE + "[" + EnumChatFormatting.GREEN + "%s" + EnumChatFormatting.WHITE + "," + EnumChatFormatting.GREEN + " %s" + EnumChatFormatting.WHITE + "," + EnumChatFormatting.GREEN + " %s" + EnumChatFormatting.WHITE + "] " + EnumChatFormatting.GRAY + "\nOwner: " + EnumChatFormatting.WHITE + "%s ", name, x1, y1, z1, x2, y2, z2, stringOwners);
	}
}