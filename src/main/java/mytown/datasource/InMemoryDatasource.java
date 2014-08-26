package mytown.datasource;

import mytown.entities.*;

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
    public boolean saveTown(Town town) {
        log.debug("Saving Town %s", town.getName());
        if (MyTownUniverse.getInstance().towns.containsValue(town)) { // Update
            if (town.getOldName() != null) { // Rename
                MyTownUniverse.getInstance().towns.remove(town.getOldName());
                MyTownUniverse.getInstance().towns.put(town.getName(), town);
                town.resetOldName();
            }
        } else { // Insert
            MyTownUniverse.getInstance().towns.put(town.getName(), town);
        }
        return true;
    }

    @Override
    public boolean saveBlock(Block block) {
        log.debug("Saving Block %s", block.getKey());
        if (MyTownUniverse.getInstance().blocks.containsValue(block)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().blocks.put(block.getKey(), block);
        }
        return true;
    }

    @Override
    public boolean saveRank(Rank rank) {
        log.debug("Saving Rank %s", rank.getKey());
        if (MyTownUniverse.getInstance().ranks.containsValue(rank)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().ranks.put(rank.getKey(), rank);
        }
        return true;
    }

    @Override
    public boolean addRankPermission(Rank rank, String perm) {
        return false;
    }

    @Override
    public boolean saveResident(Resident resident) {
        log.debug("Saving Resident %s (%s)", resident.getPlayerName(), resident.getUUID().toString());
        if (MyTownUniverse.getInstance().residents.containsValue(resident)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().residents.put(resident.getUUID().toString(), resident);
        }
        return true;
    }

    @Override
    public boolean savePlot(Plot plot) {
        log.debug("Saving Plot %s", plot.getKey());
        if (MyTownUniverse.getInstance().plots.containsValue(plot)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().plots.put(plot.getKey(), plot);
        }
        return true;
    }

    @Override
    public boolean saveNation(Nation nation) {
        log.debug("Saving Nation %s", nation.getName());
        if (MyTownUniverse.getInstance().nations.containsValue(nation)) { // Update
        } else { // Insert
            MyTownUniverse.getInstance().nations.put(nation.getName(), nation);
        }
        return true;
    }

    /* ----- Link ----- */

    @Override
    public boolean linkResidentToTown(Resident res, Town town) {
        return true;
    }

    @Override
    public boolean unlinkResidentToTown(Resident res, Town town) {
        return true;
    }

    @Override
    public boolean updateResidentToTownLink(Resident res, Town town) {
        return true;
    }

    @Override
    public boolean linkTownToNation(Town town, Nation nation) {
        return true;
    }

    @Override
    public boolean unlinkTownToNation(Town town, Nation nation) {
        return true;
    }

    @Override
    public boolean updateTownToNationLink(Town town, Nation nation) {
        return true;
    }

    @Override
    public boolean deleteTown(Town town) {
        log.debug("Deleting Town %s", town);
        return MyTownUniverse.getInstance().towns.remove(town.getName()) != null;
    }

    @Override
    public boolean deleteBlock(Block block) {
        log.debug("Deleting Block %s", block.getKey());
        return MyTownUniverse.getInstance().blocks.remove(block.getKey()) != null;
    }

    @Override
    public boolean deleteRank(Rank rank) {
        log.debug("Deleting Rank %s", rank.getKey());
        return MyTownUniverse.getInstance().ranks.remove(rank.getKey()) != null;
    }

    @Override
    public boolean deleteResident(Resident resident) {
        log.debug("Deleting Resident %s (%s)", resident.getPlayerName(), resident.getUUID().toString());
        return MyTownUniverse.getInstance().residents.remove(resident.getUUID().toString()) != null;
    }

    @Override
    public boolean deletePlot(Plot plot) {
        log.debug("Deleting Plot %s", plot.getKey());
        return MyTownUniverse.getInstance().plots.remove(plot.getKey()) != null;
    }

    @Override
    public boolean deleteNation(Nation nation) {
        log.debug("Deleting Nation %s", nation.getName());
        return MyTownUniverse.getInstance().nations.remove(nation.getName()) != null;
    }

    @Override
    public boolean removeRankPermission(Rank rank, String perm) {
        return false;
    }
}
