package mytown.datasource;

import com.mojang.authlib.GameProfile;
import mytown.MyTown;
import mytown.api.events.*;
import mytown.core.utils.Log;
import mytown.core.utils.teleport.Teleport;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

/**
 * @author Joe Goett
 */
public abstract class MyTownDatasource {
    protected Log log = null;

    /**
     * Sets the Log the Datasource uses
     *
     * @param log The log...
     */
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

    // TODO: Delete this at one point
    /**
     * Creates and returns a new Town, or null if it couldn't be created
     *
     * @return The new Town, or null if it failed
     */
    /*
    public final Town newTown(String name) {
        Town town = new Town(name);
        if (TownEvent.fire(new TownEvent.TownCreateEvent(town)))
            return null;
        return town;
    }
    */

    /**
     * Creates and returns a new Town with basic entities saved to db, or null if it couldn't be created
     *
     * @return The new Town, or null if it failed
     */
    public final Town newTown(String name, Resident creator) {
        Town town = new Town(name);

        Rank onCreationDefaultRank = null;

        // Setting spawn before saving
        town.setSpawn(new Teleport(creator.getPlayer().dimension, (float)creator.getPlayer().posX, (float)creator.getPlayer().posY, (float)creator.getPlayer().posZ, creator.getPlayer().cameraYaw, creator.getPlayer().cameraPitch));

        // Saving town to database
        if (!saveTown(town))
            throw new CommandException("Failed to save Town"); // TODO Localize!

        // Saving all ranks to database and town
        for(String rankName : Rank.defaultRanks.keySet()) {
            Rank rank = new Rank(rankName, Rank.defaultRanks.get(rankName), town);

            saveRank(rank, rankName.equals(Rank.theDefaultRank));

            if(rankName.equals(Rank.theMayorDefaultRank)) {
                onCreationDefaultRank = rank;
            }
        }

        // Linking resident to town
        if(!linkResidentToTown(creator, town, onCreationDefaultRank))
            MyTown.instance.log.error("Problem linking resident " + creator.getPlayerName() + " to town " + town.getName());

        //Claiming first block
        TownBlock block = newBlock(creator.getPlayer().dimension, creator.getPlayer().chunkCoordX, creator.getPlayer().chunkCoordZ, town);
        // Saving block to db and town
        saveBlock(block);

        // Saving and adding all flags to the database
        saveFlag(new Flag<Boolean>(FlagType.enter, false), town);
        saveFlag(new Flag<Boolean>(FlagType.breakBlocks, false), town);
        saveFlag(new Flag<Boolean>(FlagType.explosions, false), town);
        saveFlag(new Flag<Boolean>(FlagType.accessBlocks, false), town);
        saveFlag(new Flag<Boolean>(FlagType.activateBlocks, false), town);
        saveFlag(new Flag<Boolean>(FlagType.useItems, false), town);
        saveFlag(new Flag<Boolean>(FlagType.pickupItems, true), town);
        saveFlag(new Flag<String>(FlagType.mobs, "all"), town);
        saveFlag(new Flag<Boolean>(FlagType.attackEntities, false), town);
        saveFlag(new Flag<Boolean>(FlagType.placeBlocks, false), town);
        saveFlag(new Flag<Boolean>(FlagType.ic2EnergyFlow, false), town);
        saveFlag(new Flag<Boolean>(FlagType.bcBuildingMining, false), town);
        saveFlag(new Flag<Boolean>(FlagType.bcPipeFlow, false), town);

        if (TownEvent.fire(new TownEvent.TownCreateEvent(town)))
            return null;
        return town;
    }

