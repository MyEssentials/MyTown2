package mytown.datasource;

import com.mojang.authlib.GameProfile;
import mytown.MyTown;
import mytown.api.events.*;
import mytown.config.Config;
import mytown.core.utils.Log;
import mytown.core.utils.teleport.Teleport;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.economy.shop.Shop;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.UUID;

/**
 * @author Joe Goett
 */
public abstract class MyTownDatasource {
    protected Log log = null;

    public final void setLog(Log log) {
        this.log = log;
    }

    /**
     * Initialize the Datasource.
     * This should create a connection to the database.
     *
     * @return If false is returned, MyTown is put into safe-mode
     */
    public abstract boolean initialize();

    /* ----- Create ----- */

    /**
     * Creates and returns a new Town with basic entities saved to db, or null if it couldn't be created
     *
     * @return The new Town, or null if it failed
     */
    public final Town newTown(String name, Resident creator) {
        Town town = new Town(name);
        configureTown(town, creator);
        return town;
    }

    /**
     * Creates and returns a new AdminTown and fires event
     */
    public final AdminTown newAdminTown(String name, Resident creator) {
        AdminTown town = new AdminTown(name);
        configureTown(town, creator);
        return town;
    }

    /**
     * Common method for creating any type of town
     */
    @SuppressWarnings("unchecked")
    private void configureTown(Town town, Resident creator) {
        for (World world : MinecraftServer.getServer().worldServers) {
            if (!MyTownUniverse.getInstance().hasWorld(world.provider.dimensionId)) {
                saveWorld(world.provider.dimensionId);
            }
        }
        for (int dim : MyTownUniverse.getInstance().getWorldsList()) {
            if (DimensionManager.getWorld(dim) == null) {
                deleteWorld(dim);
            }
        }

        Rank onCreationDefaultRank = null;

        // Setting spawn before saving
        town.setSpawn(new Teleport(creator.getPlayer().dimension, (float) creator.getPlayer().posX, (float) creator.getPlayer().posY, (float) creator.getPlayer().posZ, creator.getPlayer().cameraYaw, creator.getPlayer().cameraPitch));

        // Saving town to database
        if (!saveTown(town))
            throw new CommandException("Failed to save Town"); // TODO Localize!


        //Claiming first block
        TownBlock block = newBlock(creator.getPlayer().dimension, creator.getPlayer().chunkCoordX, creator.getPlayer().chunkCoordZ, town);
        // Saving block to db and town
        saveBlock(block);

        // Saving and adding all flags to the database
        for (FlagType type : FlagType.values()) {
            if (type.canTownsModify()) {
                saveFlag(new Flag(type, type.getDefaultValue()), town);
            }
        }

        if (!(town instanceof AdminTown)) {
            // Saving all ranks to database and town
            for (String rankName : Rank.defaultRanks.keySet()) {
                Rank rank = new Rank(rankName, Rank.defaultRanks.get(rankName), town);

                saveRank(rank, rankName.equals(Rank.theDefaultRank));

                if (rankName.equals(Rank.theMayorDefaultRank)) {
                    onCreationDefaultRank = rank;
                }
            }
            // Linking resident to town
            if (!linkResidentToTown(creator, town, onCreationDefaultRank))
                MyTown.instance.log.error("Problem linking resident " + creator.getPlayerName() + " to town " + town.getName());

            saveTownBank(town, Config.defaultBankAmount, 0);
        }

        TownEvent.fire(new TownEvent.TownCreateEvent(town));
    }

    /**
     * Creates and returns a new Block, or null if it couldn't be created
     *
     * @return The new Block, or null if it failed
     */
    public final TownBlock newBlock(int dim, int x, int z, Town town) {
        TownBlock block = new TownBlock(dim, x, z, town);
        if (TownBlockEvent.fire(new TownBlockEvent.BlockCreateEvent(block)))
            return null;
        return block;
    }

    /**
     * Creates and returns a new Rank, or null if it couldn't be created
     *
     * @return The new Rank, or null if it failed
     */
    public final Rank newRank(String name, Town town) {
        Rank rank = new Rank(name, town);
        if (RankEvent.fire(new RankEvent.RankCreateEvent(rank)))
            return null;
        return rank;
    }

    /**
     * Creates and returns a new Resident, or null if it couldn't be created
     *
     * @return The new Resident, or null if it failed
     */
    public final Resident newResident(String uuid, String playerName) {
        Resident resident = new Resident(uuid, playerName);

        if (ResidentEvent.fire(new ResidentEvent.ResidentCreateEvent(resident)))
            return null;
        return resident;
    }

    /**
     * Creates and returns a new Plot, or null if it couldn't be created
     *
     * @return The new Plot, or null if it failed
     */
    public final Plot newPlot(String name, Town town, int dim, int x1, int y1, int z1, int x2, int y2, int z2) {
        Plot plot = new Plot(name, town, dim, x1, y1, z1, x2, y2, z2);
        if (PlotEvent.fire(new PlotEvent.PlotCreateEvent(plot)))
            return null;
        return plot;
    }

