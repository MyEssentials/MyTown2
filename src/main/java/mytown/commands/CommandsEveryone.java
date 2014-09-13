package mytown.commands;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.Command;
import mytown.core.utils.command.CommandNode;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.handlers.VisualsTickHandler;
import mytown.proxies.DatasourceProxy;
import mytown.util.Constants;
import mytown.util.Formatter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 8/28/2014.
 * Process methods for all commands that can be used by everyone
 */
public class CommandsEveryone extends Commands{

    @Command(
            name = "town",
            permission = "mytown.cmd",
            alias = {"t"})
    public static void townCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd");
    }

    @CommandNode(
            name = "leave",
            permission = "mytown.cmd.everyone.leave",
            parentName = "mytown.cmd")
    public static void leaveCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        getDatasource().unlinkResidentFromTown(res, town);

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.left.self", town.getName()));

        for (Resident r : town.getResidents()) {
            r.sendMessage(getLocal().getLocalization("mytown.notification.town.left", res.getPlayerName(), town.getName()));
        }
    }


    @CommandNode(
            name = "spawn",
            permission = "mytown.cmd.everyone.spawn",
            parentName = "mytown.cmd",
            completionKeys = {"townCompletion"})
    public static void spawnCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = null;

        if (args.size() == 0) {
            town = getTownFromResident(res);
        } else {
            town = getTownFromName(args.get(0));
        }

        // TODO Check if the Resident is allowed to go to spawn
        if (!town.hasSpawn())
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.spawn.notexist", town.getName()));
        town.sendToSpawn(res);
    }

    @CommandNode(
            name = "select",
            permission = "mytown.cmd.everyone.select",
            parentName = "mytown.cmd",
            completionKeys = {"townCompletion"})
    public static void selectCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.select"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));
        if (!town.hasResident(res))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.select.notpart", args.get(0)));
        getDatasource().saveSelectedTown(res, town);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.select", args.get(0)));
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.blocks.list",
            parentName = "mytown.cmd.assistant.blocks")
    public static void blocksListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        String s = null;
        for(Block block : town.getBlocks()) {
            if(s == null)
                s = block.toString();
            else
                s += "\n" + block.toString();
        }

        res.sendMessage(getLocal().getLocalization("mytown.notification.block.list", town.getName(), "\n" + s));
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.perm.town.list",
            parentName = "mytown.cmd.assistant.perm.town")
    public static void listPermCommand(ICommandSender sender, List<String> args) {

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        String formattedFlagList = null;
        for (Flag flag : town.getFlags()) {
            if (formattedFlagList == null) {
                formattedFlagList = "";
            } else {
                formattedFlagList += '\n';
            }
            formattedFlagList += flag;
        }
        if(formattedFlagList != null)
            res.sendMessage(formattedFlagList);
        else
            res.sendMessage(getLocal().getLocalization("mytown.cmd.err.flag.list"));
    }

    @CommandNode(
            name = "set",
            permission = "mytown.cmd.everyone.perm.plot.set",
            parentName = "mytown.cmd.everyone.perm.plot",
            completionKeys = {"flagCompletion"})
    public static void permSetPlotCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 2)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.err.perm.set.usage"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Plot plot = getPlotAtResident(res);
        if(!plot.hasOwner(res))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.perm.set.noPermission"));

        Flag flag = getFlagFromPlot(plot, args.get(0));

        if (flag.setValueFromString(args.get(1))) {
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.perm.set.success", args.get(0), args.get(1));
        } else
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.perm.valueNotValid", args.get(1)));

        getDatasource().saveFlag(flag, plot);
    }

    @CommandNode(
            name = "plot",
            permission = "mytown.cmd.everyone.perm.plot",
            parentName = "mytown.cmd.assistant.perm")
    public static void permPlotCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.perm.plot");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.perm.plot.list",
            parentName = "mytown.cmd.everyone.perm.plot")
    public static void permPlotListCommand(ICommandSender sender, List<String> args) {

        Resident res = getDatasource().getOrMakeResident(sender);
        Plot plot = getPlotAtResident(res);

        String formattedFlagList = null;
        for (Flag flag : plot.getFlags()) {
            if (formattedFlagList == null) {
                formattedFlagList = "";
            } else {
                formattedFlagList += '\n';
            }
            formattedFlagList += flag;
        }
        if(formattedFlagList != null)
            res.sendMessage(formattedFlagList);
        else
            res.sendMessage(getLocal().getLocalization("mytown.cmd.err.flag.list"));
    }

    @CommandNode(
            name = "whitelist",
            permission = "mytown.cmd.everyone.perm.plot.whitelist",
            parentName = "mytown.cmd.everyone.perm.plot",
            completionKeys = {"flagCompletionWhitelist"})
    public static void permPlotWhitelistCommand(ICommandSender sender, List<String> args) {
        if(args.size() == 0)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.usage.plot.whitelist.add"));

        Resident res = getDatasource().getOrMakeResident(sender);
        Plot plot = getPlotAtResident(res);
        String flagName = args.get(0);

        if(Flag.flagsForWhitelist.contains(flagName)) {
            res.sendMessage(getLocal().getLocalization("mytown.notification.perm.whitelist.start"));
            res.startBlockSelection(flagName, plot.getTown().getName(), true);
        }
        else
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.flag.notForWhitelist"));
    }

    @CommandNode(
            name = "plot",
            permission = "mytown.cmd.everyone.plot",
            parentName = "mytown.cmd")
    public static void plotCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.plot");
    }



    @CommandNode(
            name = "make",
            permission = "mytown.cmd.everyone.plot.make",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotMakeCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        String plotName = "NoName";

        if (args.size() > 0) {
            plotName = args.get(0);
        }

        // This handles all the db stuff... Not sure if I should change :/
        Plot plot = res.makePlotFromSelection(plotName);

        if (plot != null) {
            getDatasource().savePlot(plot);
            getDatasource().linkResidentToPlot(res, plot, true);

            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.plot.created");
        } else
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.failed"));

    }

    @CommandNode(
            name = "rename",
            permission = "mytown.cmd.everyone.plot.rename",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotRenameCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.plot.rename"));

        Resident res = getDatasource().getOrMakeResident(sender);
        Plot plot = getPlotAtResident(res);

        plot.setName(args.get(0));

        getDatasource().savePlot(plot);

        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.renamed"); // Maybe give more info about the plot?
    }

    @CommandNode(
            name = "select",
            permission = "mytown.cmd.everyone.plot.select",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotSelectCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        if(args.size() == 0) {
            Resident res = getDatasource().getOrMakeResident(sender);
            res.startPlotSelection();
        } else {
            callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.plot.select");
        }
    }
    @CommandNode(
            name = "expandVert",
            permission = "mytown.cmd.everyone.plot.select.expand",
            parentName = "mytown.cmd.everyone.plot.select")
    public static void plotSelectExpandCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);

        if (!(res.isFirstPlotSelectionActive() && res.isSecondPlotSelectionActive()))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.notSelected"));

        res.expandSelectionVert();

        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.plot.expanded");
    }

    @CommandNode(
            name = "reset",
            permission = "mytown.cmd.everyone.plot.select.reset",
            parentName = "mytown.cmd.everyone.plot.select")
    public static void plotSelectResetCommand(ICommandSender sender, List<String> args) {

        Resident res = getDatasource().getOrMakeResident(sender);
        res.resetSelection();

        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.plot.selectionReset");
    }

    @CommandNode(
            name = "show",
            permission = "mytown.cmd.everyone.plot.show",
            parentName = "mytown.cmd.everyone.plot")
        public static void plotShowCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        for (Plot plot : town.getPlots()) {
            VisualsTickHandler.instance.markPlotBorders(plot);
        }
        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.showing");
    }

    @CommandNode(
            name = "vanish",
            permission = "mytown.cmd.everyone.plot.vanish",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotVanishCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        for (Plot plot : town.getPlots()) {
            VisualsTickHandler.instance.unmarkPlotBorders(plot);
        }
        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.vanished");
    }

    @CommandNode(
            name = "add",
            permission = "mytown.cmd.everyone.plot.add",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotAddCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.plot.add");
    }

    @CommandNode(
            name = "owner",
            permission = "mytown.cmd.everyone.plot.add.owner",
            parentName = "mytown.cmd.everyone.plot.add",
            completionKeys = {"residentCompletion"})
    public static void plotAddOwnerCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        if(args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.plot.add"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident target = getResidentFromName(args.get(0));
        Plot plot = getPlotAtResident(res);

        getDatasource().linkResidentToPlot(target, plot, true);

        res.sendMessage(getLocal().getLocalization("mytown.notification.plot.owner.sender.added", target.getPlayerName(), plot.getName()));
        target.sendMessage(getLocal().getLocalization("mytown.notification.plot.owner.target.added", plot.getName()));
    }

    @CommandNode(
            name = "member",
            permission = "mytown.cmd.everyone.plot.add.member",
            parentName = "mytown.cmd.everyone.plot.add",
            completionKeys = {"residentCompletion"})
    public static void plotAddMemberCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        if(args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.plot.add"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident target = getResidentFromName(args.get(0));
        Plot plot = getPlotAtResident(res);

        getDatasource().linkResidentToPlot(target, plot, false);

        res.sendMessage(getLocal().getLocalization("mytown.notification.plot.member.sender.added", target.getPlayerName(), plot.getName()));
        target.sendMessage(getLocal().getLocalization("mytown.notification.plot.member.target.added", plot.getName()));
    }

    @CommandNode(
            name = "info",
            permission = "mytown.cmd.everyone.plot.info",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotInfoCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Plot plot = getPlotAtResident(res);

        res.sendMessage(Formatter.formatPlotInfo(plot));
    }
    @CommandNode(
            name = "delete",
            permission = "mytown.cmd.everyone.plot.delete",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotDeleteCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Plot plot = getPlotAtResident(res);
        if(!plot.hasOwner(res))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.notOwner"));

        getDatasource().deletePlot(plot);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.plot.deleted"));
    }
    @CommandNode(
            name = "ranks",
            permission = "mytown.cmd.everyone.ranks",
            parentName = "mytown.cmd")
    public static void ranksCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.ranks");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.ranks.list",
            parentName = "mytown.cmd.everyone.ranks")
    public static void listRanksCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.ranks", Formatter.formatRanksToString(town.getRanks()));
    }
}
