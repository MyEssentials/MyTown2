package mytown.datasource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import mypermissions.api.command.CommandCompletion;
import mytown.api.container.*;
import mytown.entities.*;
import mytown.handlers.VisualsHandler;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyTownUniverse { // TODO Allow migrating between different Datasources

    public static final MyTownUniverse instance = new MyTownUniverse();

    public final ResidentsContainer residents = new ResidentsContainer();
    public final TownsContainer towns = new TownsContainer();
    //public final Map<String, Nation> nations = new HashMap<String, Nation>();
    public final TownBlocksContainer blocks = new TownBlocksContainer();
    public final PlotsContainer plots = new PlotsContainer();
    public final RanksContainer ranks = new RanksContainer();
    public final List<Integer> worlds = new ArrayList<Integer>();

    public MyTownUniverse() {

    }

    /* ----- Add Entity ----- */

    public final boolean addResident(Resident res) {
        residents.add(res);
        CommandCompletion.addCompletion("residentCompletion", res.getPlayerName());
        return true;
    }

    public final boolean addTown(Town town) {
        towns.add(town);
        CommandCompletion.addCompletion("townCompletionAndAll", town.getName());
        CommandCompletion.addCompletion("townCompletion", town.getName());
        return true;
    }

    /*
    public final boolean addNation(Nation nation) {
        nations.put(nation.getName(), nation);
        return true;
    }
    */

    public final boolean addTownBlock(TownBlock block) {
        blocks.add(block);
        return true;
    }

    public final boolean addRank(Rank rank) {
        ranks.add(rank);
        CommandCompletion.addCompletion("rankCompletion", rank.getName());
        return true;
    }

    public final boolean addPlot(Plot plot) {
        for (int x = plot.getStartChunkX(); x <= plot.getEndChunkX(); x++) {
            for (int z = plot.getStartChunkZ(); z <= plot.getEndChunkZ(); z++) {
                TownBlock b = blocks.get(plot.getDim(), x, z);
                if (b != null) {
                    b.plotsContainer.add(plot);
                }
            }
        }
        plots.add(plot);
        CommandCompletion.addCompletion("plotCompletion", plot.getName());
        return true;
    }

    public final boolean addWorld(int dim) {
        worlds.add(dim);
        return true;
    }

    /* ----- Remove Entity ----- */

    public final boolean removeResident(Resident res) {
        residents.remove(res);
        CommandCompletion.removeCompletion("residentCompletion", res.getPlayerName());
        return true;
    }

    public final boolean removeTown(Town town) {
        towns.remove(town);
        VisualsHandler.instance.unmarkBlocks(town);
        CommandCompletion.removeCompletion("townCompletionAndAll", town.getName());
        CommandCompletion.removeCompletion("townCompletion", town.getName());
        return true;
    }

    /*
    public final boolean removeNation(Nation nation) {
        nations.remove(nation.getName());
        return true;
    }
    */

    public final boolean removeTownBlock(TownBlock block) {
        blocks.remove(block);
        return true;
    }

    public final boolean removeRank(Rank rank) {
        ranks.remove(rank);
        // TODO: Check properly, although it's gonna fix itself on restart
        return true;
    }

    public final boolean removePlot(Plot plot) {
        plots.remove(plot.getDbID());

        boolean removeFromCompletionMap = true;
        for(Plot p : plots) {
            if(p.getName().equals(plot.getName()))
                removeFromCompletionMap = false;
        }
        if(removeFromCompletionMap)
            CommandCompletion.removeCompletion("plotCompletion", plot.getName());

        VisualsHandler.instance.unmarkBlocks(plot);
        return true;
    }

    public final boolean removeWorld(int dim) {
        worlds.remove((Integer) dim);
        return true;
    }

    /* ----- Has Entity ----- */

    public boolean hasWorld(int dim) {
        return worlds.contains(dim);
    }
}