    /**
     * Creates and returns a new Nation, or null if it couldn't be created
     *
     * @return The new Nation, or null if it failed
     */
    public final Nation newNation(String name) {
        Nation nation = new Nation(name);
        if (NationEvent.fire(new NationEvent.NationCreateEvent(nation)))
            return null;
        return nation;
    }

    /**
     * Creates and returns a new TownFlag or null if it couldn't be created
     *
     * @return the new TownFlag, or null if failed
     */
    public final Flag newFlag(FlagType type, Object value) {
        Flag<Object> flag = new Flag<Object>(type, value);
        //TODO: Fire event
        return flag;
    }

    /* ----- Read ----- */
    /**
     * Loads all the towns, ranks, blocks, residents, plots, and nations. In that order.
     *
     * @return If successfully loaded
     */
    public boolean loadAll() { // TODO Change load order?
        return loadWorlds() && loadTowns() && loadRanks() && loadBlocks() && loadResidents() && loadPlots() && loadNations() && loadTownFlags() && loadPlotFlags() && loadBlockWhitelists() && loadSelectedTowns() && loadFriends() && loadFriendRequests() && loadTownInvites() && loadBlockOwners() && loadTownBanks() && loadShops();
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

    protected abstract boolean loadNations();

    protected abstract boolean loadTownFlags();

    protected abstract boolean loadPlotFlags();

    protected abstract boolean loadBlockWhitelists();

    protected abstract boolean loadSelectedTowns();

    protected abstract boolean loadFriends();

    protected abstract boolean loadFriendRequests();

    protected abstract boolean loadTownInvites();

    protected abstract boolean loadBlockOwners();

    protected abstract boolean loadTownBanks();

    protected abstract boolean loadShops();

    /* ----- Save ----- */

    public abstract boolean saveTown(Town town);

    public abstract boolean saveBlock(TownBlock block);

    public abstract boolean saveRank(Rank rank, boolean isDefault);

    public abstract boolean saveRankPermission(Rank rank, String perm);

    public abstract boolean saveResident(Resident resident);

    public abstract boolean savePlot(Plot plot);

    public abstract boolean saveNation(Nation nation);

    public abstract boolean saveFlag(Flag flag, Town town);

    public abstract boolean saveFlag(Flag flag, Plot plot);

    public abstract boolean saveBlockWhitelist(BlockWhitelist bw, Town town);

    public abstract boolean saveSelectedTown(Resident res, Town town);

    public abstract boolean saveFriendLink(Resident res1, Resident res2);

    public abstract boolean saveFriendRequest(Resident res1, Resident res2);

    public abstract boolean saveTownInvite(Resident res, Town town);

    public abstract boolean saveWorld(int dim);

    public abstract boolean saveBlockOwner(Resident res, int dim, int x, int y, int z);

    public abstract boolean saveTownBank(Town town, int amount, int daysNotPaid);

    public abstract boolean saveShop(Shop shop);

    /* ----- Link ----- */

    /**
     * Links the Resident to the Town, setting the Rank of the Resident in the Town
     */
    public abstract boolean linkResidentToTown(Resident res, Town town, Rank rank);

    public abstract boolean unlinkResidentFromTown(Resident res, Town town);

    public abstract boolean updateResidentToTownLink(Resident res, Town town, Rank rank);

    /**
     * Links the Resident to the Town, setting the Rank of the Resident in the Town
     */
    public abstract boolean linkTownToNation(Town town, Nation nation);

    /**
     * Unlinks the Resident from the Town
     */
    public abstract boolean unlinkTownFromNation(Town town, Nation nation);

    /**
     * Updates the link between the Town and Nation
     */
    public abstract boolean updateTownToNationLink(Town town, Nation nation);

    public abstract boolean linkResidentToPlot(Resident res, Plot plot, boolean isOwner);

    public abstract boolean unlinkResidentFromPlot(Resident res, Plot plot);

    public abstract boolean updateResidentToPlotLink(Resident res, Plot plot, boolean isOwner);

    public abstract boolean updateTownBank(Town town, int amount);

    /* ----- Delete ----- */

    public abstract boolean deleteTown(Town town);

    public abstract boolean deleteBlock(TownBlock block);

    public abstract boolean deleteRank(Rank rank);

    public abstract boolean deleteResident(Resident resident);

    public abstract boolean deletePlot(Plot plot);

    public abstract boolean deleteNation(Nation nation);

    public abstract boolean deleteFlag(Flag flag, Town town);

    public abstract boolean deleteFlag(Flag flag, Plot plot);

    public abstract boolean deleteBlockWhitelist(BlockWhitelist bw, Town town);

    public abstract boolean deleteShop(int id);

    /**
     * Deletes a town that was selected previously
     * Not extremely useful, the selected town is changed when saving another on top
     *
     * @param res
     * @return
     */
    public abstract boolean deleteSelectedTown(Resident res);

    public abstract boolean deleteFriendLink(Resident res1, Resident res2);

    public abstract boolean deleteFriendRequest(Resident res1, Resident res2, boolean response);

    /**
     * Deletes a town invite with the response to whether they should be added to town or not
     */
    public abstract boolean deleteTownInvite(Resident res, Town town, boolean response);

    public abstract boolean deleteWorld(int dim);

    public abstract boolean removeRankPermission(Rank rank, String perm);

    /**
     * Deletes everything from the BlockOwners table. No specific deletion is needed since coordinates are variable.
     */
    public abstract boolean deleteAllBlockOwners();

    /* ----- Checks ------ */

    public boolean checkAllOnStart() {
        return checkFlags() && checkTowns();
    }
    public boolean checkAllOnStop() { return checkFlags() && checkTowns() && checkShops(); }

    /**
     * Checks the flags on each town and plot. Makes sure that all the desired flagtypes are in them, if not it's gonna add them.
     * Same with having undesired ones.
     */
    protected abstract boolean checkFlags();

    /**
     * Checks whether or not the town has a default rank.
     */
    protected abstract boolean checkTowns();

    /**
     * Checks if there are shops that do not exist anymore
     */
    protected abstract boolean checkShops();

    /* ----- Has ----- */

    public final boolean hasTown(String townName) {
        return MyTownUniverse.getInstance().getTown(townName) != null;
    }

    /**
     * Checks if the Block exists give the chunk coordonates and dimension
     */
    public final boolean hasBlock(int dim, int x, int z) {
        return MyTownUniverse.getInstance().getTownBlock(String.format(TownBlock.keyFormat, dim, x, z)) != null;
    }

    /**
     * Checks if the TownBlock with the given coords and dim at the town specified exists
     */
    public final boolean hasBlock(int dim, int x, int z, Town town) {
        String key = String.format(TownBlock.keyFormat, dim, x, z);
        TownBlock b = MyTownUniverse.getInstance().getTownBlock(key);
        if (town != null && b != null && b.getTown() == town)
            return true;

        return false;
    }

    public final boolean hasResident(UUID uuid) {
        return MyTownUniverse.getInstance().getResident(uuid.toString()) != null;
    }

    public final boolean hasResident(EntityPlayer pl) {
        return hasResident(pl.getPersistentID());
    }

    public final boolean hasResident(ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
            return hasResident((EntityPlayer) sender);
        }
        return false;
    }