    /**
     * Creates and returns a new AdminTown and fires event
     *
     * @param name
     * @param creator
     * @return
     */
    public final AdminTown newAdminTown(String name, Resident creator) {
        AdminTown town = new AdminTown(name);

        Rank onCreationDefaultRank = null;

        // Setting spawn before saving
        town.setSpawn(new Teleport(creator.getPlayer().dimension, (float)creator.getPlayer().posX, (float)creator.getPlayer().posY, (float)creator.getPlayer().posZ, creator.getPlayer().cameraYaw, creator.getPlayer().cameraPitch));

        // Saving town to database
        if (!saveTown(town))
            throw new CommandException("Failed to save Town"); // TODO Localize!


        //Claiming first block
        TownBlock block = newBlock(creator.getPlayer().dimension, creator.getPlayer().chunkCoordX, creator.getPlayer().chunkCoordZ, town);
        // Saving block to db and town
        saveBlock(block);

        // Saving and adding all flags to the database
        saveFlag(new Flag<Boolean>(FlagType.enter, false), town);
        saveFlag(new Flag<Boolean>(FlagType.breakBlocks, false), town);
        saveFlag(new Flag<Boolean>(FlagType.explosions, false), town);
        saveFlag(new Flag<Boolean>(FlagType.accessBlocks, false), town);
        saveFlag(new Flag<Boolean>(FlagType.activateBlocks, false), town);
        saveFlag(new Flag<Boolean>(FlagType.useItems, false), town);
        saveFlag(new Flag<Boolean>(FlagType.pickupItems, true), town);
        saveFlag(new Flag<String>(FlagType.mobs, "all"), town);
        saveFlag(new Flag<Boolean>(FlagType.attackEntities, false), town);
        saveFlag(new Flag<Boolean>(FlagType.placeBlocks, false), town);
        // No need for checking if mod loaded FlagType and save method handles all that

        saveFlag(new Flag<Boolean>(FlagType.ic2EnergyFlow, false), town);
        saveFlag(new Flag<Boolean>(FlagType.bcBuildingMining, false), town);
        saveFlag(new Flag<Boolean>(FlagType.bcPipeFlow, false), town);


        if (TownEvent.fire(new TownEvent.TownCreateEvent(town)))
            return null;
        return town;
    }

    /**
     * Creates and returns a new Block, or null if it couldn't be created
     *
     * @return The new Block, or null if it failed
     */
    public final TownBlock newBlock(int dim, int x, int z, Town town) {
        TownBlock block = new TownBlock(dim, x, z, town);
        if (BlockEvent.fire(new BlockEvent.BlockCreateEvent(block)))
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
     * @param value
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
        return loadTowns() && loadRanks() && loadBlocks() && loadResidents() && loadPlots() && loadNations() && loadTownFlags() && loadPlotFlags() && loadBlockWhitelists() && loadSelectedTowns() && loadFriends();
    }

    /**
     * Loads all the Towns
     *
     * @return If it was successful
     */
    protected abstract boolean loadTowns();

    /**
     * Loads all the Blocks
     *
     * @return If it was successful
     */
    protected abstract boolean loadBlocks();

    /**
     * Loads all the Ranks
     *
     * @return If it was successful
     */
    protected abstract boolean loadRanks();

    /**
     * Loads all the Residents
     *
     * @return If it was successful
     */
    protected abstract boolean loadResidents();

    /**
     * Loads all the Plots
     *
     * @return If it was successful
     */
    protected abstract boolean loadPlots();

    /**
     * Loads all the Nations
     *
     * @return If it was successful
     */
    protected abstract boolean loadNations();

    /**
     * Loads all the Flags for the Towns
     *
     * @return If it was successful
     */
    protected abstract boolean loadTownFlags();

    /**
     * Loads all the Flags for the Plots
     *
     * @return
     */
    protected abstract boolean loadPlotFlags();

    /**
     * Loads all the BlockWhitelists
     *
     * @return
     */
    protected abstract boolean loadBlockWhitelists();

    /**
     * Loads the selected towns
     *
     * @return
     */
    protected abstract boolean loadSelectedTowns();

    /**
     * Loads the link between to residents which represents friendship
     *
     * @return
     */
    protected abstract boolean loadFriends();

    /**
     * Loads all the friend requests
     *
     * @return
     */
    protected abstract boolean loadFriendRequests();

    /* ----- Save ----- */

    /**
     * Saves the Town
     *
     * @return If it was successful
     */
    public abstract boolean saveTown(Town town);

    /**
     * Saves the Block
     *
     * @return If it was successful
     */
    public abstract boolean saveBlock(TownBlock block);

    /**
     * Saves the Rank
     *
     * @return If it was successful
     */
    public abstract boolean saveRank(Rank rank, boolean isDefault);

    /**
     * Adds the permission node to the Rank
     *
     * @param rank The rank
     * @param perm The permission node
     * @return Whether it was successfully added
     */
    public abstract boolean addRankPermission(Rank rank, String perm);

    /**
     * Saves the Resident
     *
     * @return If it was successful
     */
    public abstract boolean saveResident(Resident resident);

    /**
     * Saves the Plot
     *
     * @return If it was successful
     */
    public abstract boolean savePlot(Plot plot);

    /**
     * Saves the Nation
     *
     * @return If it was successful
     */
    public abstract boolean saveNation(Nation nation);

    /**
     * Saves the Flag
     *
     * @return If it was successful
     */
    public abstract boolean saveFlag(Flag flag, Town town);

