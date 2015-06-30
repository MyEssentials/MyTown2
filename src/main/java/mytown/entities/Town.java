package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.api.interfaces.*;
import mytown.config.Config;
import mytown.core.utils.PlayerUtils;
import mytown.core.teleport.Teleport;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.handlers.VisualsHandler;
import mytown.proxies.LocalizationProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.*;

/**
 * Defines a Town. A Town is made up of Residents, Ranks, Blocks, and Plots.
 */
public class Town implements IResidentsContainer, IRanksContainer, ITownBlocksContainer, IPlotsContainer, IFlagsContainer, IBlockWhitelistsContainer, Comparable<Town> {
    private String name, oldName = null;

    private final Map<Resident, Rank> residents = new HashMap<Resident, Rank>();
    private final List<Rank> ranks = new ArrayList<Rank>();
    private final List<Flag> flags = new ArrayList<Flag>();
    private final List<BlockWhitelist> blockWhitelists = new ArrayList<BlockWhitelist>();
    private final List<Plot> plots = new ArrayList<Plot>();
    protected final Map<String, TownBlock> blocks = new HashMap<String, TownBlock>();

    private Rank defaultRank = null;
    protected int extraBlocks = 0;
    protected int maxFarClaims = Config.maxFarClaims;
    private int maxPlots;

    private int bankAmount = 0;
    private int daysNotPaid = 0;

    private Nation nation = null;
    private Teleport spawn = null;

    public Town(String name) { setName(name); }