    public final boolean hasResident(String username) {
        GameProfile profile = MinecraftServer.getServer().func_152358_ax().func_152655_a(username); // TODO I have no idea if this will actually work. xD
        return profile != null && hasResident(profile.getId());
    }

    /* ----- Helper ----- */

    /**
     * Gets or makes a new Resident, optionally saving it. CAN return null!
     *
     * @param uuid The UUID of the Resident (EntityPlayer#getPersistentID())
     * @param save Whether to save the newly created Resident
     * @return The new Resident, or null if it failed
     */
    public Resident getOrMakeResident(UUID uuid, String playerName, boolean save) {
        Resident res = MyTownUniverse.getInstance().getResident(uuid.toString());
        if (res == null) {
            res = newResident(uuid.toString(), playerName);
            if (save && res != null) { // Only save if a new Resident
                if (!saveResident(res)) { // If saving fails, return null
                    return null;
                }
            }
        }
        return res;
    }

    /**
     * Gets or makes a new Resident. Does save, and CAN return null!
     *
     * @param uuid The UUID of the Resident (EntityPlayer#getPersistentID())
     * @return The new Resident, or null if it failed
     */
    public Resident getOrMakeResident(UUID uuid, String playerName) {
        return getOrMakeResident(uuid, playerName, true);
    }

    public Resident getOrMakeResident(EntityPlayer player) {
        return getOrMakeResident(player.getPersistentID(), player.getDisplayName());
    }

    public Resident getOrMakeResident(Entity e) {
        if (e instanceof EntityPlayer) {
            return getOrMakeResident((EntityPlayer) e);
        }
        return null;
    }

    public Resident getOrMakeResident(ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
            return getOrMakeResident((EntityPlayer) sender);
        }
        return null;
    }

    public Resident getOrMakeResident(String username) {
        GameProfile profile = MinecraftServer.getServer().func_152358_ax().func_152655_a(username);
        return profile == null ? null : getOrMakeResident(profile.getId(), profile.getName());
    }

    public TownBlock getBlock(int dim, int chunkX, int chunkZ) {
        return MyTownUniverse.getInstance().getTownBlock(String.format(TownBlock.keyFormat, dim, chunkX, chunkZ));
    }

    public Rank getRank(String rankName, Town town) {
        for (Rank rank : MyTownUniverse.getInstance().getRanksMap().values()) {
            if (rank.getName().equals(rankName) && rank.getTown().equals(town))
                return rank;
        }
        return null;
    }
}