    /**
     * Saves the Flag to the plot
     *
     * @return If it was successful
     */
    public abstract boolean saveFlag(Flag flag, Plot plot);

    /**
     * Saves the BlockWhitelist to the town
     *
     * @param bw
     * @param town
     * @return
     */
    public abstract boolean saveBlockWhitelist(BlockWhitelist bw, Town town);

    /**
     * Saves a link of the town with the resident
     *
     * @param res
     * @return
     */
    public abstract boolean saveSelectedTown(Resident res, Town town);

    /**
     * Saves a link between 2 residents which represents friendship
     *
     * @param res1
     * @param res2
     * @return
     */
    public abstract boolean saveFriendLink(Resident res1, Resident res2);

    /**
     * Saves a friend request to the database
     *
     * @param res1
     * @param res2
     * @return
     */
    public abstract boolean saveFriendRequest(Resident res1, Resident res2);


    /* ----- Link ----- */

    /**
     * Links the Resident to the Town, setting the Rank of the Resident in the Town
     *
     * @param res The Resident to Link
     * @param town The Town to Link
     * @param rank The Rank with which the resident is assigned to the town
     * @return If the link was successful
     */
    public abstract boolean linkResidentToTown(Resident res, Town town, Rank rank);

    /**
     * Unlinks the Resident from the Town
     *
     * @param res The Resident to Unlink
     * @param town The Town to Unlink
     * @return If the unlink was successful
     */
    public abstract boolean unlinkResidentFromTown(Resident res, Town town);

    /**
     * Updates the link between the Resident and the Town
     *
     * @param res The Resident
     * @param town the Town
     * @return If the link update was successful
     */
    public abstract boolean updateResidentToTownLink(Resident res, Town town, Rank rank);

    /**
     * Links the Resident to the Town, setting the Rank of the Resident in the Town
     *
     * @param town The Town to Link
     * @param nation The Nation to Link
     * @return If the link was successful
     */
    public abstract boolean linkTownToNation(Town town, Nation nation);

    /**
     * Unlinks the Resident from the Town
     *
     * @param town The Town to Unlink
     * @param nation The Nation to Unlink
     * @return If the unlink was successful
     */
    public abstract boolean unlinkTownFromNation(Town town, Nation nation);

    /**
     * Updates the link between the Town and Nation
     *
     * @param town The Town
     * @param nation The Nation
     * @return If the link update was successful
     */
    public abstract boolean updateTownToNationLink(Town town, Nation nation);

    /**
     * Links resident to plot as owner or not
     *
     * @param res
     * @param plot
     * @param isOwner
     * @return
     */
    public abstract boolean linkResidentToPlot(Resident res, Plot plot, boolean isOwner);

    /**
     * Unlink the Resident from the Plot
     *
     * @param res
     * @param plot
     * @return
     */
    public abstract boolean unlinkResidentFromPlot(Resident res, Plot plot);

    /**
     * Updates the link between the Resident and the Plot
     *
     * @param res
     * @param plot
     * @param isOwner
     * @return
     */
    public abstract boolean updateResidentToPlotLink(Resident res, Plot plot, boolean isOwner);

    /* ----- Delete ----- */

    /**
     * Deletes the Town
     *
     * @return If it was successful
     */
    public abstract boolean deleteTown(Town town);

    /**
     * Deletes the Block
     *
     * @return If it was successful
     */
    public abstract boolean deleteBlock(TownBlock block);

    /**
     * Deletes the Rank
     *
     * @return If it was successful
     */
    public abstract boolean deleteRank(Rank rank);

    /**
     * Deletes the Resident
     *
     * @return If it was successful
     */
    public abstract boolean deleteResident(Resident resident);

    /**
     * Deletes the Plot
     *
     * @return If it was successful
     */
    public abstract boolean deletePlot(Plot plot);



    /**
     * Deletes the Nation
     *
     * @return If it was successful
     */
    public abstract boolean deleteNation(Nation nation);

    // TODO: Decide whether or not we want these functions
    /**
     * Deletes the Flag from the given town
     *
     * @return If it was successful
     */
    //public abstract boolean deleteFlag(Flag flag, Town town);

    /**
     * Deletes the Flag from the given plot
     *
     * @param flag
     * @param plot
     * @return
     */
    //public abstract boolean deleteFlag(Flag flag, Plot plot);


    /**
     * Deletes the BlockWhitelist from the given town
     *
     * @param bw
     * @param town
     * @return
     */
    public abstract boolean deleteBlockWhitelist(BlockWhitelist bw, Town town);

