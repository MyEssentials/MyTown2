package mytown.commands;

import com.google.common.collect.ImmutableList;
import mytown.api.interfaces.IFlagsContainer;
import myessentials.Localization;
import myessentials.command.CommandManager;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.EconomyProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.util.*;

/**
 * Base class for all classes that hold command methods... Mostly for some utils
 */
public abstract class Commands {

    protected Commands() {

    }

    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
    public static MyTownUniverse getUniverse() {
        return MyTownUniverse.instance;
    }
    public static Localization getLocal() {
        return LocalizationProxy.getLocalization();
    }

    /**
     * Custom check for commands which require certain rank permissions.
     */
    public static boolean firstPermissionBreach(String permission, ICommandSender sender) {
        // Since everybody should have permission to /t and outsider commands
        if ("mytown.cmd".equals(permission))
            return true;

        if (!(sender instanceof EntityPlayer))
            return true;

        Resident res = getDatasource().getOrMakeResident(sender);
        // Get its rank with the permissions
        Rank rank = res.getTownRank(res.getSelectedTown());

        if (rank == null) {
            return true;
        }
        return rank.hasPermissionOrSuperPermission(permission);
    }

    /**
     * Populates the tab completion map.
     */
    public static void populateCompletionMap() {
        List<String> populator = new ArrayList<String>();
        populator.addAll(getUniverse().getTownsMap().keySet());
        populator.add("@a");
        CommandManager.completionMap.put("townCompletionAndAll", populator);

        populator = new ArrayList<String>();
        populator.addAll(getUniverse().getTownsMap().keySet());
        CommandManager.completionMap.put("townCompletion", populator);

        populator = new ArrayList<String>();
        for (Resident res : getUniverse().getResidentsMap().values()) {
            populator.add(res.getPlayerName());
        }
        CommandManager.completionMap.put("residentCompletion", populator);

        populator = new ArrayList<String>();
        for (FlagType flag : FlagType.values()) {
            populator.add(flag.toString().toLowerCase());
        }
        CommandManager.completionMap.put("flagCompletion", populator);

        populator = new ArrayList<String>();
        for (FlagType flag : FlagType.values()) {
            if (flag.isWhitelistable())
                populator.add(flag.toString().toLowerCase());
        }
        CommandManager.completionMap.put("flagCompletionWhitelist", populator);

        populator = new ArrayList<String>();
        for(Plot plot : MyTownUniverse.instance.getPlotsMap().values()) {
            populator.add(plot.toString());
        }
        CommandManager.completionMap.put("plotCompletion", populator);

        populator = new ArrayList<String>();
        populator.addAll(Rank.defaultRanks.keySet());
        CommandManager.completionMap.put("rankCompletion", populator);
    }

    /* ---- HELPERS ---- */

    public static Town getTownFromResident(Resident res) {
        Town town = res.getSelectedTown();
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.partOfTown");
        return town;
    }

    public static Town getTownFromName(String name) {
        Town town = getUniverse().getTownsMap().get(name);
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.town.notexist", name);
        return town;
    }

    public static Resident getResidentFromName(String playerName) {
        Resident res = getDatasource().getOrMakeResident(playerName);
        if (res == null)
            throw new MyTownCommandException("mytown.cmd.err.resident.notexist", playerName);
        return res;
    }

    public static Plot getPlotAtResident(Resident res) {
        Town town = getTownFromResident(res);
        Plot plot = town.getPlotAtResident(res);
        if (plot == null)
            throw new MyTownCommandException("mytown.cmd.err.plot.notInPlot");
        return plot;
    }

    public static ImmutableList<Town> getInvitesFromResident(Resident res) {
        ImmutableList<Town> list = res.getInvites();
        if (list == null || list.isEmpty())
            throw new MyTownCommandException("mytown.cmd.err.invite.noinvitations");
        return list;
    }

    public static Flag getFlagFromType(IFlagsContainer hasFlags, FlagType flagType) {
        Flag flag = hasFlags.getFlag(flagType);
        if (flag == null)
            throw new MyTownCommandException("mytown.cmd.err.flagNotExists", flagType.toString());
        return flag;
    }

