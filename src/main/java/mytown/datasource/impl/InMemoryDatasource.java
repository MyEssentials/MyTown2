package mytown.datasource.impl;

import mytown.datasource.MyTownDatasource;
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
        if (towns.containsValue(town)) { // Update
            if (town.getOldName() != null) { // Rename
                towns.remove(town.getOldName());
                towns.put(town.getName(), town);
                town.resetOldName();
            }
        } else { // Insert
            towns.put(town.getName(), town);
        }
        return true;
    }

    @Override
    public boolean saveBlock(Block block) {
        log.debug("Saving Block %s", block.getKey());
        if (blocks.containsValue(block)) { // Update
        } else { // Insert
            blocks.put(block.getKey(), block);
        }
        return true;
    }

    @Override
    public boolean saveRank(Rank rank) {
        log.debug("Saving Rank %s", rank.getKey());
        if (ranks.containsValue(rank)) { // Update
        } else { // Insert
            ranks.put(rank.getKey(), rank);
        }
        return true;
    }

    @Override
    public boolean saveResident(Resident resident) {
        log.debug("Saving Resident %s (%s)", resident.getPlayerName(), resident.getUUID().toString());
        if (residents.containsValue(resident)) { // Update
        } else { // Insert
            residents.put(resident.getUUID().toString(), resident);
        }
        return true;
    }

    @Override
    public boolean savePlot(Plot plot) {
        log.debug("Saving Plot %s", plot.getKey());
        if (plots.containsValue(plot)) { // Update
        } else { // Insert
            plots.put(plot.getKey(), plot);
        }
        return true;
    }

    @Override
    public boolean saveNation(Nation nation) {
        log.debug("Saving Nation %s", nation.getName());
        if (nations.containsValue(nation)) { // Update
        } else { // Insert
            nations.put(nation.getName(), nation);
        }
        return true;
    }

    @Override
    public boolean deleteTown(Town town) {
        log.debug("Deleting Town %s", town);
        return towns.remove(town.getName()) != null;
    }

    @Override
    public boolean deleteBlock(Block block) {
        log.debug("Deleting Block %s", block.getKey());
        return blocks.remove(block.getKey()) != null;
    }

    @Override
    public boolean deleteRank(Rank rank) {
        log.debug("Deleting Rank %s", rank.getKey());
        return ranks.remove(rank.getKey()) != null;
    }

    @Override
    public boolean deleteResident(Resident resident) {
        log.debug("Deleting Resident %s (%s)", resident.getPlayerName(), resident.getUUID().toString());
        return residents.remove(resident.getUUID().toString()) != null;
    }

    @Override
    public boolean deletePlot(Plot plot) {
        log.debug("Deleting Plot %s", plot.getKey());
        return plots.remove(plot.getKey()) != null;
    }

    @Override
    public boolean deleteNation(Nation nation) {
        log.debug("Deleting Nation %s", nation.getName());
        return nations.remove(nation.getName()) != null;
    }
}
