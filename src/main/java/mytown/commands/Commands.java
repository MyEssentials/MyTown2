package mytown.commands;

import com.google.common.collect.ImmutableList;
import mytown.MyTown;
import mytown.core.Localization;
import mytown.core.MyTownCore;
import mytown.core.utils.command.CommandManager;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Block;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.entities.Rank;
import mytown.util.Utils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.List;

/**
 * Created by AfterWind on 8/29/2014.
 * Base class for all classes that hold command methods... Mostly for some utils
 */
public abstract class Commands {
    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
    public static MyTownUniverse getUniverse() {
        return MyTownUniverse.getInstance();
    }
    public static Localization getLocal() { return LocalizationProxy.getLocalization(); }
    public static boolean callSubFunctions(ICommandSender sender, List<String> args, List<String> subCommands, String callersPermNode) {
        if(args.size() > 0) {
            for(String s : subCommands) {
                String name = CommandManager.commandNames.get(s);
                MyTown.instance.log.info("Found command " + s);
                // Checking if name corresponds and if parent's corresponds
                if(name.equals(args.get(0)) && CommandManager.getParentPermNode(s).equals(callersPermNode)) {
                    MyTown.instance.log.info("Called " + s);
                    CommandManager.commandCall(s, sender, args.subList(1, args.size()));
                    return true;
                }
            }
        } else {
            MyTownCore.Instance.log.info("Nothing found...");
            // TODO: Give help
        }
        return false;
    }

    public static boolean firstPermissionBreach(String permission, ICommandSender sender) {
        Resident res = getDatasource().getOrMakeResident(sender);
        // Get its rank with the permissions
        Rank rank = res.getTownRank(res.getSelectedTown());

        if(rank == null) {
            MyTown.instance.log.info("Did not find rank..." + permission);
            return Rank.outsiderPermCheck(permission);
        }
        MyTown.instance.log.info("Found rank " + rank.getName() + permission);
        return rank.hasPermissionOrSuperPermission(permission);
    }

    /* ---- HELPERS ---- */

    public static Town getTownFromResident(Resident res) {
        Town town = res.getSelectedTown();
        if(town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        return town;
    }

    public static Town getTownFromName(String name) {
        Town town = MyTownUniverse.getInstance().getTownsMap().get(name);
        if(town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.town.notexist"), name);
        return town;
    }

    public static Resident getResidentFromName(String playerName) {
        Resident res = getDatasource().getOrMakeResident(playerName);
        if(res == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.resident.notexist", playerName));
        return res;
    }

    public static Plot getPlotAtResident(Resident res) {
        Town town = getTownFromResident(res);
        Plot plot = town.getPlotAtResident(res);
        if(plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.notInPlot"));
        return plot;
    }

    public static ImmutableList<Town> getInvitesFromResident(Resident res) {
        ImmutableList<Town> list = res.getInvites();
        if(list == null || list.isEmpty())
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.invite.noinvitations"));
        return list;
    }

    public static Flag getFlagFromPlot(Plot plot, String name) {
        Flag flag = plot.getFlag(name);
        if(flag == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.flagNotExists", name));
        return flag;
    }

    public static Flag getFlagFromTown(Town town, String name) {
        Flag flag = town.getFlag(name);
        if(flag == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.flagNotExists", name));
        return flag;
    }

    public static Block getBlockAtResident(Resident res) {
        Block block = getDatasource().getBlock(res.getPlayer().dimension, res.getPlayer().chunkCoordX, res.getPlayer().chunkCoordZ);
        if(block == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.claim.notexist"));
        return block;
    }

    public static Rank getRankFromTown(Town town, String rankName) {
        Rank rank = town.getRank(rankName);
        if(rank == null) {
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.rank.notexist", rankName, town.getName()));
        }
        return rank;
    }

    public static Rank getRankFromResident(Resident res) {
        Rank rank = res.getTownRank();
        if(rank == null) {
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        }
        return rank;
    }

    public static Plot getPlotAtPosition(int dim, int x, int y, int z) {
        Town town = Utils.getTownAtPosition(dim, x >> 4, z >> 4);
        if(town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.blockNotInPlot"));
        Plot plot = town.getPlotAtCoords(dim, x, y, z);
        if(plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.blockNotInPlot"));
        return plot;
    }
}