    public static Flag getFlagFromName(IFlagsContainer hasFlags, String name) {
        Flag flag;
        try {
            flag = hasFlags.getFlag(FlagType.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new MyTownCommandException("mytown.cmd.err.flagNotExists", ex, name);
        }
        if (flag == null)
            throw new MyTownCommandException("mytown.cmd.err.flagNotExists", name);
        return flag;
    }

    public static TownBlock getBlockAtResident(Resident res) {
        TownBlock block = getDatasource().getBlock(res.getPlayer().dimension, ((int) res.getPlayer().posX) >> 4, ((int) res.getPlayer().posZ >> 4));
        if (block == null)
            throw new MyTownCommandException("mytown.cmd.err.claim.notexist", res.getSelectedTown());
        return block;
    }

    public static Rank getRankFromTown(Town town, String rankName) {
        Rank rank = town.getRank(rankName);
        if (rank == null) {
            throw new MyTownCommandException("mytown.cmd.err.rank.notexist", rankName, town.getName());
        }
        return rank;
    }

    public static Rank getRankFromResident(Resident res) {
        Rank rank = res.getTownRank();
        if (rank == null) {
            throw new MyTownCommandException("mytown.cmd.err.partOfTown");
        }
        return rank;
    }

    public static Plot getPlotAtPosition(int dim, int x, int y, int z) {
        Town town = MyTownUtils.getTownAtPosition(dim, x >> 4, z >> 4);
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.blockNotInPlot");
        Plot plot = town.getPlotAtCoords(dim, x, y, z);
        if (plot == null)
            throw new MyTownCommandException("mytown.cmd.err.blockNotInPlot");
        return plot;
    }

    public static Plot getPlotFromName(Town town, String name) {
        Plot plot = town.getPlot(name);
        if(plot == null)  {
            throw new MyTownCommandException("mytown.cmd.err.plot.notExists", name);
        }
        return plot;
    }

    public static FlagType getFlagTypeFromName(String name) {
        try {
            return FlagType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MyTownCommandException("mytown.cmd.err.flagNotExists", e, name);
        }
    }

    public static void sendMessageBackToSender(ICommandSender sender, String message) {
        if (sender instanceof EntityPlayer) {
            Resident res = getDatasource().getOrMakeResident(sender);
            res.sendMessage(message);
        } else {
            sender.addChatMessage(new ChatComponentText(message));
        }
    }

    public static void makePayment(EntityPlayer player, int amount) {
        if(amount == 0)
            return;
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(player);
        if(!EconomyProxy.getEconomy().takeMoneyFromPlayer(player, amount)){
            throw new MyTownCommandException("mytown.cmd.err.resident.payment", EconomyProxy.getCurrency(amount));
        }
        res.sendMessage(getLocal().getLocalization("mytown.notification.resident.payment", EconomyProxy.getCurrency(amount)));
    }

    public static void makeRefund(EntityPlayer player, int amount) {
        if(amount == 0)
            return;
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(player);
        EconomyProxy.getEconomy().giveMoneyToPlayer(player, amount);
        res.sendMessage(getLocal().getLocalization("mytown.notification.resident.refund", EconomyProxy.getCurrency(amount)));
    }

    public static void makeBankPayment(ICommandSender sender, Town town, int amount) {
        if(amount == 0)
            return;

        if(town.getBankAmount() < amount)
            throw new MyTownCommandException("mytown.cmd.err.town.payment", EconomyProxy.getCurrency(amount));

        getDatasource().updateTownBank(town, town.getBankAmount() - amount);
        sendMessageBackToSender(sender, LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.payment", EconomyProxy.getCurrency(amount)));
    }

    public static void makeBankRefund(ICommandSender sender, Town town, int amount) {
        if(amount == 0)
            return;

        getDatasource().updateTownBank(town, town.getBankAmount() + amount);
        sendMessageBackToSender(sender, LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.refund", EconomyProxy.getCurrency(amount)));
    }
}
