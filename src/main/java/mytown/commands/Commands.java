package mytown.commands;

import myessentials.Localization;
import mypermissions.api.command.CommandCompletion;
import mytown.MyTown;
import mytown.api.container.FlagsContainer;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.EconomyProxy;
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
        return MyTown.instance.LOCAL;
    }

    /**
     * Populates the tab completion map.
     */
    public static void populateCompletionMap() {

        List<String> populator = new ArrayList<String>();
        for(Town town : getUniverse().towns) {
            populator.add(town.getName());
        }

        CommandCompletion.addCompletions("townCompletionAndAll", populator);
        CommandCompletion.addCompletion("townCompletionAndAll", "@a");

        CommandCompletion.addCompletions("townCompletion", populator);

        populator = new ArrayList<String>();
        for (Resident res : getUniverse().residents) {
            populator.add(res.getPlayerName());
        }
        CommandCompletion.addCompletions("residentCompletion", populator);

        populator = new ArrayList<String>();
        for (FlagType flag : FlagType.values()) {
            populator.add(flag.toString().toLowerCase());
        }
        CommandCompletion.addCompletions("flagCompletion", populator);

        populator = new ArrayList<String>();
        for (FlagType flag : FlagType.values()) {
            if (flag.isWhitelistable())
                populator.add(flag.toString().toLowerCase());
        }
        CommandCompletion.addCompletions("flagCompletionWhitelist", populator);

        populator = new ArrayList<String>();
        for(Plot plot : MyTownUniverse.instance.plots) {
            populator.add(plot.toString());
        }
        CommandCompletion.addCompletions("plotCompletion", populator);

        populator = new ArrayList<String>();
        for(Rank rank : Rank.defaultRanks) {
            populator.add(rank.getName());
        }
        CommandCompletion.addCompletions("rankCompletion", populator);
    }

    /* ---- HELPERS ---- */

    public static Town getTownFromResident(Resident res) {
        Town town = res.townsContainer.getMainTown();
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.partOfTown");
        return town;
    }

    public static Town getTownFromName(String name) {
        Town town = getUniverse().towns.get(name);
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.town.notexist", name);
        return town;
    }

    public static Resident getResidentFromName(String playerName) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(playerName);
        if (res == null)
            throw new MyTownCommandException("mytown.cmd.err.resident.notexist", playerName);
        return res;
    }

    public static Plot getPlotAtResident(Resident res) {
        Town town = getTownFromResident(res);
        Plot plot = town.plotsContainer.get(res);
        if (plot == null)
            throw new MyTownCommandException("mytown.cmd.err.plot.notInPlot");
        return plot;
    }

    public static List<Town> getInvitesFromResident(Resident res) {
        if (res.townInvitesContainer.isEmpty())
            throw new MyTownCommandException("mytown.cmd.err.invite.noinvitations");
        return res.townInvitesContainer;
    }

    public static Flag getFlagFromType(FlagsContainer flagsContainer, FlagType flagType) {
        Flag flag = flagsContainer.get(flagType);
        if (flag == null)
            throw new MyTownCommandException("mytown.cmd.err.flagNotExists", flagType.toString());
        return flag;
    }

    public static Flag getFlagFromName(FlagsContainer flagsContainer, String name) {
        Flag flag;
        try {
            flag = flagsContainer.get(FlagType.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new MyTownCommandException("mytown.cmd.err.flagNotExists", ex, name);
        }
        if (flag == null)
            throw new MyTownCommandException("mytown.cmd.err.flagNotExists", name);
        return flag;
    }

    public static TownBlock getBlockAtResident(Resident res) {
        TownBlock block = getUniverse().blocks.get(res.getPlayer().dimension, ((int) res.getPlayer().posX) >> 4, ((int) res.getPlayer().posZ >> 4));
        if (block == null)
            throw new MyTownCommandException("mytown.cmd.err.claim.notexist", res.townsContainer.getMainTown().getName());
        return block;
    }

    public static Rank getRankFromTown(Town town, String rankName) {
        Rank rank = town.ranksContainer.get(rankName);
        if (rank == null) {
            throw new MyTownCommandException("mytown.cmd.err.rank.notexist", rankName, town.getName());
        }
        return rank;
    }

    public static Rank getRankFromResident(Resident res) {
        Town town = res.townsContainer.getMainTown();
        if (town == null) {
            throw new MyTownCommandException("mytown.cmd.err.partOfTown");
        }
        return town.residentsMap.get(res);
    }

    public static Plot getPlotAtPosition(int dim, int x, int y, int z) {
        Town town = MyTownUtils.getTownAtPosition(dim, x >> 4, z >> 4);
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.blockNotInPlot");
        Plot plot = town.plotsContainer.get(dim, x, y, z);
        if (plot == null)
            throw new MyTownCommandException("mytown.cmd.err.blockNotInPlot");
        return plot;
    }

    public static Plot getPlotFromName(Town town, String name) {
        Plot plot = town.plotsContainer.get(name);
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

    public static Rank.Type getRankTypeFromString(String name) {
        try {
            return Rank.Type.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MyTownCommandException("mytown.cmd.err.ranks.typeNotExists", e, name);
        }
    }

    public static void sendMessageBackToSender(ICommandSender sender, String message) {
        if (sender instanceof EntityPlayer) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            res.sendMessage(message);
        } else {
            sender.addChatMessage(new ChatComponentText(message));
        }
    }

    public static void makePayment(EntityPlayer player, int amount) {
        if(amount == 0)
            return;
        Resident res = MyTownUniverse.instance.getOrMakeResident(player);
        if(!EconomyProxy.getEconomy().takeMoneyFromPlayer(player, amount)){
            throw new MyTownCommandException("mytown.cmd.err.resident.payment", EconomyProxy.getCurrency(amount));
        }
        res.sendMessage(getLocal().getLocalization("mytown.notification.resident.payment", EconomyProxy.getCurrency(amount)));
    }

    public static void makeRefund(EntityPlayer player, int amount) {
        if(amount == 0)
            return;
        Resident res = MyTownUniverse.instance.getOrMakeResident(player);
        EconomyProxy.getEconomy().giveMoneyToPlayer(player, amount);
        res.sendMessage(getLocal().getLocalization("mytown.notification.resident.refund", EconomyProxy.getCurrency(amount)));
    }

    public static void makeBankPayment(ICommandSender sender, Town town, int amount) {
        if(amount == 0)
            return;

        if(town.bank.getAmount() < amount)
            throw new MyTownCommandException("mytown.cmd.err.town.payment", EconomyProxy.getCurrency(amount));

        town.bank.addAmount(-amount);
        getDatasource().saveTownBank(town.bank);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.town.payment", EconomyProxy.getCurrency(amount)));
    }

    public static void makeBankRefund(ICommandSender sender, Town town, int amount) {
        if(amount == 0)
            return;

        town.bank.addAmount(amount);
        getDatasource().saveTownBank(town.bank);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.town.refund", EconomyProxy.getCurrency(amount)));
    }
}
