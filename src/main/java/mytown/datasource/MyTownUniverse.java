package mytown.datasource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import mytown.core.utils.command.CommandManager;
import mytown.entities.*;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Joe Goett
 */
public class MyTownUniverse { // TODO Allow migrating between different Datasources
    private Map<String, Resident> residents = new HashMap<String, Resident>();
    private Map<String, Town> towns = new HashMap<String, Town>();
    private Map<String, Nation> nations = new HashMap<String, Nation>();
    private Map<String, TownBlock> blocks = new HashMap<String, TownBlock>();
    private Map<Integer, Plot> plots = new HashMap<Integer, Plot>();
    private Map<String, Rank> ranks = new HashMap<String, Rank>();
    private List<Integer> worlds = new ArrayList<Integer>();

    public MyTownUniverse() {}

    public final ImmutableMap<String, Resident> getResidentsMap() {
        return ImmutableMap.copyOf(residents);
    }

    public final ImmutableMap<String, Town> getTownsMap() {
        return ImmutableMap.copyOf(towns);
    }

    public final ImmutableMap<String, Nation> getNationsMap() {
        return ImmutableMap.copyOf(nations);
    }

    public final ImmutableMap<String, TownBlock> getBlocksMap() {
        return ImmutableMap.copyOf(blocks);
    }

    public final ImmutableMap<Integer, Plot> getPlotsMap() {
        return ImmutableMap.copyOf(plots);
    }

    public final ImmutableMap<String, Rank> getRanksMap() {
        return ImmutableMap.copyOf(ranks);
    }

    public final ImmutableList<Integer> getWorldsList() {
        return ImmutableList.copyOf(worlds);
    }


    /* ----- Add Entity ----- */

    public final boolean addResident(Resident res) {
        residents.put(res.getUUID().toString(), res);
        CommandManager.completionMap.get("residentCompletion").add(res.getPlayerName());
        return true;
    }

    public final boolean addTown(Town town) {
        towns.put(town.getName(), town);
        CommandManager.completionMap.get("townCompletionAndAll").add(town.getName());
        CommandManager.completionMap.get("townCompletion").add(town.getName());
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
        CommandManager.completionMap.get("rankCompletion").add(rank.getName());
        return true;
    }

    public final boolean addPlot(Plot plot) {
        for (int x = plot.getStartChunkX(); x <= plot.getEndChunkX(); x++) {
            for (int z = plot.getStartChunkZ(); z <= plot.getEndChunkZ(); z++) {
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

    /* ----- Remove Entity ----- */

    public final boolean removeResident(Resident res) {
        residents.remove(res.getUUID().toString());
        CommandManager.completionMap.get("residentCompletion").remove(res.getPlayerName());
        return true;
    }

    public final boolean removeTown(Town town) {
        towns.remove(town.getName());
        CommandManager.completionMap.get("townCompletionAndAll").remove(town.getName());
        CommandManager.completionMap.get("townCompletion").remove(town.getName());
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
        //CommandManager.completionMap.get("rankCompletion").remove(rank.getName());
        return true;
    }

    public final boolean removePlot(Plot plot) {
        plots.remove(plot.getDb_ID());
        return true;
    }

    public final boolean removeWorld(int dim) {
        worlds.remove((Integer) dim);
        return true;
    }

    /* ----- Get Entity ----- */

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

    /* ----- Has Entity ----- */

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

    public boolean hasWorld(int dim) {
        return worlds.contains(dim);
    }

    /* ----- Singleton ----- */

    private static MyTownUniverse instance = null;

    public static MyTownUniverse getInstance() {
        if (instance == null) {
            instance = new MyTownUniverse();
        }
        return instance;
    }
}
