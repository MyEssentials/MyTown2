package mytown.datasource;

import com.mojang.authlib.GameProfile;
import myessentials.teleport.Teleport;
import myessentials.utils.PlayerUtils;
import mypermissions.api.command.CommandCompletion;
import mytown.MyTown;
import mytown.api.container.*;
import mytown.api.events.*;
import mytown.config.Config;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.handlers.VisualsHandler;
import mytown.proxies.DatasourceProxy;
import mytown.util.exceptions.MyTownCommandException;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MyTownUniverse { // TODO Allow migrating between different Datasources

    public static final MyTownUniverse instance = new MyTownUniverse();

    public final ResidentsContainer residents = new ResidentsContainer();
    public final TownsContainer towns = new TownsContainer();
    //public final Map<String, Nation> nations = new HashMap<String, Nation>();
    public final TownBlocksContainer blocks = new TownBlocksContainer();
    public final PlotsContainer plots = new PlotsContainer();
    public final RanksContainer ranks = new RanksContainer();
    public final BanksContainer banks = new BanksContainer();
    public final List<Integer> worlds = new ArrayList<Integer>();

    public MyTownUniverse() {

    }

    /* ----- Create Entity ----- */

    /**
     * Creates and returns a new Town with basic entities saved to db, or null if it couldn't be created
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
            if (!MyTownUniverse.instance.worlds.contains(world.provider.dimensionId)) {
                getDatasource().saveWorld(world.provider.dimensionId);
            }
        }
        /*
        for (int dim : MyTownUniverse.instance.getWorldsList()) {
            if (MinecraftServer.getServer().worldServerForDimension(dim) == null) {
                deleteWorld(dim);
            }
        }
        */

        // Setting spawn before saving
        town.setSpawn(new Teleport(creator.getPlayer().dimension, (float) creator.getPlayer().posX, (float) creator.getPlayer().posY, (float) creator.getPlayer().posZ, creator.getPlayer().cameraYaw, creator.getPlayer().cameraPitch));

        // Saving town to database
        if (!getDatasource().saveTown(town))
            throw new CommandException("Failed to save Town");

        //Claiming first block
        TownBlock block = newBlock(creator.getPlayer().dimension, ((int)creator.getPlayer().posX) >> 4, ((int)creator.getPlayer().posZ) >> 4, false, Config.costAmountClaim, town);

        // Saving block to db and town
        if(MyTownUniverse.instance.blocks.contains(creator.getPlayer().dimension, ((int) creator.getPlayer().posX) >> 4, ((int) creator.getPlayer().posZ) >> 4)) {
            throw new MyTownCommandException("mytown.cmd.err.claim.already");
        }

        getDatasource().saveBlock(block);

        // Saving and adding all flags to the database
        for (FlagType type : FlagType.values()) {
            if (type.canTownsModify()) {
                getDatasource().saveFlag(new Flag(type, type.getDefaultValue()), town);
            }
        }

        if (!(town instanceof AdminTown)) {
            // Saving all ranks to database and town
            for (Rank template : Rank.defaultRanks) {
                Rank rank = new Rank(template.getName(), town, template.getType());
                rank.permissionsContainer.addAll(template.permissionsContainer);

                getDatasource().saveRank(rank);
            }
            // Linking resident to town
            if (!getDatasource().linkResidentToTown(creator, town, town.ranksContainer.getMayorRank())) {
                MyTown.instance.LOG.error("Problem linking resident {} to town {}", creator.getPlayerName(), town.getName());
            }

            getDatasource().saveTownBank(town.bank);
        }

        TownEvent.fire(new TownEvent.TownCreateEvent(town));
    }

    /**
     * Creates and returns a new Block, or null if it couldn't be created
     */
    public final TownBlock newBlock(int dim, int x, int z, boolean isFarClaim, int pricePaid, Town town) {
        if(!worlds.contains(dim)) {
            getDatasource().saveWorld(dim);
        }

        TownBlock block = new TownBlock(dim, x, z, isFarClaim, pricePaid, town);
        if (TownBlockEvent.fire(new TownBlockEvent.BlockCreateEvent(block)))
            return null;
        return block;
    }

    /**
     * Creates and returns a new Rank, or null if it couldn't be created
     */
    public final Rank newRank(String name, Town town, Rank.Type type) {
        Rank rank = new Rank(name, town, type);
        if (RankEvent.fire(new RankEvent.RankCreateEvent(rank)))
            return null;
        return rank;
    }

    /**
     * Creates and returns a new Resident, or null if it couldn't be created
     */
    public final Resident newResident(UUID uuid, String playerName) {
        Resident resident = new Resident(uuid, playerName);

        if (ResidentEvent.fire(new ResidentEvent.ResidentCreateEvent(resident)))
            return null;
        return resident;
    }

    /**
     * Creates and returns a new Plot, or null if it couldn't be created
     */
    public final Plot newPlot(String name, Town town, int dim, int x1, int y1, int z1, int x2, int y2, int z2) {
        Plot plot = new Plot(name, town, dim, x1, y1, z1, x2, y2, z2);
        if (PlotEvent.fire(new PlotEvent.PlotCreateEvent(plot)))
            return null;
        return plot;
    }

    /**
     * Creates and returns a new Nation, or null if it couldn't be created
     */
    public final Nation newNation(String name) {
        Nation nation = new Nation(name);
        if (NationEvent.fire(new NationEvent.NationCreateEvent(nation)))
            return null;
        return nation;
    }

    /**
     * Creates and returns a new TownFlag or null if it couldn't be created
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public final Flag newFlag(FlagType type, Object value) {
        Flag<Object> flag = new Flag<Object>(type, value);
        //TODO: Fire event
        return flag;
    }

    public Resident getOrMakeResident(UUID uuid, String playerName, boolean save) {
        Resident res = instance.residents.get(uuid);
        if (res == null) {
            res = instance.newResident(uuid, playerName);
            if (save && res != null && !getDatasource().saveResident(res)) { // Only save if a new Residen
                return null;
            }
        }
        return res;
    }

    public Resident getOrMakeResident(UUID uuid, String playerName) {
        return getOrMakeResident(uuid, playerName, true);
    }

    public Resident getOrMakeResident(UUID uuid) {
        return getOrMakeResident(uuid, PlayerUtils.getUsernameFromUUID(uuid));
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

    /* ----- Modifying Entity */

    public final void renameTown(Town town, String newName) {
        String oldName = town.getName();
        town.rename(newName);
        getDatasource().saveTown(town);

        CommandCompletion.removeCompletion("townCompletion", oldName);
        CommandCompletion.removeCompletion("townCompletionAndAll", oldName);

        CommandCompletion.addCompletion("townCompletion", newName);
        CommandCompletion.addCompletion("townCompletionAndAll", newName);
    }

    /* ----- Add Entity ----- */

    public final void addResident(Resident res) {
        residents.add(res);
        CommandCompletion.addCompletion("residentCompletion", res.getPlayerName());
    }

    public final void addTown(Town town) {
        towns.add(town);
        CommandCompletion.addCompletion("townCompletionAndAll", town.getName());
        CommandCompletion.addCompletion("townCompletion", town.getName());
    }

    /*
    public final void addNation(Nation nation) {
        nations.put(nation.getName(), nation);
        return true;
    }
    */

    public final void addTownBlock(TownBlock block) {
        blocks.add(block);
    }

    public final void addRank(Rank rank) {
        ranks.add(rank);
        CommandCompletion.addCompletion("rankCompletion", rank.getName());
    }

    public final void addPlot(Plot plot) {
        for (int x = plot.getStartChunkX(); x <= plot.getEndChunkX(); x++) {
            for (int z = plot.getStartChunkZ(); z <= plot.getEndChunkZ(); z++) {
                TownBlock b = blocks.get(plot.getDim(), x, z);
                if (b != null) {
                    b.plotsContainer.add(plot);
                }
            }
        }
        plots.add(plot);
        plot.checkForSellSign();
        CommandCompletion.addCompletion("plotCompletion", plot.getName());
    }

    public final void addBank(Bank bank) {
        banks.add(bank);
    }


    public final void addWorld(int dim) {
        worlds.add(dim);
    }

    /* ----- Remove Entity ----- */

    public final void removeResident(Resident res) {
        residents.remove(res);
        CommandCompletion.removeCompletion("residentCompletion", res.getPlayerName());
    }

    public final void removeTown(Town town) {
        towns.remove(town);
        VisualsHandler.instance.unmarkBlocks(town);
        CommandCompletion.removeCompletion("townCompletionAndAll", town.getName());
        CommandCompletion.removeCompletion("townCompletion", town.getName());
    }

    /*
    public final void removeNation(Nation nation) {
        nations.remove(nation.getName());
    }
    */

    public final void removeTownBlock(TownBlock block) {
        blocks.remove(block);
    }

    public final void removeRank(Rank rank) {
        ranks.remove(rank);
        // TODO: Check properly, although it's gonna fix itself on restart
    }

    public final void removePlot(Plot plot) {
        plots.remove(plot);

        boolean removeFromCompletionMap = true;
        for(Plot p : plots) {
            if(p.getName().equals(plot.getName()))
                removeFromCompletionMap = false;
        }
        if(removeFromCompletionMap)
            CommandCompletion.removeCompletion("plotCompletion", plot.getName());

        VisualsHandler.instance.unmarkBlocks(plot);
    }

    public final void removeWorld(int dim) {
        worlds.remove((Integer) dim);
    }

    /* ----- Utils ----- */
    private MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}
