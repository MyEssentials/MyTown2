package mytown.datasource;

import mytown.entities.*;
import mytown.entities.flag.Flag;

/**
 * @author Joe Goett
 */
public class InMemoryDatasource extends MyTownDatasource {
    @Override
    public boolean initialize() {
        log.debug("Initializing In-Memory Datasource");
        return true;
    }

    @Override
    protected boolean loadWorlds() {
        return false;
    }

    @Override
    protected boolean loadTowns() {
        log.debug("Loading Towns");
        return true;
    }

    @Override
    protected boolean loadBlocks() {
        log.debug("Loading Blocks");
        return true;
    }

    @Override
    protected boolean loadRanks() {
        log.debug("Loading Ranks");
        return true;
    }

    @Override
    protected boolean loadResidents() {
        log.debug("Loading Residents");
        return true;
    }

    @Override
    protected boolean loadPlots() {
        log.debug("Loading Plots");
        return true;
    }

    @Override
    protected boolean loadNations() {
        log.debug("Loading Nations");
        return true;
    }

    @Override
    protected boolean loadTownFlags() {
        log.debug("Loading Flags");
        return true;
    }

    @Override
    protected boolean loadPlotFlags() {
        log.debug("Loading Flags");
        return true;
    }

    @Override
    protected boolean loadBlockWhitelists() {
        return false;
    }

    @Override
    protected boolean loadSelectedTowns() {
        return false;
    }

    @Override
    protected boolean loadFriends() {
        return false;
    }

    @Override
    protected boolean loadFriendRequests() {
        return false;
    }

    @Override
    protected boolean loadTownInvites() {
        return false;
    }

    @Override
    protected boolean loadBlockOwners() {
        return false;
    }

    @Override
    protected boolean loadTownBanks() {
        return false;
    }

    @Override
    public boolean saveTown(Town town) {
        log.debug("Saving Town %s", town.getName());
        if (MyTownUniverse.getInstance().hasTown(town)) { // Update
            if (town.getOldName() != null) { // Rename
                MyTownUniverse.getInstance().removeTown(town);
                MyTownUniverse.getInstance().addTown(town);
                town.resetOldName();
            }
        } else { // Insert
            MyTownUniverse.getInstance().addTown(town);
        }
        return true;
    }

    @Override
    public boolean saveBlock(TownBlock block) {
        log.debug("Saving Block %s", block.getKey());
        if (MyTownUniverse.getInstance().hasTownBlock(block)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().addTownBlock(block);
        }
        return true;
    }

    @Override
    public boolean saveRank(Rank rank, boolean isDefault) {
        log.debug("Saving Rank %s", rank.getKey());
        if (MyTownUniverse.getInstance().hasRank(rank)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().addRank(rank);
        }
        return true;
    }

    @Override
    public boolean saveRankPermission(Rank rank, String perm) {
        return false;
    }

    @Override
    public boolean saveResident(Resident resident) {
        log.debug("Saving Resident %s (%s)", resident.getPlayerName(), resident.getUUID().toString());
        if (MyTownUniverse.getInstance().hasResident(resident)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().addResident(resident);
        }
        return true;
    }

    @Override
    public boolean savePlot(Plot plot) {
        log.debug("Saving Plot %s", plot.getKey());
        if (MyTownUniverse.getInstance().hasPlot(plot)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().addPlot(plot);
        }
        return true;
    }

    @Override
    public boolean saveNation(Nation nation) {
        return false;
    }

    @Override
    public boolean saveFlag(Flag flag, Town town) {
        return false;
    }

    @Override
    public boolean saveFlag(Flag flag, Plot plot) {
        return false;
    }

    @Override
    public boolean saveBlockWhitelist(BlockWhitelist bw, Town town) {
        return false;
    }

    @Override
    public boolean saveSelectedTown(Resident res, Town town) {
        return false;
    }

    @Override
    public boolean saveFriendLink(Resident res1, Resident res2) {
        return false;
    }

    @Override
    public boolean saveFriendRequest(Resident res1, Resident res2) {
        return false;
    }

    @Override
    public boolean saveTownInvite(Resident res, Town town) {
        return false;
    }

    @Override
    public boolean saveWorld(int dim) {
        return false;
    }

    @Override
    public boolean saveBlockOwner(Resident res, int dim, int x, int y, int z) {
        return false;
    }

    @Override
    public boolean saveTownBank(Town town, int amount, int daysNotPaid) {
        return false;
    }