    public void setMaxPlots(int maxPlots) { this.maxPlots = maxPlots; }
    public int getMaxPlots() { return maxPlots; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOldName() { return oldName; }

    /**
     * Renames this current Town setting oldName to the previous name. You MUST set oldName to null after saving it in the Datasource
     */
    public void rename(String newName) {
        oldName = name;
        name = newName;
    }

    /**
     * Resets the oldName to null. You MUST call this after a name change in the Datasource!
     */
    public void resetOldName() {
        oldName = null;
    }

    /* ----- IHasResidents ----- */

    public void addResident(Resident res, Rank rank) { residents.put(res, rank); }

    @Override
    public void addResident(Resident res) { addResident(res, defaultRank); }

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
    public boolean hasResident(Resident res) { return res != null && residents.containsKey(res); }

    public boolean hasResident(String username) {
        for (Resident res : residents.keySet()) {
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ImmutableList<Resident> getResidents() { return ImmutableList.copyOf(residents.keySet()); }
    public Rank getResidentRank(Resident res) { return residents.get(res); }
    public void setResidentRank(Resident res, Rank rank) {
        if (residents.containsKey(res)) {
            residents.put(res, rank);
        }
    }

    /* ----- IHasRanks ----- */


    @Override
    public void addRank(Rank rank) { ranks.add(rank); }

    @Override
    public void removeRank(Rank rank) { ranks.remove(rank); }

    @Override
    public void setDefaultRank(Rank rank) { defaultRank = rank; }

    @Override
    public Rank getDefaultRank() { return defaultRank; }

    @Override
    public boolean hasRank(Rank rank) { return ranks.contains(rank); }

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

    @Override
    public void addBlock(TownBlock block) {
        blocks.put(block.getKey(), block);

        VisualsHandler.instance.updateTownBorders(this);
    }

    @Override
    public void removeBlock(TownBlock block) {
        blocks.remove(block.getKey());
        VisualsHandler.instance.updateTownBorders(this);
    }

    @Override
    public boolean hasBlock(TownBlock block) { return blocks.containsValue(block); }

    @Override
    public ImmutableList<TownBlock> getBlocks() { return ImmutableList.copyOf(blocks.values()); }

    @Override
    public TownBlock getBlockAtCoords(int dim, int x, int z) {  return blocks.get(String.format(TownBlock.KEY_FORMAT, dim, x, z)); }

    @Override
    public int getExtraBlocks() { return extraBlocks; }

    @Override
    public void setExtraBlocks(int extra) { extraBlocks = extra < 0 ? 0 : extra; }

    @Override
    public int getMaxBlocks() { // TODO Cache this stuff?
        int maxBlocks = Config.blocksMayor + (Config.blocksResident * (residents.size() - 1)) + extraBlocks;
        for (Resident res : getResidents()) {
            maxBlocks += res.getExtraBlocks();
        }

        return maxBlocks;
    }

    public int getFarClaims() {
        int farClaims = 0;
        for(TownBlock block : blocks.values())
            if(block.isFarClaim())
                farClaims++;
        return farClaims;
    }

    @Override
    public int getMaxFarClaims() { return maxFarClaims; }

    @Override
    public void setMaxFarClaims(int maxFarClaims) { this.maxFarClaims = maxFarClaims; }

    public void showBorders(Resident caller) {
        if(caller.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.markTownBorders(this, (EntityPlayerMP)caller.getPlayer());
    }

    public void hideBorders(Resident caller) {
        if(caller.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) caller.getPlayer(), this);
    }

    /* ----- IHasPlots ----- */


    /**
     * Returns plots owned by the resident
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
     */
    public boolean canResidentMakePlot(Resident res) {
        if(!residents.containsKey(res))
            return false;
        return getAmountPlotsOwned(res) >= maxPlots && !residents.get(res).hasPermission("mytown.plot.unlimited");
    }

    @Override
    public void addPlot(Plot plot) { plots.add(plot); }

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
        plots.remove(plot);
    }

    @Override
    public boolean hasPlot(Plot plot) { return plots.contains(plot); }

    @Override
    public ImmutableList<Plot> getPlots() { return ImmutableList.copyOf(plots); }

    @Override
    public Plot getPlotAtCoords(int dim, int x, int y, int z) {
        TownBlock b = getBlockAtCoords(dim, x >> 4, z >> 4);
        if (b != null) {
            return b.getPlotAtCoords(dim, x, y, z);
        }

        return null;
    }

    @Override
    public Plot getPlot(String name) {
        for(Plot plot : plots) {
            if(plot.getName().equals(name))
                return plot;
        }
        return null;
    }

    public Plot getPlotAtResident(Resident res) {
        /*
        MyTown.instance.log.info("Floor: " + Math.floor(res.getPlayer().posX) + ", " + Math.floor(res.getPlayer().posY) + ", " + Math.floor(res.getPlayer().posZ));
        MyTown.instance.log.info("Ceil: " + Math.ceil(res.getPlayer().posX) + ", " + Math.ceil(res.getPlayer().posY) + ", " + Math.ceil(res.getPlayer().posZ));
        MyTown.instance.log.info("Pos: " + (int)res.getPlayer().posX + ", " + (int)res.getPlayer().posY + ", " + (int)res.getPlayer().posZ);
        */
        return getPlotAtCoords(res.getPlayer().dimension, (int) Math.floor(res.getPlayer().posX), (int) Math.floor(res.getPlayer().posY), (int) Math.floor(res.getPlayer().posZ));
    }

    public void showPlots(Resident caller) {
        if(caller.getPlayer() instanceof EntityPlayerMP)
            for(Plot plot : plots) {
                VisualsHandler.instance.markPlotBorders(plot, (EntityPlayerMP)caller.getPlayer());
            }
    }

    public void hidePlots(Resident caller) {
        if(caller.getPlayer() instanceof EntityPlayerMP)
            for(Plot plot : plots)
                VisualsHandler.instance.unmarkBlocks((EntityPlayerMP)caller.getPlayer(), plot);
    }

    /* ----- IHasFlags ------ */

    @Override
    public void addFlag(Flag flag) {
        flags.add(flag);
        Collections.sort(flags);
    }

    @Override
    public boolean hasFlag(FlagType type) {
        for (Flag flag : flags)
            if (flag.getFlagType() == type)
                return true;
        return false;
    }

    @Override
    public ImmutableList<Flag> getFlags() { return ImmutableList.copyOf(flags); }

    @Override
    public Flag getFlag(FlagType type) {
        for (Flag flag : flags)
            if (flag.getFlagType() == type)
                return flag;
        return null;
    }

    @Override
    public boolean removeFlag(FlagType type) {
        for (Iterator<Flag> it = flags.iterator(); it.hasNext(); ) {
            if (it.next().getFlagType() == type) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getValue(FlagType type) {
        for (Flag flag : flags) {
            if (flag.getFlagType() == type)
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

    @Override
    public void addBlockWhitelist(BlockWhitelist bw) { blockWhitelists.add(bw); }

    @Override
    public boolean hasBlockWhitelist(int dim, int x, int y, int z, FlagType flagType) {
        for (BlockWhitelist bw : blockWhitelists) {
            if (bw.getDim() == dim && bw.getX() == x && bw.getY() == y && bw.getZ() == z && bw.getFlagType().equals(flagType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasBlockWhitelist(BlockWhitelist bw) { return blockWhitelists.contains(bw); }

    @Override
    public void removeBlockWhitelist(BlockWhitelist bw) { blockWhitelists.remove(bw); }

    @Override
    public void removeBlockWhitelist(int dim, int x, int y, int z, FlagType flagType) {
        for (Iterator<BlockWhitelist> it = blockWhitelists.iterator(); it.hasNext(); ) {
            BlockWhitelist bw = it.next();
            if (bw.getDim() == dim && bw.getX() == x && bw.getY() == y && bw.getZ() == z && bw.getFlagType().equals(flagType)) {
                it.remove();
            }
        }
    }

    @Override
    public BlockWhitelist getBlockWhitelist(int dim, int x, int y, int z, FlagType flagType) {
        for (BlockWhitelist bw : blockWhitelists) {
            if (bw.getDim() == dim && bw.getX() == x && bw.getY() == y && bw.getZ() == z && bw.getFlagType().equals(flagType)) {
                return bw;
            }
        }
        return null;
    }

    @Override
    public List<BlockWhitelist> getWhitelists() { return ImmutableList.copyOf(blockWhitelists); }

    /* ----- Nation ----- */

    public Nation getNation() { return nation; }
    public void setNation(Nation nation) { this.nation = nation; }

    /* ----- Spawn ----- */

    public void sendToSpawn(Resident res) {
        EntityPlayer pl = res.getPlayer();
        if (pl != null) {
            PlayerUtils.teleport((EntityPlayerMP)pl, spawn.getDim(), spawn.getX(), spawn.getY(), spawn.getZ());
            res.setTeleportCooldown(Config.teleportCooldown);
        }
    }

    public boolean hasSpawn() { return spawn != null; }
    public Teleport getSpawn() { return spawn; }
    public void setSpawn(Teleport spawn) { this.spawn = spawn; }

    /* ----- Bank ----- */

    public void setBankAmount(int amount) { bankAmount = amount; }
    public int getBankAmount() { return this.bankAmount; }

    public boolean makePayment(int amount) {
        if (bankAmount >= amount) {
            bankAmount -= amount;
            return true;
        }
        return false;
    }

    public void payUpkeep() {
        int amount = getNextPaymentAmount();
        if(makePayment(amount)) {
            daysNotPaid = 0;
            notifyEveryone(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.upkeep"));
        } else {
            daysNotPaid++;
            notifyEveryone(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.upkeep.failed", Config.upkeepTownDeletionDays - daysNotPaid));
        }

    }

    public void setDaysNotPaid(int days) { this.daysNotPaid = days; }
    public int getDaysNotPaid() { return this.daysNotPaid; }

    public int getNextPaymentAmount() {
        return (Config.costTownUpkeep + Config.costAdditionalUpkeep * blocks.size()) * (1 + daysNotPaid);
    }

    /* ----- Helpers ----- */

    /**
     * Checks if the given block in non-chunk coordinates is in this Town
     */
    public boolean isPointInTown(int dim, int x, int z) { return isChunkInTown(dim, x >> 4, z >> 4); }
    public boolean isChunkInTown(int dim, int cx, int cz) {
        return blocks.containsKey(String.format(TownBlock.KEY_FORMAT, dim, cx, cz));
    }

    public void notifyResidentJoin(Resident res) {
        for (Resident toRes : residents.keySet()) {
            toRes.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.joined", res.getPlayerName(), getName()));
        }
    }

    /**
     * Notifies every resident in this town sending a message.
     */
    public void notifyEveryone(String message) {
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

    /**
     * Checks if the Resident is allowed to do the action specified by the FlagType at the coordinates given.
     * This method will go through all the plots and prioritize the plot's flags over town flags.
     */
    @SuppressWarnings("unchecked")
    public boolean checkPermission(Resident res, FlagType flagType, Object denialValue, int dim, int x, int y, int z) {
        Plot plot = getPlotAtCoords(dim, x, y, z);

        if (plot == null || (flagType.isTownOnly() && flagType != FlagType.MODIFY)) {
            return checkPermission(res, flagType, denialValue);
        } else {
            if (plot.getValue(flagType).equals(denialValue) && !plot.hasResident(res))
                return PlayerUtils.isOp(res.getPlayer());
        }
        return true;
    }

    /**
     * Checks if the Resident is allowed to do the action specified by the FlagType in this town.
     */
    @SuppressWarnings("unchecked")
    public boolean checkPermission(Resident res, FlagType flagType, Object denialValue) {
        if(getValue(flagType).equals(denialValue) && (!hasResident(res) || ((Boolean)getValue(FlagType.RESTRICTIONS) && flagType != FlagType.ENTER && getMayor() != res))) {
            return PlayerUtils.isOp(res.getPlayer());
        }
        return true;
    }

    /**
     * Used to get the owners of a plot (or a town) at the position given
     * Returns null if position is not in town
     */
    public List<Resident> getOwnersAtPosition(int dim, int x, int y, int z) {
        List<Resident> list = new ArrayList<Resident>();
        Plot plot = getPlotAtCoords(dim, x, y, z);
        if (plot == null) {
            if (isPointInTown(dim, x, z) && !(this instanceof AdminTown) && !residents.isEmpty()) {
            	Resident mayor = getMayor();
                if (mayor != null) {
                	list.add(mayor);
                }
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
