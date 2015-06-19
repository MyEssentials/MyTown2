package mytown.commands;

import mytown.config.Config;
import mytown.core.utils.ChatUtils;
import mytown.core.utils.PlayerUtils;
import mytown.core.utils.StringUtils;
import mytown.core.command.Command;
import mytown.core.command.CommandNode;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.entities.tools.PlotSelectionTool;
import mytown.entities.tools.PlotSellTool;
import mytown.entities.tools.Tool;
import mytown.entities.tools.WhitelisterTool;
import mytown.proxies.EconomyProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Formatter;
import mytown.util.exceptions.MyTownCommandException;
import mytown.util.exceptions.MyTownWrongUsageException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * Process methods for all commands that can be used by everyone
 */
public class CommandsEveryone extends Commands {

    @Command(
            name = "mytown",
            permission = "mytown.cmd",
            alias = {"t", "town"})
    public static void townCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd");
    }

    @CommandNode(
            name = "leave",
            permission = "mytown.cmd.everyone.leave",
            parentName = "mytown.cmd")
    public static void leaveCommand(ICommandSender sender, List<String> args) {
        if (args.isEmpty())
            callSubFunctions(sender, args, "mytown.cmd.everyone.leave");
        else {
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            if (town.getResidentRank(res) != null && town.getResidentRank(res).getName().equals(Rank.theMayorDefaultRank)) {
                res.sendMessage(getLocal().getLocalization("mytown.notification.town.left.asMayor"));
                return;
            }

            getDatasource().unlinkResidentFromTown(res, town);

            res.sendMessage(getLocal().getLocalization("mytown.notification.town.left.self", town.getName()));
            town.notifyEveryone(getLocal().getLocalization("mytown.notification.town.left", res.getPlayerName(), town.getName()));
        }
    }

    @CommandNode(
            name = "spawn",
            permission = "mytown.cmd.everyone.spawn",
            parentName = "mytown.cmd",
            completionKeys = {"townCompletion"})
    public static void spawnCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer)sender;
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town;
        int amount;

        if (args.isEmpty()) {
            town = getTownFromResident(res);
            amount = Config.costAmountSpawn;
        } else {
            town = getTownFromName(args.get(0));
            amount = Config.costAmountOtherSpawn;
        }

        if (!town.hasSpawn())
            throw new MyTownCommandException("mytown.cmd.err.spawn.notexist", town.getName());

        if(!town.checkPermission(res, FlagType.ENTER, false, town.getSpawn().getDim(), (int)town.getSpawn().getX(), (int)town.getSpawn().getY(), (int)town.getSpawn().getZ()))
            throw new MyTownCommandException("mytown.cmd.err.spawn.protected", town.getName());

        if(res.getTeleportCooldown() > 0)
            throw new MyTownCommandException("mytown.cmd.err.spawn.cooldown", res.getTeleportCooldown(), res.getTeleportCooldown() / 20);

        makePayment(player, amount);
        town.sendToSpawn(res);
    }

    @CommandNode(
            name = "select",
            permission = "mytown.cmd.everyone.select",
            parentName = "mytown.cmd",
            completionKeys = {"townCompletion"})
    public static void selectCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.select");
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));
        if (!town.hasResident(res))
            throw new MyTownCommandException("mytown.cmd.err.select.notpart", args.get(0));
        getDatasource().saveSelectedTown(res, town);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.select", args.get(0)));
    }


    @CommandNode(
            name = "blocks",
            permission = "mytown.cmd.everyone.blocks",
            parentName = "mytown.cmd")
    public static void blocksCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.everyone.blocks");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.blocks.list",
            parentName = "mytown.cmd.everyone.blocks")
    public static void blocksListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.block.list", town.getName(), "\n" + Formatter.formatTownBlocksToString(town.getBlocks())));
    }


    @CommandNode(
            name = "perm",
            permission = "mytown.cmd.everyone.perm",
            parentName = "mytown.cmd")
    public static void permCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.everyone.perm");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.perm.list",
            parentName = "mytown.cmd.everyone.perm")
    public static void permListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        res.sendMessage(Formatter.formatFlagsToString(town));
    }

    public static class Plots {

        @CommandNode(
                name = "perm",
                permission = "mytown.cmd.everyone.plot.perm",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotPermCommand(ICommandSender sender, List<String> args) {
            callSubFunctions(sender, args, "mytown.cmd.everyone.plot.perm");
        }

        @CommandNode(
                name = "set",
                permission = "mytown.cmd.everyone.plot.perm.set",
                parentName = "mytown.cmd.everyone.plot.perm",
                completionKeys = {"flagCompletion"})
        public static void plotPermSetCommand(ICommandSender sender, List<String> args) {

            if (args.size() < 2)
                throw new MyTownWrongUsageException("mytown.cmd.err.perm.set.usage");
            Resident res = getDatasource().getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            if (!plot.hasOwner(res))
                throw new MyTownCommandException("mytown.cmd.err.plot.perm.set.noPermission");

            Flag flag = getFlagFromName(plot, args.get(0));

            if (flag.setValueFromString(args.get(1))) {
                ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.perm.set.success", args.get(0), args.get(1));
            } else
                throw new MyTownCommandException("mytown.cmd.err.perm.valueNotValid", args.get(1));

            getDatasource().saveFlag(flag, plot);
        }

        @CommandNode(
                name = "list",
                permission = "mytown.cmd.everyone.plot.perm.list",
                parentName = "mytown.cmd.everyone.plot.perm")
        public static void plotPermListCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            res.sendMessage(Formatter.formatFlagsToString(plot));
        }

        @CommandNode(
                name = "whitelist",
                permission = "mytown.cmd.everyone.plot.perm.whitelist",
                parentName = "mytown.cmd.everyone.plot.perm")
        public static void plotPermWhitelistCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);

            res.setCurrentTool(new WhitelisterTool(res));
            res.sendMessage(getLocal().getLocalization("mytown.notification.perm.whitelist.start"));
        }

        @CommandNode(
                name = "plot",
                permission = "mytown.cmd.everyone.plot",
                parentName = "mytown.cmd")
        public static void plotCommand(ICommandSender sender, List<String> args) {
            callSubFunctions(sender, args, "mytown.cmd.everyone.plot");
        }

        @CommandNode(
                name = "rename",
                permission = "mytown.cmd.everyone.plot.rename",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotRenameCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.rename");

            Resident res = getDatasource().getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);

            plot.setName(args.get(0));

            getDatasource().savePlot(plot);

            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.renamed"); // Maybe give more info about the plot?
        }

        @CommandNode(
                name = "new",
                permission = "mytown.cmd.everyone.plot.new",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotNewCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.new");

            Resident res = getDatasource().getOrMakeResident(sender);
            res.setCurrentTool(new PlotSelectionTool(res, args.get(0)));
            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.start"));
        }

        @CommandNode(
                name = "select",
                permission = "mytown.cmd.everyone.plot.select",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotSelectCommand(ICommandSender sender, List<String> args) {
            callSubFunctions(sender, args, "mytown.cmd.everyone.plot.select");
        }

        @CommandNode(
                name = "reset",
                permission = "mytown.cmd.everyone.plot.select.reset",
                parentName = "mytown.cmd.everyone.plot.select")
        public static void plotSelectResetCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Tool currentTool = res.getCurrentTool();
            if(currentTool == null || !(currentTool instanceof PlotSelectionTool))
                throw new MyTownCommandException("mytown.cmd.err.plot.selection.none");
            ((PlotSelectionTool) currentTool).resetSelection(true, 0);
            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.selectionReset"));
        }

        @CommandNode(
                name = "show",
                permission = "mytown.cmd.everyone.plot.show",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotShowCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            town.showPlots(res);
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.showing");
        }

        @CommandNode(
                name = "hide",
                permission = "mytown.cmd.everyone.plot.hide",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotHideCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            town.hidePlots(res);
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.vanished");
        }

        @CommandNode(
                name = "add",
                permission = "mytown.cmd.everyone.plot.add",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotAddCommand(ICommandSender sender, List<String> args) {
            callSubFunctions(sender, args, "mytown.cmd.everyone.plot.add");
        }

        @CommandNode(
                name = "owner",
                permission = "mytown.cmd.everyone.plot.add.owner",
                parentName = "mytown.cmd.everyone.plot.add",
                completionKeys = {"residentCompletion"})
        public static void plotAddOwnerCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.add");
            Resident res = getDatasource().getOrMakeResident(sender);
            Resident target = getResidentFromName(args.get(0));

            Town town = getTownFromResident(res);
            if (!target.hasTown(town))
                throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", target.getPlayerName(), town.getName());

            Plot plot = getPlotAtResident(res);

            if(!plot.hasOwner(res))
                throw new MyTownCommandException("mytown.cmd.err.plot.notOwner");

            if(plot.hasResident(target))
                throw new MyTownCommandException("mytown.cmd.err.plot.add.alreadyInPlot");

            if (!town.canResidentMakePlot(target))
                throw new MyTownCommandException("mytown.cmd.err.plot.limit.toPlayer", target.getPlayerName());

            getDatasource().linkResidentToPlot(target, plot, true);

            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.owner.sender.added", target.getPlayerName(), plot.getName()));
            target.sendMessage(getLocal().getLocalization("mytown.notification.plot.owner.target.added", plot.getName()));
        }

        @CommandNode(
                name = "member",
                permission = "mytown.cmd.everyone.plot.add.member",
                parentName = "mytown.cmd.everyone.plot.add",
                completionKeys = {"residentCompletion"})
        public static void plotAddMemberCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.add");
            Resident res = getDatasource().getOrMakeResident(sender);
            Resident target = getResidentFromName(args.get(0));
            Plot plot = getPlotAtResident(res);

            if(!plot.hasOwner(res))
                throw new MyTownCommandException("mytown.cmd.err.plot.notOwner");

            if(plot.hasResident(target))
                throw new MyTownCommandException("mytown.cmd.err.plot.add.alreadyInPlot");

            getDatasource().linkResidentToPlot(target, plot, false);

            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.member.sender.added", target.getPlayerName(), plot.getName()));
            target.sendMessage(getLocal().getLocalization("mytown.notification.plot.member.target.added", plot.getName()));
        }

        @CommandNode(
                name = "remove",
                permission = "mytown.cmd.everyone.plot.remove",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotRemoveCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.remove");
            Resident res = getDatasource().getOrMakeResident(sender);
            Resident target = getResidentFromName(args.get(0));
            Plot plot = getPlotAtResident(res);

            if(!plot.hasOwner(res))
                throw new MyTownCommandException("mytown.cmd.err.plot.notOwner");

            if(!plot.hasResident(target))
                throw new MyTownCommandException("mytown.cmd.err.plot.remove.notInPlot");

            if(plot.hasOwner(target) && plot.getOwners().size() == 1)
                throw new MyTownCommandException("mytown.cmd.err.plot.remove.onlyOwner");

            getDatasource().unlinkResidentFromPlot(target, plot);

            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.sender.removed", target.getPlayerName(), plot.getName()));
            target.sendMessage(getLocal().getLocalization("mytown.notification.plot.target.removed", plot.getName()));
        }

            @CommandNode(
                name = "info",
                permission = "mytown.cmd.everyone.plot.info",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotInfoCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.info", plot.getName(), Formatter.formatResidentsToString(plot), plot.getStartX(), plot.getStartY(), plot.getStartZ(), plot.getEndX(), plot.getEndY(), plot.getEndZ()));
        }

        @CommandNode(
                name = "delete",
                permission = "mytown.cmd.everyone.plot.delete",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotDeleteCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            if (!plot.hasOwner(res))
                throw new MyTownCommandException("mytown.cmd.err.plot.notOwner");

            getDatasource().deletePlot(plot);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.deleted", plot.getName()));
        }

        @CommandNode(
                name = "sell",
                permission = "mytown.cmd.everyone.plot.sell",
                parentName = "mytown.cmd.everyone.plot")
        public static void plotSellCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.sell");
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            if(!StringUtils.tryParseInt(args.get(0)) || Integer.parseInt(args.get(0)) < 0)
                throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));

            int price = Integer.parseInt(args.get(0));
            res.setCurrentTool(new PlotSellTool(res, price));
        }
    }

    @CommandNode(
            name = "ranks",
            permission = "mytown.cmd.everyone.ranks",
            parentName = "mytown.cmd")
    public static void ranksCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.everyone.ranks");
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

    @CommandNode(
            name = "borders",
            permission = "mytown.cmd.everyone.borders",
            parentName = "mytown.cmd")
    public static void bordersCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.everyone.borders");
    }

    @CommandNode(
            name = "show",
            permission = "mytown.cmd.everyone.borders.show",
            parentName = "mytown.cmd.everyone.borders")
    public static void bordersShowCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        town.showBorders(res);
        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.borders.show", town.getName()));
    }

    @CommandNode(
            name = "hide",
            permission = "mytown.cmd.everyone.borders.hide",
            parentName = "mytown.cmd.everyone.borders")
    public static void bordersHideCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        town.hideBorders(res);
        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.borders.hide"));
    }

    @CommandNode(
            name = "bank",
            permission = "mytown.cmd.everyone.bank",
            parentName = "mytown.cmd")
    public static void bankCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.everyone.bank");
    }

    @CommandNode(
            name = "info",
            permission = "mytown.cmd.everyone.bank.info",
            parentName = "mytown.cmd.everyone.bank")
    public static void bankAmountCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if(town instanceof AdminTown)
            throw new MyTownCommandException("mytown.cmd.err.adminTown", town.getName());

        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.bank.info", EconomyProxy.getCurrency(town.getBankAmount()), EconomyProxy.getCurrency(town.getNextPaymentAmount())));
    }

    @CommandNode(
            name = "pay",
            permission = "mytown.cmd.everyone.bank.pay",
            parentName = "mytown.cmd.everyone.bank")
    public static void bankPayCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.bank.pay");

        if(!StringUtils.tryParseInt(args.get(0)))
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if(town instanceof AdminTown)
            throw new MyTownCommandException("mytown.cmd.err.adminTown", town.getName());

        int amount = Integer.parseInt(args.get(0));
        makePayment(res.getPlayer(), amount);
        getDatasource().updateTownBank(town, town.getBankAmount() + amount);
    }

    @CommandNode(
            name = "wild",
            permission = "mytown.cmd.everyone.wild",
            parentName = "mytown.cmd")
    public static void permWildCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.everyone.wild");
    }

    @CommandNode(
            name = "perm",
            permission = "mytown.cmd.everyone.wild.perm",
            parentName = "mytown.cmd.everyone.wild")
    public static void permWildListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        res.sendMessage(Formatter.formatFlagsToString(Wild.instance));
    }
}