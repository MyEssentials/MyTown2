package mytown.entities;

import myessentials.entities.Volume;
import mytown.api.container.PlotsContainer;
import mytown.config.Config;

public class TownBlock {
    /**
     * Used for storing in database
     */
    public static final String KEY_FORMAT = "%s;%s;%s";

    private final int dim;
    private final int x;
    private final int z;
    private final Town town;
    private String key;

    private final boolean isFarClaim;
    private final int pricePaid;

    public final PlotsContainer plotsContainer = new PlotsContainer(Config.defaultMaxPlots);

    public TownBlock(int dim, int x, int z, boolean isFarClaim, int pricePaid, Town town) {
        this.dim = dim;
        this.x = x;
        this.z = z;
        this.town = town;
        this.isFarClaim = isFarClaim;
        this.pricePaid = pricePaid;
        updateKey();
    }

    public int getDim() {
        return dim;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String getCoordString() {
        return String.format("%s, %s", x, z);
    }

    public Town getTown() {
        return town;
    }

    public String getKey() {
        return key;
    }

    private void updateKey() {
        key = String.format(KEY_FORMAT, dim, x, z);
    }

    public boolean isFarClaim() {
        return this.isFarClaim;
    }

    public int getPricePaid() {
        return this.pricePaid;
    }

    @Override
    public String toString() {
        return String.format("Block: {Dim: %s, X: %s, Z: %s, Town: %s, Plots: %s}", dim, x, z, town.getName(), plotsContainer.size());
    }

    /* ----- Helpers ----- */

    /**
     * Checks if the point is inside this Block
     */
    public boolean isPointIn(int dim, float x, float z) {
        return isChunkIn(dim, ((int) x) >> 4, ((int) z) >> 4);
    }

    /**
     * Checks if the chunk is this Block
     */
    public boolean isChunkIn(int dim, int cx, int cz) {
        return dim == this.dim && cx == x && cz == z;
    }
    
    public Volume getAreaLimit(Volume area) {
    	int minX = this.getX() << 4;
        int minY = area.getMinY();
        int minZ = this.getZ() << 4;
        int maxX = minX + 15;
        int maxY = area.getMaxY();
        int maxZ = maxY + 15;
        
        if (area.getMinX() > minX) {
        	minX = area.getMinX();
        }
        if (area.getMinZ() > minZ) {
        	minZ = area.getMinZ();
        }
        if (area.getMaxX() < maxX) {
        	maxX = area.getMaxX();
        }
        if (area.getMaxZ() < maxZ) {
        	maxZ = area.getMaxZ();
        }

        return new Volume(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
