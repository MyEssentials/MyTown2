package mytown.entities;

import mytown.core.utils.teleport.Teleport;
import mytown.entities.interfaces.IHasBlocks;
import mytown.entities.interfaces.IHasPlots;
import mytown.entities.interfaces.IHasRanks;
import mytown.entities.interfaces.IHasResidents;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// TODO Implement TownType

/**
 * Defines a Town. A Town is made up of Residents, Ranks, Blocks, and Plots.
 *
 * @author Joe Goett
 */
public class Town implements IHasResidents, IHasRanks, IHasBlocks, IHasPlots, Comparable<Town> {
    private String name, oldName = null;

    public Town(String name) {
        setName(name);
    }

    /**
     * Returns the name of the Town
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Renames this current Town setting oldName to the previous name. You MUST set oldName to null after saving it in the Datasource
     * @param newName
     */
    public void rename(String newName) {
        oldName = name;
        name = newName;
    }

    public String getOldName() {
        return oldName;
    }

    /**
     * Resets the oldName to null. You MUST call this after a name change in the Datasource!
     */
    public void resetOldName() {
        oldName = null;
    }

    /**
     * Sets the name of the Town
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Town: {Name: %s}", name);
    }

    /* ----- IHasResidents ----- */

    private Map<Resident, Rank> residents = null;

    /**
     * Adds the Resident with the given Rank
     * @param res
     * @param rank
     */
    public void addResident(Resident res, Rank rank) {
        residents.put(res, rank);
    }

    public void addResident(Resident res) {
        addResident(res, defaultRank);
    }

    public void removeResident(Resident res) {
        residents.remove(res);
    }

    public boolean hasResident(Resident res) {
        return residents.containsKey(res);
    }

    public Collection<Resident> getResidents() {
        return residents.keySet();
    }

    /**
     * Returns the Rank the Resident is assigned to.
     * @param
     * @return
     */
    public Rank getResidentRank(Resident res) {
        return residents.get(res);
    }

    /**
     * Sets the given Residents Rank for this Town.
     * @param res
     * @param rank
     */
    public void setResidentRank(Resident res, Rank rank) {
        if (residents.containsKey(res)) { // So a Resident is not accidentally added by setting the Rank of a non-resident
            residents.put(res, rank);
        }
    }

    /* ----- IHasRanks ----- */

    private List<Rank> ranks = null;
    private Rank defaultRank = null; // TODO Set default rank during creation?

    public void addRank(Rank rank) {
        ranks.add(rank);
    }

    public void removeRank(Rank rank) {
        ranks.remove(rank);
    }

    public void setDefaultRank(Rank rank) {
        defaultRank = rank;
    }

    public Rank getDefaultRank() {
        return defaultRank;
    }

    public boolean hasRank(Rank rank) {
        return ranks.contains(rank);
    }

    public Collection<Rank> getRanks() {
        return ranks;
    }

    /* ----- IHasBlocks ----- */

    private Map<String, Block> blocks = null;

    public void addBlock(Block block) {
        blocks.put(block.getKey(), block);
    }

    public void removeBlock(Block block) {
        blocks.remove(block);
    }

    public boolean hasBlock(Block block) {
        return blocks.containsValue(block);
    }

    public Collection<Block> getBlocks() {
        return blocks.values();
    }

    public Block getBlockAtCoords(int dim, int x, int z) {
        return blocks.get(String.format(Block.keyFormat, dim, x, z));
    }

    /* ----- IHasPlots ----- */

    private List<Plot> plots = null;

    public void addPlot(Plot plot) {
        plots.add(plot);
    }

    public void removePlot(Plot plot) {
        plots.remove(plot);
    }

    public boolean hasPlot(Plot plot) {
        return plots.contains(plot);
    }

    public Collection<Plot> getPlots() {
        return plots;
    }

    public Plot getPlotAtCoord(int dim, int x, int y, int z) {
        return getBlockAtCoords(dim, x >> 4, z >> 4).getPlotAtCoord(dim, x, y, z);
    }

    /* ----- Nation ----- */

    private Nation nation = null;

    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation nation) {
        this.nation = nation;
    }

    /* ----- Spawn ----- */

    private Teleport spawn = null;

    /**
     * Sends the Resident to the spawn
     * @param res
     */
    public void sendToSpawn(Resident res) {
        if (spawn == null) return;
        EntityPlayer pl = res.getPlayer();
        if (pl != null) {
            spawn.teleport(pl);
        }
    }

    /**
     * Returns if this Town has a spawn
     * @return
     */
    public boolean hasSpawn() {
        return spawn != null;
    }

    /**
     * Returns the spawn
     * @return
     */
    public Teleport getSpawn() {
        return spawn;
    }

    /**
     * Sets the spawn
     * @param spawn
     */
    public void setSpawn(Teleport spawn) {
        this.spawn = spawn;
    }

    /* ----- Helpers ----- */

    /**
     * Checks if the given point is in this Town
     * @param dim
     * @param x
     * @param z
     * @return
     */
    public boolean isPointInTown(int dim, float x, float z) {
        return isChunkInTown(dim, (int)x >> 4, (int)z >> 4);
    }

    /**
     * Checks if the chunk is in the town
     * @param dim
     * @param cx
     * @param cz
     * @return
     */
    public boolean isChunkInTown(int dim, int cx, int cz) {
        return blocks.containsKey(String.format(Block.keyFormat, dim, cx, cz));
    }

    /* ----- Comparable ----- */

    @Override
    public int compareTo(Town t) { // TODO Flesh this out more for ranking towns?
        int thisNumberOfResidents = residents.size(),
                thatNumberOfResidents = t.getResidents().size();
        if (thisNumberOfResidents > thatNumberOfResidents)
            return -1;
        else if (thisNumberOfResidents == thatNumberOfResidents)
            return 0;
        else if (thisNumberOfResidents < thatNumberOfResidents)
            return 1;

        return -1;
    }
}
