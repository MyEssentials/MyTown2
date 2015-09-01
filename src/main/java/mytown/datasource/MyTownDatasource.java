package mytown.datasource;

import com.mojang.authlib.GameProfile;
import mytown.MyTown;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public abstract class MyTownDatasource {
    protected static Logger LOG = MyTown.instance.LOG;

    /**
     * Initialize the Datasource.
     * This should create a connection to the database.
     */
    public abstract boolean initialize();

    /* ----- Read ----- */
    /**
     * Loads all the towns, ranks, blocks, residents, plots, and nations. In that order.
     */
    public boolean loadAll() {
        return loadWorlds() && loadTowns() && loadRanks() && loadBlocks() && loadResidents() &&
                loadPlots() && /*loadNations() &&*/ loadTownFlags() && loadPlotFlags() &&
                loadBlockWhitelists() && loadSelectedTowns() &&
                loadTownInvites() && loadBlockOwners() && loadTownBanks() &&
                loadRankPermissions() && loadResidentsToTowns() && /*loadTownsToNations() &&*/ loadResidentsToPlots();
    }

    /**
     * Loads all worlds, and saves to db if any are missing
     */
    protected abstract boolean loadWorlds();

    protected abstract boolean loadTowns();

    protected abstract boolean loadBlocks();

    protected abstract boolean loadRanks();

    protected abstract boolean loadResidents();

    protected abstract boolean loadPlots();
    /*
    protected abstract boolean loadNations();
    */
    protected abstract boolean loadTownFlags();

    protected abstract boolean loadPlotFlags();

    protected abstract boolean loadBlockWhitelists();

    protected abstract boolean loadTownInvites();

    protected abstract boolean loadBlockOwners();

    protected abstract boolean loadTownBanks();

    protected abstract boolean loadRankPermissions();

    protected abstract boolean loadResidentsToTowns();
    /*
    protected abstract boolean loadTownsToNations();
    */
    protected abstract boolean loadResidentsToPlots();

    protected abstract boolean loadSelectedTowns();


    /* ----- Save ----- */

    public abstract boolean saveTown(Town town);

    public abstract boolean saveBlock(TownBlock block);

    public abstract boolean saveRank(Rank rank);

    public abstract boolean saveRankPermission(Rank rank, String perm);

    public abstract boolean saveResident(Resident resident);

    public abstract boolean savePlot(Plot plot);
    /*
    public abstract boolean saveNation(Nation nation);
    */
    public abstract boolean saveFlag(Flag flag, Town town);

    public abstract boolean saveFlag(Flag flag, Plot plot);

    public abstract boolean saveBlockWhitelist(BlockWhitelist bw, Town town);

    public abstract boolean saveTownInvite(Resident res, Town town);

    public abstract boolean saveWorld(int dim);

    public abstract boolean saveBlockOwner(Resident res, int dim, int x, int y, int z);

    public abstract boolean saveTownBank(Bank bank);

    public abstract boolean saveSelectedTown(Resident res, Town town);

    /* ----- Link ----- */

    /**
     * Links the Resident to the Town, setting the Rank of the Resident in the Town
     */
    public abstract boolean linkResidentToTown(Resident res, Town town, Rank rank);

    public abstract boolean unlinkResidentFromTown(Resident res, Town town);

    public abstract boolean updateResidentToTownLink(Resident res, Town town, Rank rank);
    /*
    public abstract boolean linkTownToNation(Town town, Nation nation);

    public abstract boolean unlinkTownFromNation(Town town, Nation nation);

    public abstract boolean updateTownToNationLink(Town town, Nation nation);
    */
    public abstract boolean linkResidentToPlot(Resident res, Plot plot, boolean isOwner);

    public abstract boolean unlinkResidentFromPlot(Resident res, Plot plot);

    public abstract boolean updateResidentToPlotLink(Resident res, Plot plot, boolean isOwner);

    /* ----- Delete ----- */

    public abstract boolean deleteTown(Town town);

    public abstract boolean deleteBlock(TownBlock block);

    public abstract boolean deleteRank(Rank rank);

    public abstract boolean deleteResident(Resident resident);

    public abstract boolean deletePlot(Plot plot);
    /*
    public abstract boolean deleteNation(Nation nation);
    */
    public abstract boolean deleteFlag(Flag flag, Town town);

    public abstract boolean deleteFlag(Flag flag, Plot plot);

    public abstract boolean deleteBlockWhitelist(BlockWhitelist bw, Town town);

    /**
     * Deletes a town that was selected previously
     * Not extremely useful, the selected town is changed when saving another on top
     */
    public abstract boolean deleteSelectedTown(Resident res);
    /**
     * Deletes a town invite with the response to whether they should be added to town or not
     */
    public abstract boolean deleteTownInvite(Resident res, Town town, boolean response);

    public abstract boolean deleteWorld(int dim);

    public abstract boolean deleteRankPermission(Rank rank, String perm);

    /**
     * Deletes everything from the BlockOwners table. No specific deletion is needed since coordinates are variable.
     */
    public abstract boolean deleteAllBlockOwners();

    /* ----- Checks ------ */

    public boolean checkAllOnStart() {
        return checkFlags() && checkTowns();
    }
    public boolean checkAllOnStop() { return checkFlags() && checkTowns(); }

    /**
     * Checks the flags on each town and plot. Makes sure that all the desired flagtypes are in them, if not it's gonna add them.
     * Same with having undesired ones.
     */
    protected abstract boolean checkFlags();

    /**
     * Checks whether or not the town has a default rank.
     */
    protected abstract boolean checkTowns();

    /* ----- Reset ----- */

    public abstract boolean resetRanks(Town town);

    /* ----- Helper ----- */

    public MyTownUniverse getUniverse() {
        return MyTownUniverse.instance;
    }
}
