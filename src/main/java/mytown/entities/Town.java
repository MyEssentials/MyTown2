package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.core.utils.teleport.Teleport;
import mytown.entities.flag.Flag;
import mytown.entities.interfaces.*;
import mytown.proxies.LocalizationProxy;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

// TODO Implement TownType

/**
 * Defines a Town. A Town is made up of Residents, Ranks, Blocks, and Plots.
 *
 * @author Joe Goett
 */
public class Town implements IHasResidents, IHasRanks, IHasBlocks, IHasPlots, IHasFlags, IHasBlockWhitelists, Comparable<Town> {
    private String name, oldName = null;

    public Town(String name) {
        setName(name);
    }

    /**
     * Returns the name of the Town
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Renames this current Town setting oldName to the previous name. You MUST set oldName to null after saving it in the Datasource
     *
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
     *
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

    private Map<Resident, Rank> residents = new Hashtable<Resident, Rank>();

    /**
     * Adds the Resident with the given Rank
     *
     * @param res
     * @param rank
     */
    public void addResident(Resident res, Rank rank) {
        residents.put(res, rank);
    }

    @Override
    public void addResident(Resident res) {
        addResident(res, defaultRank);
    }

    @Override
    public void removeResident(Resident res) {
        for (Iterator<Plot> it = plots.iterator(); it.hasNext(); ) {
            Plot plot = it.next();
            if (plot.hasOwner(res) && plot.getOwners().size() <= 1) {
                it.remove();
            }
        }
        residents.remove(res);
    }

    @Override
    public boolean hasResident(Resident res) {
        return residents.containsKey(res);
    }

