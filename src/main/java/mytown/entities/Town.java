package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.MyTown;
import mytown.api.interfaces.*;
import mytown.config.Config;
import mytown.core.Utils;
import mytown.core.utils.teleport.Teleport;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.handlers.VisualsTickHandler;
import mytown.proxies.LocalizationProxy;
import mytown.util.MyTownUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.DimensionManager;

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

    /**
     * Returns the name that is currently used in the DB
     *
     * @return
     */
    public String getDBName() {
        if (getOldName() == null)
            return getName();
        else
            return getOldName();
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

    protected Map<String, TownBlock> blocks = new Hashtable<String, TownBlock>();
    protected int extraBlocks = 0;
    private boolean isShowingBorders = false;

    @Override
    public void addBlock(TownBlock block) {
        blocks.put(block.getKey(), block);
        if(isShowingBorders) {
            hideBorders();
            showBorders();
        }
    }

    @Override
    public void removeBlock(TownBlock block) {
        blocks.remove(block.getKey());
        if(isShowingBorders) {
            hideBorders();
            showBorders();
        }
    }

    @Override
    public boolean hasBlock(TownBlock block) {
        return blocks.containsValue(block);
    }

    @Override
    public ImmutableList<TownBlock> getBlocks() {
        return ImmutableList.copyOf(blocks.values());
    }

    @Override
    public TownBlock getBlockAtCoords(int dim, int x, int z) {
        return blocks.get(String.format(TownBlock.keyFormat, dim, x, z));
    }

    @Override
    public int getExtraBlocks() {
        return extraBlocks;
    }

    @Override
    public void setExtraBlocks(int extra) {
        extraBlocks = extra;
    }

    @Override
    public int getMaxBlocks() { // TODO Cache this stuff?
        int tmpCount = Config.blocksMayor + (Config.blocksResident * (residents.size() - 1)) + extraBlocks;

        for (Resident res : getResidents()) {
            tmpCount += res.getExtraBlocks();
        }

        return tmpCount;
    }

    public void showBorders() {
        isShowingBorders = true;
        VisualsTickHandler.instance.markTownBorders(this);
    }

    public void hideBorders() {
        isShowingBorders = false;
        VisualsTickHandler.instance.unmarkBlocks(this);
    }

    @Override
    public boolean hasMaxAmountOfBlocks() {
        return blocks.size() >= getMaxBlocks();
    }

    /* ----- IHasPlots ----- */

    private int maxPlots;
    private boolean isShowingPlots = false;

    public void setMaxPlots(int maxPlots) {
        this.maxPlots = maxPlots;
    }

    public int getMaxPlots() {
        return maxPlots;
    }

    /**
     * Returns plots owned by the player
     *
     * @param res
     * @return
     */
    public List<Plot> getPlotsOwned(Resident res) {
        List<Plot> list = new ArrayList<Plot>();
        for (Plot plot : plots) {
            if (plot.hasOwner(res))
                list.add(plot);
        }
        return list;
    }

    /**
     * Gets how many plots this resident is owner in
     *
     * @param res
     * @return
     */
    public int getAmountPlotsOwned(Resident res) {
        int x = 0;
        for (Plot plot : plots) {
            if (plot.hasOwner(res))
                x++;
        }
        return x;
    }

    /**
     * Returns if resident can make any more plots
     *
     * @param res
     * @return
     */
    public boolean canResidentMakePlot(Resident res) {
        return getAmountPlotsOwned(res) >= maxPlots && !residents.get(res).hasPermission("mytown.plot.unlimited");
    }

    private List<Plot> plots = new ArrayList<Plot>();

    @Override
    public void addPlot(Plot plot) {
        if(isShowingPlots)
            VisualsTickHandler.instance.markPlotBorders(plot);

        plots.add(plot);
    }

    @Override
    public void removePlot(Plot plot) {
        for (int x = plot.getStartChunkX(); x <= plot.getEndChunkX(); x++) {
            for (int z = plot.getStartChunkZ(); z <= plot.getEndChunkZ(); z++) {
                TownBlock b = getBlockAtCoords(plot.getDim(), x, z);
                if (b != null) {
                    b.removePlot(plot);
                }
            }
        }
        if(isShowingPlots)
            VisualsTickHandler.instance.unmarkBlocks(plot);
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
        TownBlock b = getBlockAtCoords(dim, x >> 4, z >> 4);
        if (b != null) {
            return b.getPlotAtCoords(dim, x, y, z);
        }

        return null;
    }

    public Plot getPlotAtResident(Resident res) {
        return getPlotAtCoords(res.getPlayer().dimension, (int) res.getPlayer().posX, (int) res.getPlayer().posY, (int) res.getPlayer().posZ);
    }

    public void showPlots() {
        this.isShowingPlots = true;
        for (Plot plot : plots) {
            VisualsTickHandler.instance.markPlotBorders(plot);
        }
    }

    public void hidePlots() {
        this.isShowingPlots = false;
        for (Plot plot : plots) {
            VisualsTickHandler.instance.unmarkBlocks(plot);
        }
    }

    /* ----- IHasFlags ------ */

    private List<Flag> flags = new ArrayList<Flag>();

    @Override
    public void addFlag(Flag flag) {
        flags.add(flag);
    }

    @Override
    public boolean hasFlag(FlagType type) {
        for (Flag flag : flags)
            if (flag.flagType == type)
                return true;
        return false;
    }

    @Override
    public ImmutableList<Flag> getFlags() {
        return ImmutableList.copyOf(flags);
    }

    @Override
    public Flag getFlag(FlagType type) {
        for (Flag flag : flags)
            if (flag.flagType == type)
                return flag;
        return null;
    }

    @Override
    public boolean removeFlag(FlagType type) {
        for (Iterator<Flag> it = flags.iterator(); it.hasNext(); ) {
            if (it.next().flagType == type) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getValue(FlagType type) {
        for (Flag flag : flags) {
            if (flag.flagType == type)
                return flag.getValue();
        }
        return type.getDefaultValue();
    }

    @Override
    public Object getValueAtCoords(int dim, int x, int y, int z, FlagType flagType) {
        Plot plot = getPlotAtCoords(dim, x, y, z);
        if (plot == null) {
            return getValue(flagType);
        }
        return plot.getValue(flagType);
    }

    /* ---- IHasBlockWhitelists ---- */

    private List<BlockWhitelist> blockWhitelists = new ArrayList<BlockWhitelist>();

    @Override
    public void addBlockWhitelist(BlockWhitelist bw) {
        blockWhitelists.add(bw);
    }

    @Override
    public boolean hasBlockWhitelist(int dim, int x, int y, int z, FlagType flagType) {
        for (BlockWhitelist bw : blockWhitelists) {
            if (bw.dim == dim && bw.x == x && bw.y == y && bw.z == z && bw.getFlagType().equals(flagType)) {
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
    public void removeBlockWhitelist(int dim, int x, int y, int z, FlagType flagType) {
        for (Iterator<BlockWhitelist> it = blockWhitelists.iterator(); it.hasNext(); ) {
            BlockWhitelist bw = it.next();
            if (bw.dim == dim && bw.x == x && bw.y == y && bw.z == z && bw.getFlagType().equals(flagType)) {
                it.remove();
            }
        }
    }

    @Override
    public BlockWhitelist getBlockWhitelist(int dim, int x, int y, int z, FlagType flagType) {
        for (BlockWhitelist bw : blockWhitelists) {
            if (bw.dim == dim && bw.x == x && bw.y == y && bw.z == z && bw.getFlagType().equals(flagType)) {
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
        EntityPlayer pl = res.getPlayer();
        if (pl != null) {
            spawn.teleport(pl);
            res.setTeleportCooldown(Config.teleportCooldown);
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
    public boolean isPointInTown(int dim, int x, int z) {
        return isChunkInTown(dim, x >> 4, z >> 4);
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
        return blocks.containsKey(String.format(TownBlock.keyFormat, dim, cx, cz));
    }

    public void notifyResidentJoin(Resident res) {
        for (Resident toRes : residents.keySet()) {
            toRes.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.joined", res.getPlayerName(), getName()));
        }
    }

    public void notifyEveryone(String message) {
        // TODO: Check permission for if it should receive message
        for (Resident r : residents.keySet()) {
            r.sendMessage(message);
        }
    }

    public boolean residentHasFriendInTown(Resident res) {
        if (res.hasTown(this))
            return true;
        for (Resident r : residents.keySet()) {
            if (r.hasFriend(res))
                return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean checkPermission(Resident res, FlagType flagType, int dim, int x, int y, int z) {
        if (flagType.getType() != Boolean.class)
            throw new RuntimeException("FlagType is not boolean!");
        Plot plot = getPlotAtCoords(dim, x, y, z);

        if (plot == null) {
            if (!(Boolean) getValue(flagType) && !hasResident(res) && !residentHasFriendInTown(res)) {
                //if (MyTown.instance.isCauldron && BukkitCompat.getInstance().hasPEX())
                    // Check if PEX has a permission bypass
                //    return PEXCompat.getInstance().checkPermission(res, flagType.getBypassPermission()) || Utils.isOp(res);
                //else
                    // Check, when PEX is not present, if player is OP
                    return Utils.isOp(res.getPlayer());
            }
        } else {
            if (!(Boolean) plot.getValue(flagType) && !plot.hasResident(res) && !plot.residentHasFriendInPlot(res))
                return Utils.isOp(res.getPlayer());
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean checkPermission(Resident res, FlagType flagType) {
        if (flagType.getType() != Boolean.class)
            throw new RuntimeException("FlagType is not boolean!");

        if (!(Boolean) getValue(flagType) && !hasResident(res) && !residentHasFriendInTown(res)) {
            //TODO: Check for permission
            return Utils.isOp(res.getPlayer());
        }
        return true;
    }

    /**
     * Used to get the owners of a plot (or a town) at the position given
     * Returns null if position is not in town
     *
     * @return
     */
    public List<Resident> getOwnersAtPosition(int dim, int x, int y, int z) {
        List<Resident> list = new ArrayList<Resident>();
        Plot plot = getPlotAtCoords(dim, x, y, z);
        if (plot == null) {
            if (isPointInTown(dim, x, z) && !(this instanceof AdminTown) && residents.size() != 0) {
                list.add(getMayor());
            } else {
                return list;
            }
        } else {
            for (Resident res : plot.getOwners()) {
                list.add(res);
            }
        }
        return list;
    }

    /**
     * Gets the resident with the rank of 'Mayor' (or whatever it's named)
     *
     * @return
     */
    public Resident getMayor() {
        for (Resident res : residents.keySet()) {
            if (residents.get(res).getName().equals(Rank.theMayorDefaultRank))
                return res;
        }
        return null;
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