    /**
     * Deletes a town that was selected previously
     * Not extremely useful, the selected town is changed when saving another on top
     *
     * @param res
     * @return
     */
    public abstract boolean deleteSelectedTown(Resident res);

    /**
     * Deletes the friend link between 2 residents
     * #whydidyouruinafriendshipsadface
     *
     * @param res1
     * @param res2
     * @return
     */
    public abstract boolean deleteFriendLink(Resident res1, Resident res2);

    /**
     * Deletes a friend request
     *
     * @param res1
     * @param res2
     * @return
     */
    public abstract boolean deleteFriendRequest(Resident res1, Resident res2, boolean response);
    /**
     * Removes the permission node from the Rank
     *
     * @param rank The rank
     * @param perm The permission node
     * @return Whether it was successfully removed
     */
    public abstract boolean removeRankPermission(Rank rank, String perm);

    /* ----- Has ----- */

    /**
     * Checks if the townName exists
     *
     * @param townName The name to check
     * @return If the name exists
     */
    public final boolean hasTown(String townName) {
        return MyTownUniverse.getInstance().towns.containsKey(townName);
    }

    /**
     * Checks if the Block exists
     *
     * @param dim The dimension to check in
     * @param x The x chunk coord to check at
     * @param z The z chunk coord to check at
     * @return If the Block exists
     */
    public final boolean hasBlock(int dim, int x, int z) {
        return MyTownUniverse.getInstance().blocks.containsKey(String.format(TownBlock.keyFormat, dim, x, z));
    }

    /**
     * Checks if the TownBlock with the given coords and dim at the town specified exists
     *
     * @param dim The dimension to check in
     * @param x The x coord to check at
     * @param z The z coord to check at
     * @param inChunkCoords true if x and z are in chunk coordinates, false otherwise
     * @return If the Block exists
     */
    public final boolean hasBlock(int dim, int x, int z, boolean inChunkCoords, Town town) {
        String key;
        if (inChunkCoords) {
            key = String.format(TownBlock.keyFormat, dim, x, z);
        } else {
            key = String.format(TownBlock.keyFormat, dim, x >> 4, z >> 4);
        }

        TownBlock b = MyTownUniverse.getInstance().blocks.get(key);
        if (town != null && b != null && b.getTown() == town)
            return true;

        return hasBlock(dim, x, z);
    }

    /**
     * Checks if the Datasource has a Resident with the given UUID
     *
     * @param uuid
     * @return
     */
    public final boolean hasResident(UUID uuid) {
        return MyTownUniverse.getInstance().residents.containsKey(uuid.toString());
    }

    /**
     * Checks if the Resident exists
     *
     * @param pl
     * @return
     */
    public final boolean hasResident(EntityPlayer pl) {
        return hasResident(pl.getPersistentID());
    }

    /**
     * Checks if the Resident exists
     *
     * @param sender
     * @return
     */
    public final boolean hasResident(ICommandSender sender) {
        if (sender instanceof EntityPlayer) {
            return hasResident((EntityPlayer) sender);
        }
        return false;
    }

    /**
     * Checks if the Datasource has a Resident with the given username
     *
     * @param username
     * @return
     */
    public final boolean hasResident(String username) {
        GameProfile profile = MinecraftServer.getServer().func_152358_ax().func_152655_a(username); // TODO I have no idea if this will actually work. xD
        return profile != null && hasResident(profile.getId());
        /*
        for (Resident res : residents.values()) {
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
        */
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
        Resident res = MyTownUniverse.getInstance().residents.get(uuid.toString());
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
        GameProfile profile = MinecraftServer.getServer().func_152358_ax().func_152655_a(username); // TODO I have no idea if this will actually work. xD
        return profile == null ? null : getOrMakeResident(profile.getId(), profile.getName());
    }

    /**
     * Returns the Block at the given location. Can return null if it doesn't exist!
     *
     * @param dim The dimension to check in
     * @param chunkX The chunk x to check at
     * @param chunkZ The chunk z to check at
     * @return The Block, or null if it doesn't exist
     */
    public TownBlock getBlock(int dim, int chunkX, int chunkZ) {
        return MyTownUniverse.getInstance().blocks.get(String.format(TownBlock.keyFormat, dim, chunkX, chunkZ));
    }

    /**
     * Returns the rank with the name and town specified
     *
     * @param rankName
     * @param town
     * @return
     */
    public Rank getRank(String rankName, Town town) {
        for(Rank rank : MyTownUniverse.getInstance().getRanksMap().values()) {
            if(rank.getName().equals(rankName) && rank.getTown().equals(town))
                return rank;
        }
        return null;
    }
}