    public boolean hasResident(String username) {
        for (Resident res : residents.keySet()) { // TODO Can this be made faster?
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ImmutableList<Resident> getResidents() {
        return ImmutableList.copyOf(residents.keySet());
    }

    /**
     * Returns the Rank the Resident is assigned to.
     *
     * @param
     * @return
     */
    public Rank getResidentRank(Resident res) {
        return residents.get(res);
    }

    /**
     * Sets the given Residents Rank for this Town.
     *
     * @param res
     * @param rank
     */
    public void setResidentRank(Resident res, Rank rank) {
        if (residents.containsKey(res)) { // So a Resident is not accidentally added by setting the Rank of a non-resident
            residents.put(res, rank);
        }
    }

    /* ----- IHasRanks ----- */

    private List<Rank> ranks = new ArrayList<Rank>();
    private Rank defaultRank = null; // TODO Set default rank during creation?

    @Override
    public void addRank(Rank rank) {
        ranks.add(rank);
    }

    @Override
    public void removeRank(Rank rank) {
        ranks.remove(rank);
    }

    @Override
    public void setDefaultRank(Rank rank) {
        defaultRank = rank;
    }

    @Override
    public Rank getDefaultRank() {
        return defaultRank;
    }

    @Override
    public boolean hasRank(Rank rank) {
        return ranks.contains(rank);
    }

    @Override
    public boolean hasRankName(String rankName) {
        for (Rank r : ranks) {
            if (r.getName().equals(rankName))
                return true;
        }
        return false;
    }

    @Override
    public Rank getRank(String rankName) {
        for (Rank r : ranks) {
            if (r.getName().equals(rankName))
                return r;
        }
        return null;
    }

    @Override
    public boolean promoteResident(Resident res, Rank rank) {
        if (hasResident(res) && hasRank(rank)) {
            residents.remove(res);
            residents.put(res, rank);
            return true;
        }
        return false;
    }


    @Override
    public ImmutableList<Rank> getRanks() {
        return ImmutableList.copyOf(ranks);
    }

    /* ----- IHasBlocks ----- */

    protected Map<String, Block> blocks = new Hashtable<String, Block>();

    @Override
    public void addBlock(Block block) {
        blocks.put(block.getKey(), block);
    }

    @Override
    public void removeBlock(Block block) {
        blocks.remove(block);
    }

    @Override
    public boolean hasBlock(Block block) {
        return blocks.containsValue(block);
    }

    @Override
    public ImmutableList<Block> getBlocks() {
        return ImmutableList.copyOf(blocks.values());
    }

    @Override
    public Block getBlockAtCoords(int dim, int x, int z) {
        return blocks.get(String.format(Block.keyFormat, dim, x, z));
    }

    /* ----- IHasPlots ----- */

    private List<Plot> plots = new ArrayList<Plot>();

    @Override
    public void addPlot(Plot plot) {
        plots.add(plot);
    }

    @Override
    public void removePlot(Plot plot) {
        plots.remove(plot);
    }

    @Override
    public boolean hasPlot(Plot plot) {
        return plots.contains(plot);
    }

    @Override
    public ImmutableList<Plot> getPlots() {
        return ImmutableList.copyOf(plots);
    }

    @Override
    public Plot getPlotAtCoords(int dim, int x, int y, int z) {
        for (Plot plot : plots) {
            if (plot.isCoordWithin(dim, x, y, z)) {
                return plot;
            }
        }
        return null;
    }

    public Plot getPlotAtResident(Resident res) {
        return getPlotAtCoords(res.getPlayer().dimension, (int) res.getPlayer().posX, (int) res.getPlayer().posY, (int) res.getPlayer().posZ);
    }

    /* ----- IHasFlags ------ */

    private List<Flag> flags = new ArrayList<Flag>();

    @Override
    public void addFlag(Flag flag) {
        flags.add(flag);
    }

    @Override
    public boolean hasFlag(String name) {
        for (Flag flag : flags)
            if (flag.getName().equals(name))
                return true;
        return false;
    }

    @Override
    public ImmutableList<Flag> getFlags() {
        return ImmutableList.copyOf(flags);
    }

    @Override
    public Flag getFlag(String name) {
        for (Flag flag : flags)
            if (flag.getName().equals(name))
                return flag;
        return null;
    }

    /**
     * Gets the flag on the specified coordinates. Returns town's flag if no plot is found.
     *
     * @param x
     * @param y
     * @param z
     * @param flagName
     * @return
     */
    public Flag getFlagAtCoords(int dim, int x, int y, int z, String flagName) {
        Plot plot = getPlotAtCoords(dim, x, y, z);
        if (plot == null) {
            return getFlag(flagName);
        }
        return plot.getFlag(flagName);
    }

    /* ---- IHasBlockWhitelists ---- */

    private List<BlockWhitelist> blockWhitelists = new ArrayList<BlockWhitelist>();

    @Override
    public void addBlockWhitelist(BlockWhitelist bw) {
        blockWhitelists.add(bw);
    }

    @Override
    public boolean hasBlockWhitelist(int dim, int x, int y, int z, String flagName, int plotID) {
        for (BlockWhitelist bw : blockWhitelists) {
            if (bw.dim == dim && bw.x == x && bw.y == y && bw.z == z && bw.getFlagName().equals(flagName) && bw.getPlotID() == plotID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasBlockWhitelist(BlockWhitelist bw) {
        return blockWhitelists.contains(bw);
    }

    @Override
    public void removeBlockWhitelist(BlockWhitelist bw) {
        blockWhitelists.remove(bw);
    }

    @Override
    public void removeBlockWhitelist(int dim, int x, int y, int z, String flagName, int plotID) {
        for (Iterator<BlockWhitelist> it = blockWhitelists.iterator(); it.hasNext(); ) {
            BlockWhitelist bw = it.next();
            if (bw.dim == dim && bw.x == x && bw.y == y && bw.z == z && bw.getFlagName().equals(flagName) && bw.getPlotID() == plotID) {
                it.remove();
            }
        }
    }

    @Override
    public BlockWhitelist getBlockWhitelist(int dim, int x, int y, int z, String flagName, int plotID) {
        for (BlockWhitelist bw : blockWhitelists) {
            if (bw.dim == dim && bw.x == x && bw.y == y && bw.z == z && bw.getFlagName().equals(flagName) && bw.getPlotID() == plotID) {
                return bw;
            }
        }
        return null;
    }

    @Override
    public List<BlockWhitelist> getWhitelists() {
        return blockWhitelists;
    }

    /*
    public boolean flagValueOfPossiblyWhitelisted(int dim, int x, int y, int z) {
        if(!isPointInTown(dim, x >> 4, y >> 4)) {
            return false;
        } else {

        }
    }
    */


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
     *
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
     *
     * @return
     */
    public boolean hasSpawn() {
        return spawn != null;
    }

    /**
     * Returns the spawn
     *
     * @return
     */
    public Teleport getSpawn() {
        return spawn;
    }

    /**
     * Sets the spawn
     *
     * @param spawn
     */
    public void setSpawn(Teleport spawn) {
        this.spawn = spawn;
    }

    /* ----- Helpers ----- */

    /**
     * Checks if the given point is in this Town
     *
     * @param dim
     * @param x
     * @param z
     * @return
     */
    public boolean isPointInTown(int dim, float x, float z) {
        return isChunkInTown(dim, (int) x >> 4, (int) z >> 4);
    }

    /**
     * Checks if the chunk is in the town
     *
     * @param dim
     * @param cx
     * @param cz
     * @return
     */
    public boolean isChunkInTown(int dim, int cx, int cz) {
        return blocks.containsKey(String.format(Block.keyFormat, dim, cx, cz));
    }

    public void notifyResidentJoin(Resident res) {
        for (Resident toRes : residents.keySet()) {
            toRes.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.joined", res.getPlayerName(), getName()));
        }
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
