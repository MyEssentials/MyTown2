package mytown.commands;

import myessentials.command.CommandResponse;
import myessentials.command.annotation.Command;
import mytown.config.Config;
import myessentials.utils.ChatUtils;
import myessentials.utils.StringUtils;
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
    public static CommandResponse townCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "leave",
            permission = "mytown.cmd.everyone.leave",
            parentName = "mytown.cmd")
    public static CommandResponse leaveCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if (town.getResidentRank(res) != null && town.getResidentRank(res).getName().equals(Rank.theMayorDefaultRank)) {
            throw new MyTownCommandException("mytown.notification.town.left.asMayor");
        }

        getDatasource().unlinkResidentFromTown(res, town);

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.left.self", town.getName()));
        town.notifyEveryone(getLocal().getLocalization("mytown.notification.town.left", res.getPlayerName(), town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "spawn",
            permission = "mytown.cmd.everyone.spawn",
            parentName = "mytown.cmd",
            completionKeys = {"townCompletion"})
    public static CommandResponse spawnCommand(ICommandSender sender, List<String> args) {
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

        if(!town.hasPermission(res, FlagType.ENTER, false, town.getSpawn().getDim(), (int) town.getSpawn().getX(), (int) town.getSpawn().getY(), (int) town.getSpawn().getZ()))
            throw new MyTownCommandException("mytown.cmd.err.spawn.protected", town.getName());

        if(res.getTeleportCooldown() > 0)
            throw new MyTownCommandException("mytown.cmd.err.spawn.cooldown", res.getTeleportCooldown(), res.getTeleportCooldown() / 20);

        makePayment(player, amount);
        town.sendToSpawn(res);
        return CommandResponse.DONE;
    }

    @Command(
            name = "select",
            permission = "mytown.cmd.everyone.select",
            parentName = "mytown.cmd",
            completionKeys = {"townCompletion"})
    public static CommandResponse selectCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.select");
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));
        if (!town.hasResident(res))
            throw new MyTownCommandException("mytown.cmd.err.select.notpart", args.get(0));
        getDatasource().saveSelectedTown(res, town);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.select", args.get(0)));
        return CommandResponse.DONE;
    }


    @Command(
            name = "blocks",
            permission = "mytown.cmd.everyone.blocks",
            parentName = "mytown.cmd")
    public static CommandResponse blocksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.everyone.blocks.list",
            parentName = "mytown.cmd.everyone.blocks")
    public static CommandResponse blocksListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.block.list", town.getName(), "\n" + Formatter.formatTownBlocksToString(town.getBlocks())));
        return CommandResponse.DONE;
    }


    @Command(
            name = "perm",
            permission = "mytown.cmd.everyone.perm",
            parentName = "mytown.cmd")
    public static CommandResponse permCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.everyone.perm.list",
            parentName = "mytown.cmd.everyone.perm")
    public static CommandResponse permListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        res.sendMessage(Formatter.formatFlagsToString(town));
        return CommandResponse.DONE;
    }

    public static class Plots {

        @Command(
                name = "perm",
                permission = "mytown.cmd.everyone.plot.perm",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotPermCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "set",
                permission = "mytown.cmd.everyone.plot.perm.set",
                parentName = "mytown.cmd.everyone.plot.perm",
                completionKeys = {"flagCompletion"})
        public static CommandResponse plotPermSetCommand(ICommandSender sender, List<String> args) {

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
            return CommandResponse.DONE;
        }

        @Command(
                name = "list",
                permission = "mytown.cmd.everyone.plot.perm.list",
                parentName = "mytown.cmd.everyone.plot.perm")
        public static CommandResponse plotPermListCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            res.sendMessage(Formatter.formatFlagsToString(plot));
            return CommandResponse.DONE;
        }

        @Command(
                name = "whitelist",
                permission = "mytown.cmd.everyone.plot.perm.whitelist",
                parentName = "mytown.cmd.everyone.plot.perm")
        public static CommandResponse plotPermWhitelistCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);

            res.setCurrentTool(new WhitelisterTool(res));
            res.sendMessage(getLocal().getLocalization("mytown.notification.perm.whitelist.start"));
            return CommandResponse.DONE;
        }

        @Command(
                name = "plot",
                permission = "mytown.cmd.everyone.plot",
                parentName = "mytown.cmd")
        public static CommandResponse plotCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "rename",
                permission = "mytown.cmd.everyone.plot.rename",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotRenameCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.rename");

            Resident res = getDatasource().getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);

            plot.setName(args.get(0));
            getDatasource().savePlot(plot);

            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.renamed"));
            return CommandResponse.DONE;
        }

        @Command(
                name = "new",
                permission = "mytown.cmd.everyone.plot.new",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotNewCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.new");

            Resident res = getDatasource().getOrMakeResident(sender);
            res.setCurrentTool(new PlotSelectionTool(res, args.get(0)));
            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.start"));
            return CommandResponse.DONE;
        }

        @Command(
                name = "select",
                permission = "mytown.cmd.everyone.plot.select",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotSelectCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "reset",
                permission = "mytown.cmd.everyone.plot.select.reset",
                parentName = "mytown.cmd.everyone.plot.select")
        public static CommandResponse plotSelectResetCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Tool currentTool = res.getCurrentTool();
            if(currentTool == null || !(currentTool instanceof PlotSelectionTool))
                throw new MyTownCommandException("mytown.cmd.err.plot.selection.none");
            ((PlotSelectionTool) currentTool).resetSelection(true, 0);
            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.selectionReset"));
            return CommandResponse.DONE;
        }

        @Command(
                name = "show",
                permission = "mytown.cmd.everyone.plot.show",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotShowCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            town.showPlots(res);
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.showing");
            return CommandResponse.DONE;
        }

        @Command(
                name = "hide",
                permission = "mytown.cmd.everyone.plot.hide",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotHideCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            town.hidePlots(res);
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.vanished");
            return CommandResponse.DONE;
        }

        @Command(
                name = "add",
                permission = "mytown.cmd.everyone.plot.add",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotAddCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "owner",
                permission = "mytown.cmd.everyone.plot.add.owner",
                parentName = "mytown.cmd.everyone.plot.add",
                completionKeys = {"residentCompletion"})
        public static CommandResponse plotAddOwnerCommand(ICommandSender sender, List<String> args) {
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
            return CommandResponse.DONE;
        }

        @Command(
                name = "member",
                permission = "mytown.cmd.everyone.plot.add.member",
                parentName = "mytown.cmd.everyone.plot.add",
                completionKeys = {"residentCompletion"})
        public static CommandResponse plotAddMemberCommand(ICommandSender sender, List<String> args) {
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
            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "mytown.cmd.everyone.plot.remove",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotRemoveCommand(ICommandSender sender, List<String> args) {
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
            return CommandResponse.DONE;

        }

        @Command(
                name = "info",
                permission = "mytown.cmd.everyone.plot.info",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotInfoCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.info", plot.getName(), Formatter.formatResidentsToString(plot), plot.getStartX(), plot.getStartY(), plot.getStartZ(), plot.getEndX(), plot.getEndY(), plot.getEndZ()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "delete",
                permission = "mytown.cmd.everyone.plot.delete",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotDeleteCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            if (!plot.hasOwner(res))
                throw new MyTownCommandException("mytown.cmd.err.plot.notOwner");

            getDatasource().deletePlot(plot);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.deleted", plot.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "sell",
                permission = "mytown.cmd.everyone.plot.sell",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotSellCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.sell");
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            if(!StringUtils.tryParseInt(args.get(0)) || Integer.parseInt(args.get(0)) < 0)
                throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));

            int price = Integer.parseInt(args.get(0));
            res.setCurrentTool(new PlotSellTool(res, price));
            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "ranks",
            permission = "mytown.cmd.everyone.ranks",
            parentName = "mytown.cmd")
    public static CommandResponse ranksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.everyone.ranks.list",
            parentName = "mytown.cmd.everyone.ranks")
    public static CommandResponse listRanksCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.ranks", Formatter.formatRanksToString(town.getRanks()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "borders",
            permission = "mytown.cmd.everyone.borders",
            parentName = "mytown.cmd")
    public static CommandResponse bordersCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "show",
            permission = "mytown.cmd.everyone.borders.show",
            parentName = "mytown.cmd.everyone.borders")
    public static CommandResponse bordersShowCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        town.showBorders(res);
        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.borders.show", town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "hide",
            permission = "mytown.cmd.everyone.borders.hide",
            parentName = "mytown.cmd.everyone.borders")
    public static CommandResponse bordersHideCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        town.hideBorders(res);
        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.borders.hide"));
        return CommandResponse.DONE;
    }

    @Command(
            name = "bank",
            permission = "mytown.cmd.everyone.bank",
            parentName = "mytown.cmd")
    public static CommandResponse bankCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "info",
            permission = "mytown.cmd.everyone.bank.info",
            parentName = "mytown.cmd.everyone.bank")
    public static CommandResponse bankAmountCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if(town instanceof AdminTown)
            throw new MyTownCommandException("mytown.cmd.err.adminTown", town.getName());

        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.bank.info", EconomyProxy.getCurrency(town.getBankAmount()), EconomyProxy.getCurrency(town.getNextPaymentAmount())));
        return CommandResponse.DONE;
    }

    @Command(
            name = "pay",
            permission = "mytown.cmd.everyone.bank.pay",
            parentName = "mytown.cmd.everyone.bank")
    public static CommandResponse bankPayCommand(ICommandSender sender, List<String> args) {
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
        return CommandResponse.DONE;
    }

    @Command(
            name = "wild",
            permission = "mytown.cmd.everyone.wild",
            parentName = "mytown.cmd")
    public static CommandResponse permWildCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "perm",
            permission = "mytown.cmd.everyone.wild.perm",
            parentName = "mytown.cmd.everyone.wild")
    public static CommandResponse permWildListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        res.sendMessage(Formatter.formatFlagsToString(Wild.instance));
        return CommandResponse.DONE;
    }
}