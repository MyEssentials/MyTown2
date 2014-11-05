package mytown.datasource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import mytown.core.utils.command.CommandCompletion;
import mytown.entities.*;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author Joe Goett
 */
public class MyTownUniverse { // TODO Allow migrating between different Datasources
    private Map<String, Resident> residents;
    private Map<String, Town> towns;
    private Map<String, Nation> nations;
    private Map<String, TownBlock> blocks;
    private Map<Integer, Plot> plots;
    private Map<String, Rank> ranks;
    private List<Integer> worlds;

    //TODO: Some stuff are stored twice, fix it

    private MyTownUniverse() {
        residents = new Hashtable<String, Resident>();
        towns = new Hashtable<String, Town>();
        nations = new Hashtable<String, Nation>();
        blocks = new Hashtable<String, TownBlock>();
        plots = new Hashtable<Integer, Plot>();
        ranks = new Hashtable<String, Rank>();

        worlds = new ArrayList<Integer>();
    }

    /**
     * Returns an ImmutableMap of Residents
     *
     * @return ImmutableMap of Residents
     */
    public final ImmutableMap<String, Resident> getResidentsMap() {
        return ImmutableMap.copyOf(residents);
    }

    /**
     * Returns an ImmutableMap of Towns
     *
     * @return ImmutableMap of Towns
     */
    public final ImmutableMap<String, Town> getTownsMap() {
        return ImmutableMap.copyOf(towns);
    }

    /**
     * Returns an ImmutableMap of Nations
     *
     * @return ImmutableMap of Nations
     */
    public final ImmutableMap<String, Nation> getNationsMap() {
        return ImmutableMap.copyOf(nations);
    }

    /**
     * Returns an ImmutableMap of Blocks
     *
     * @return ImmutableMap of Blocks
     */
    public final ImmutableMap<String, TownBlock> getBlocksMap() {
        return ImmutableMap.copyOf(blocks);
    }

    /**
     * Returns an ImmutableMap of Plots
     *
     * @return ImmutableMap of Plots
     */
    public final ImmutableMap<Integer, Plot> getPlotsMap() {
        return ImmutableMap.copyOf(plots);
    }

    /**
     * Returns an ImmutableMap of Ranks
     *
     * @return ImmutableMap of Ranks
     */
    public final ImmutableMap<String, Rank> getRanksMap() {
        return ImmutableMap.copyOf(ranks);
    }

    /**
     * Returns a ImmutableList of all the worlds that appear in the database
     *
     * @return
     */
    public final ImmutableList<Integer> getWorldsList() { return ImmutableList.copyOf(worlds); }

    private static MyTownUniverse instance = null;

    public final boolean addResident(Resident res) {
        residents.put(res.getUUID().toString(), res);
        CommandCompletion.completionMap.get("residentCompletion").add(res.getPlayerName());
        return true;
    }

    public final boolean addTown(Town town) {
        towns.put(town.getName(), town);
        CommandCompletion.completionMap.get("townCompletionAndAll").add(town.getName());
        CommandCompletion.completionMap.get("townCompletion").add(town.getName());
        return true;
    }

    public final boolean addNation(Nation nation) {
        nations.put(nation.getName(), nation);
        return true;
    }

    public final boolean addTownBlock(TownBlock block) {
        blocks.put(block.getKey(), block);
        return true;
    }

    public final boolean addRank(Rank rank) {
        ranks.put(rank.getKey(), rank);
        CommandCompletion.completionMap.get("rankCompletion").add(rank.getName());
        return true;
    }

    public final boolean addPlot(Plot plot) {
        for (int x = plot.getStartChunkX(); x < plot.getEndChunkX(); x++) {
            for (int z = plot.getStartChunkZ(); z < plot.getEndChunkZ(); z++) {
                TownBlock b = getTownBlock(plot.getDim(), x, z);
                if (b != null) {
                    b.addPlot(plot);
                }
            }
        }

        plots.put(plot.getDb_ID(), plot);
        return true;
    }

    public final boolean addWorld(int dim) {
        worlds.add(dim);
        return true;
    }

    public final boolean removeResident(Resident res) {
        residents.remove(res.getUUID().toString());
        CommandCompletion.completionMap.get("residentCompletion").remove(res.getPlayerName());
        return true;
    }

    public final boolean removeTown(Town town) {
        towns.remove(town.getName());
        CommandCompletion.completionMap.get("townCompletionAndAll").remove(town.getName());
        CommandCompletion.completionMap.get("townCompletion").remove(town.getName());
        return true;
    }

    public final boolean removeNation(Nation nation) {
        nations.remove(nation.getName());
        return true;
    }

    public final boolean removeTownBlock(TownBlock block) {
        blocks.remove(block.getKey());
        return true;
    }

    public final boolean removeRank(Rank rank) {
        ranks.remove(rank.getKey());
        // TODO: Check properly, although it's gonna fix itself on restart
        //CommandCompletion.completionMap.get("rankCompletion").remove(rank.getName());
        return true;
    }

    public final boolean removePlot(Plot plot) {
        for (int x = plot.getStartChunkX(); x < plot.getEndChunkX(); x++) {
            for (int z = plot.getStartChunkZ(); z < plot.getEndChunkZ(); z++) {
                TownBlock b = getTownBlock(plot.getDim(), x, z);
                if (b != null) {
                    b.removePlot(plot);
                }
            }
        }

        plots.remove(plot.getDb_ID());
        return true;
    }

    public final boolean removeWorld(int dim) {
        worlds.remove((Integer)dim);
        return true;
    }

    public Resident getResident(String key) {
        return residents.get(key);
    }

    public Resident getResidentByName(String username) {
        GameProfile profile = MinecraftServer.getServer().func_152358_ax().func_152655_a(username);
        return getResident(profile.getId().toString());
    }

    public Town getTown(String key) {
        return towns.get(key);
    }

    public Nation getNation(String key) {
        return nations.get(key);
    }

    public TownBlock getTownBlock(int dim, int x, int z) {
        return getTownBlock(String.format(TownBlock.keyFormat, dim, x, z));
    }

    public TownBlock getTownBlock(String key) {
        return blocks.get(key);
    }

    public Rank getRank(String key) {
        return ranks.get(key);
    }

    public Plot getPlot(int key) {
        return plots.get(key);
    }

    public boolean hasResident(Resident res) {
        return residents.containsValue(res);
    }

    public boolean hasTown(Town town) {
        return towns.containsValue(town);
    }

    public boolean hasNation(Nation nation) {
        return nations.containsValue(nation);
    }

    public boolean hasTownBlock(TownBlock block) {
        return blocks.containsValue(block);
    }

    public boolean hasRank(Rank rank) {
        return ranks.containsValue(rank);
    }

    public boolean hasPlot(Plot plot) {
        return plots.containsValue(plot);
    }

    public boolean hasWorld(int dim) { return worlds.contains(dim); }

    public static MyTownUniverse getInstance() {
        if (instance == null) {
            instance = new MyTownUniverse();
        }
        return instance;
    }
}