    /*

    @Override
    public boolean saveNation(Nation nation) {
        log.debug("Saving Nation %s", nation.getName());
        if (MyTownUniverse.getInstance().nations.containsValue(nation)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().nations.put(nation.getName(), nation);
        }
        return true;
    }

    @Override
    public boolean saveFlag(Flag flag, Plot plot) {
        log.debug("Saving Flag %s for plot:", flag.getName(), plot.getKey());
        if (plot.hasFlag(flag.getName())) { // Update
        } else { // Insert
            plot.addFlag(flag);
        }
        return true;
    }


    @Override
    public boolean saveBlockWhitelist(BlockWhitelist bw, Town town) {
        return false;
    }

    @Override
    public boolean saveSelectedTown(Resident res, Town town) {
        return false;
    }

    @Override
    public boolean saveFlag(Flag flag, Town town) {
        log.debug("Saving Flag %s for town:", flag.getName(), town.getName());
        if (town.hasFlag(flag.getName())) { // Update
        } else { // Insert
            town.addFlag(flag);
        }
        return true;
    }
    */

    /* ----- Link ----- */

    @Override
    public boolean linkResidentToTown(Resident res, Town town, Rank rank) {
        return true;
    }

    @Override
    public boolean unlinkResidentFromTown(Resident res, Town town) {
        return true;
    }

    @Override
    public boolean updateResidentToTownLink(Resident res, Town town, Rank rank) {
        return false;
    }

    @Override
    public boolean linkTownToNation(Town town, Nation nation) {
        return true;
    }

    @Override
    public boolean unlinkTownFromNation(Town town, Nation nation) {
        return true;
    }

    @Override
    public boolean updateTownToNationLink(Town town, Nation nation) {
        return true;
    }

    @Override
    public boolean linkResidentToPlot(Resident res, Plot plot, boolean isOwner) {
        return false;
    }

    @Override
    public boolean unlinkResidentFromPlot(Resident res, Plot plot) {
        return false;
    }

    @Override
    public boolean updateResidentToPlotLink(Resident res, Plot plot, boolean isOwner) {
        return false;
    }

    @Override
    public boolean updateTownBank(Town town, int amount) {
        return false;
    }

    @Override
    public boolean deleteTown(Town town) {
        log.debug("Deleting Town %s", town);
        return MyTownUniverse.getInstance().removeTown(town);
    }

    @Override
    public boolean deleteBlock(TownBlock townBlock) {
        log.debug("Deleting Block %s", townBlock.getKey());
        return MyTownUniverse.getInstance().removeTownBlock(townBlock);
    }

    @Override
    public boolean deleteRank(Rank rank) {
        log.debug("Deleting Rank %s", rank.getKey());
        return MyTownUniverse.getInstance().removeRank(rank);
    }

    @Override
    public boolean deleteResident(Resident resident) {
        log.debug("Deleting Resident %s (%s)", resident.getPlayerName(), resident.getUUID().toString());
        return MyTownUniverse.getInstance().removeResident(resident);
    }

    @Override
    public boolean deletePlot(Plot plot) {
        log.debug("Deleting Plot %s", plot.getKey());
        return MyTownUniverse.getInstance().removePlot(plot);
    }

    @Override
    public boolean deleteNation(Nation nation) {
        log.debug("Deleting Nation %s", nation.getName());
        return MyTownUniverse.getInstance().removeNation(nation);
    }

    @Override
    public boolean deleteFlag(Flag flag, Town town) {
        return false;
    }

    @Override
    public boolean deleteFlag(Flag flag, Plot plot) {
        return false;
    }

    @Override
    public boolean deleteBlockWhitelist(BlockWhitelist bw, Town town) {
        return false;
    }

    @Override
    public boolean deleteSelectedTown(Resident res) {
        return false;
    }

    @Override
    public boolean deleteFriendLink(Resident res1, Resident res2) {
        return false;
    }

    @Override
    public boolean deleteFriendRequest(Resident res1, Resident res2, boolean response) {
        return false;
    }

    @Override
    public boolean deleteTownInvite(Resident res, Town town, boolean response) {
        return false;
    }

    @Override
    public boolean deleteWorld(int dim) {
        return false;
    }

    @Override
    public boolean removeRankPermission(Rank rank, String perm) {
        return false;
    }

    @Override
    public boolean deleteAllBlockOwners() {
        return false;
    }

    @Override
    protected boolean checkFlags() {
        return false;
    }

    @Override
    protected boolean checkTowns() {
        return false;
    }
}
