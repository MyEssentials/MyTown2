package mytown.entities;

import myessentials.entities.Volume;
import myessentials.utils.PlayerUtils;
import mytown.api.container.FlagsContainer;
import mytown.api.container.GenericContainer;
import mytown.api.container.ResidentsContainer;
import mytown.entities.blocks.SellSign;
import mytown.entities.flag.FlagType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class Plot {
    private int dbID;
    private final int dim, x1, y1, z1, x2, y2, z2;
    private Town town;
    private String key, name;

    public final FlagsContainer flagsContainer = new FlagsContainer();
    public final ResidentsContainer membersContainer = new ResidentsContainer();
    public final ResidentsContainer ownersContainer = new ResidentsContainer();
    public final GenericContainer<SellSign> signContainer = new GenericContainer<SellSign>();

    public Plot(String name, Town town, int dim, int x1, int y1, int z1, int x2, int y2, int z2) {
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
        this.name = name;
        this.town = town;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.dim = dim;

        updateKey();
    }

    public boolean isCoordWithin(int dim, int x, int y, int z) {
        return dim == this.dim && x1 <= x && x <= x2 && y1 <= y && y <= y2 && z1 <= z && z <= z2;
    }

    public boolean hasPermission(Resident res, FlagType flagType, Object denialValue) {
        return !flagsContainer.getValue(flagType).equals(denialValue) || membersContainer.contains(res) || ownersContainer.contains(res) || PlayerUtils.isOp(res.getPlayer());
    }

    public void checkForSellSign() {
        World world = MinecraftServer.getServer().worldServerForDimension(dim);
        SellSign sign;
        for(int i = x1; i <= x2; i++) {
            for(int j = y1; j <= y2; j++) {
                for(int k = z1; k <= z2; k++) {
                    sign = SellSign.findSignAndCreate(world, i, j, k);
                    if(sign != null) {
                        signContainer.set(sign);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Plot: {Name: %s, Dim: %s, Start: [%s, %s, %s], End: [%s, %s, %s]}", name, dim, x1, y1, z1, x2, y2, z2);
    }

    public int getDim() {
        return dim;
    }

    public int getStartX() {
        return x1;
    }

    public int getStartY() {
        return y1;
    }

    public int getStartZ() {
        return z1;
    }

    public int getEndX() {
        return x2;
    }

    public int getEndY() {
        return y2;
    }

    public int getEndZ() {
        return z2;
    }

	public int getIntersectingArea(Volume rangeBox) {
		// Check if ranges max is greater than plots min and ranges min is less than plots max
        if (rangeBox.getMaxX() >= x1 && rangeBox.getMinX() <= x2 &&
            rangeBox.getMaxY() >= y1 && rangeBox.getMinY() <= y2 &&
            rangeBox.getMaxZ() >= z1 && rangeBox.getMinZ() <= z2) {

    		int minX, maxX, minY, maxY, minZ, maxZ;

        	minX = (x1 < rangeBox.getMinX()) ? rangeBox.getMinX() : x1;
        	minY = (y1 < rangeBox.getMinY()) ? rangeBox.getMinY() : y1;
        	minZ = (z1 < rangeBox.getMinZ()) ? rangeBox.getMinZ() : z1;
        	maxX = (x2 > rangeBox.getMaxX()) ? rangeBox.getMaxX() : x2;
        	maxY = (y2 > rangeBox.getMaxY()) ? rangeBox.getMaxY() : y2;
        	maxZ = (z2 > rangeBox.getMaxZ()) ? rangeBox.getMaxZ() : z2;

    		return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    	}

        return 0;
    }

    public int getStartChunkX() {
        return x1 >> 4;
    }

    public int getStartChunkZ() {
        return z1 >> 4;
    }

    public int getEndChunkX() {
        return x2 >> 4;
    }

    public int getEndChunkZ() {
        return z2 >> 4;
    }

    public Town getTown() {
        return town;
    }

    public String getKey() {
        return key;
    }

    /**
     * Updates the key of the plot if any changes have been made to it.
     */
    private void updateKey() {
        key = String.format("%s;%s;%s;%s;%s;%s;%s", dim, x1, y1, z1, x2, y2, z2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDbID(int id) {
        this.dbID = id;
    }

    public int getDbID() {
        return this.dbID;
    }
}
