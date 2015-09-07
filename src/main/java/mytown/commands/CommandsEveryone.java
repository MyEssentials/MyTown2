package mytown.commands;

import myessentials.utils.ChatUtils;
import myessentials.utils.ColorUtils;
import myessentials.utils.StringUtils;
import mypermissions.api.command.CommandResponse;
import mypermissions.api.command.annotation.Command;
import mytown.config.Config;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.entities.tools.PlotSelectionTool;
import mytown.entities.tools.PlotSellTool;
import mytown.entities.tools.Tool;
import mytown.entities.tools.WhitelisterTool;
import mytown.proxies.EconomyProxy;
import mytown.util.exceptions.MyTownCommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Iterator;
import java.util.List;

/**
 * Process methods for all commands that can be used by everyone
 */
public class CommandsEveryone extends Commands {

    @Command(
            name = "mytown",
            permission = "mytown.cmd",
            alias = {"t", "town"},
            syntax = "/town <command>")
    public static CommandResponse townCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "leave",
            permission = "mytown.cmd.everyone.leave",
            parentName = "mytown.cmd",
            syntax = "/town leave [delete]")
    public static CommandResponse leaveCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if (town.residentsMap.get(res) != null && town.residentsMap.get(res).getType() == Rank.Type.MAYOR) {
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
            syntax = "/town spawn [town]",
            completionKeys = {"townCompletion"})
    public static CommandResponse spawnCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer)sender;
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
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
        town.bank.addAmount(amount);
        getDatasource().saveTownBank(town.bank);
        town.sendToSpawn(res);
        return CommandResponse.DONE;
    }

    @Command(
            name = "select",
            permission = "mytown.cmd.everyone.select",
            parentName = "mytown.cmd",
            syntax = "/town select <town>",
            completionKeys = {"townCompletion"})
    public static CommandResponse selectCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));
        if (!town.residentsMap.containsKey(res))
            throw new MyTownCommandException("mytown.cmd.err.select.notpart", args.get(0));
        getDatasource().saveSelectedTown(res, town);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.select", args.get(0)));
        return CommandResponse.DONE;
    }


    @Command(
            name = "blocks",
            permission = "mytown.cmd.everyone.blocks",
            parentName = "mytown.cmd",
            syntax = "/town blocks <command>")
    public static CommandResponse blocksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.everyone.blocks.list",
            parentName = "mytown.cmd.everyone.blocks",
            syntax = "/town blocks list")
    public static CommandResponse blocksListCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.block.list", town.getName(), "\\n" + town.townBlocksContainer.toString()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "info",
            permission = "mytown.cmd.everyone.blocks.info",
            parentName = "mytown.cmd.everyone.blocks",
            syntax = "/town blocks info")
    public static CommandResponse blocksInfoCommand(ICommandSender sender, List<String> args) {
        Resident res = getUniverse().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        String blocks = town.townBlocksContainer.size() + "/" + town.getMaxBlocks();
        String extraBlocks = town.getExtraBlocks() + "\\n";
        String dash = ColorUtils.colorInfoText + " - ";
        extraBlocks += dash + "TOWN (" + town.townBlocksContainer.getExtraBlocks() + ")\\n";
        for(Iterator<Resident> it = town.residentsMap.keySet().iterator(); it.hasNext();) {
            Resident resInTown = it.next();
            extraBlocks += dash + ColorUtils.colorInfoText + resInTown.getPlayerName() + " (" + resInTown.getExtraBlocks() + ")";
            if(it.hasNext()) {
                extraBlocks += "\\n";
            }
        }

        String farBlocks = town.townBlocksContainer.getFarClaims() + "/" + town.townBlocksContainer.getMaxFarClaims();

        res.sendMessage(getLocal().getLocalization("mytown.notification.blocks.info", blocks, extraBlocks, farBlocks));

        return CommandResponse.DONE;
    }

    @Command(
            name = "perm",
            permission = "mytown.cmd.everyone.perm",
            parentName = "mytown.cmd",
            syntax = "/town perm <command>")
    public static CommandResponse permCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.everyone.perm.list",
            parentName = "mytown.cmd.everyone.perm",
            syntax = "/town perm list")
    public static CommandResponse permListCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        res.sendMessage(town.flagsContainer.toStringForTowns());
        return CommandResponse.DONE;
    }

    public static class Plots {

        @Command(
                name = "perm",
                permission = "mytown.cmd.everyone.plot.perm",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot perm <command>")
        public static CommandResponse plotPermCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "set",
                permission = "mytown.cmd.everyone.plot.perm.set",
                parentName = "mytown.cmd.everyone.plot.perm",
                syntax = "/town plot perm set <flag> <value>",
                completionKeys = {"flagCompletion"})
        public static CommandResponse plotPermSetCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            if (!plot.ownersContainer.contains(res) && !plot.getTown().hasPermission(res, "mytown.bypass.plot")) {
                throw new MyTownCommandException("mytown.cmd.err.plot.perm.set.noPermission");
            }

            Flag flag = getFlagFromName(plot.flagsContainer, args.get(0));

            if (flag.setValueFromString(args.get(1))) {
                ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.perm.set.success", args.get(0), args.get(1));
            } else {
                throw new MyTownCommandException("mytown.cmd.err.perm.valueNotValid", args.get(1));
            }

            getDatasource().saveFlag(flag, plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "list",
                permission = "mytown.cmd.everyone.plot.perm.list",
                parentName = "mytown.cmd.everyone.plot.perm",
                syntax = "/town plot perm list")
        public static CommandResponse plotPermListCommand(ICommandSender sender, List<String> args) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            res.sendMessage(plot.flagsContainer.toStringForPlot(plot.getTown()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "whitelist",
                permission = "mytown.cmd.everyone.plot.perm.whitelist",
                parentName = "mytown.cmd.everyone.plot.perm",
                syntax = "/town plot perm whitelist")
        public static CommandResponse plotPermWhitelistCommand(ICommandSender sender, List<String> args) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);

            res.toolContainer.set(new WhitelisterTool(res));
            res.sendMessage(getLocal().getLocalization("mytown.notification.perm.whitelist.start"));
            return CommandResponse.DONE;
        }

        @Command(
                name = "plot",
                permission = "mytown.cmd.everyone.plot",
                parentName = "mytown.cmd",
                syntax = "/town plot <command>")
        public static CommandResponse plotCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "rename",
                permission = "mytown.cmd.everyone.plot.rename",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot rename <name>")
        public static CommandResponse plotRenameCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);

            if (!plot.ownersContainer.contains(res) && !plot.getTown().hasPermission(res, "mytown.bypass.plot")) {
                throw new MyTownCommandException("mytown.cmd.err.plot.perm.set.noPermission");
            }

            plot.setName(args.get(0));
            getDatasource().savePlot(plot);

            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.renamed"));
            return CommandResponse.DONE;
        }

        @Command(
                name = "new",
                permission = "mytown.cmd.everyone.plot.new",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot new <plot>")
        public static CommandResponse plotNewCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            res.toolContainer.set(new PlotSelectionTool(res, args.get(0)));
            return CommandResponse.DONE;
        }

        @Command(
                name = "select",
                permission = "mytown.cmd.everyone.plot.select",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot select <command>")
        public static CommandResponse plotSelectCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "reset",
                permission = "mytown.cmd.everyone.plot.select.reset",
                parentName = "mytown.cmd.everyone.plot.select",
                syntax = "/town plot select reset")
        public static CommandResponse plotSelectResetCommand(ICommandSender sender, List<String> args) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Tool currentTool = res.toolContainer.get();
            if(currentTool == null || !(currentTool instanceof PlotSelectionTool))
                throw new MyTownCommandException("mytown.cmd.err.plot.noPermission");
            ((PlotSelectionTool) currentTool).resetSelection(true, 0);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.selectionReset"));
            return CommandResponse.DONE;
        }

        @Command(
                name = "show",
                permission = "mytown.cmd.everyone.plot.show",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot show")
        public static CommandResponse plotShowCommand(ICommandSender sender, List<String> args) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            town.plotsContainer.show(res);
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.showing");
            return CommandResponse.DONE;
        }

        @Command(
                name = "hide",
                permission = "mytown.cmd.everyone.plot.hide",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot hide")
        public static CommandResponse plotHideCommand(ICommandSender sender, List<String> args) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            town.plotsContainer.hide(res);
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.vanished");
            return CommandResponse.DONE;
        }

        @Command(
                name = "add",
                permission = "mytown.cmd.everyone.plot.add",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot add <command>")
        public static CommandResponse plotAddCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "owner",
                permission = "mytown.cmd.everyone.plot.add.owner",
                parentName = "mytown.cmd.everyone.plot.add",
                syntax = "/town plot add owner <resident>",
                completionKeys = {"residentCompletion"})
        public static CommandResponse plotAddOwnerCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Resident target = getResidentFromName(args.get(0));

            Town town = getTownFromResident(res);
            if (!target.townsContainer.contains(town)) {
                throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", target.getPlayerName(), town.getName());
            }

            Plot plot = getPlotAtResident(res);

            if(!plot.ownersContainer.contains(res) && !town.hasPermission(res, "mytown.bypass.plot")) {
                throw new MyTownCommandException("mytown.cmd.err.plot.noPermission");
            }

            if(plot.ownersContainer.contains(target) || plot.membersContainer.contains(target)) {
                throw new MyTownCommandException("mytown.cmd.err.plot.add.alreadyInPlot");
            }

            if (!town.plotsContainer.canResidentMakePlot(target)) {
                throw new MyTownCommandException("mytown.cmd.err.plot.limit.toPlayer", target.getPlayerName());
            }

            getDatasource().linkResidentToPlot(target, plot, true);

            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.owner.sender.added", target.getPlayerName(), plot.getName()));
            target.sendMessage(getLocal().getLocalization("mytown.notification.plot.owner.target.added", plot.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "member",
                permission = "mytown.cmd.everyone.plot.add.member",
                parentName = "mytown.cmd.everyone.plot.add",
                syntax = "/town plot add member <resident>",
                completionKeys = {"residentCompletion"})
        public static CommandResponse plotAddMemberCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Resident target = getResidentFromName(args.get(0));
            Plot plot = getPlotAtResident(res);

            if(!plot.ownersContainer.contains(res) && !plot.getTown().hasPermission(res, "mytown.bypass.plot")) {
                throw new MyTownCommandException("mytown.cmd.err.plot.notOwner");
            }

            if(plot.ownersContainer.contains(target) || plot.membersContainer.contains(target)) {
                throw new MyTownCommandException("mytown.cmd.err.plot.add.alreadyInPlot");
            }

            getDatasource().linkResidentToPlot(target, plot, false);

            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.member.sender.added", target.getPlayerName(), plot.getName()));
            target.sendMessage(getLocal().getLocalization("mytown.notification.plot.member.target.added", plot.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "mytown.cmd.everyone.plot.remove",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot remove <resident>",
                completionKeys = {"residentCompletion"})
        public static CommandResponse plotRemoveCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Resident target = getResidentFromName(args.get(0));
            Plot plot = getPlotAtResident(res);

            if(!plot.ownersContainer.contains(res) && !plot.getTown().hasPermission(res, "mytown.bypass.plot")) {
                throw new MyTownCommandException("mytown.cmd.err.plot.noPermission");
            }

            if(!plot.ownersContainer.contains(target) && !plot.membersContainer.contains(target)) {
                throw new MyTownCommandException("mytown.cmd.err.plot.remove.notInPlot");
            }

            if(plot.ownersContainer.contains(target) && plot.ownersContainer.size() == 1) {
                throw new MyTownCommandException("mytown.cmd.err.plot.remove.onlyOwner");
            }

            getDatasource().unlinkResidentFromPlot(target, plot);

            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.sender.removed", target.getPlayerName(), plot.getName()));
            target.sendMessage(getLocal().getLocalization("mytown.notification.plot.target.removed", plot.getName()));
            return CommandResponse.DONE;

        }

        @Command(
                name = "info",
                permission = "mytown.cmd.everyone.plot.info",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot info")
        public static CommandResponse plotInfoCommand(ICommandSender sender, List<String> args) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.info", plot.getName(), plot.ownersContainer.toString(), plot.membersContainer.toString(), plot.getStartX(), plot.getStartY(), plot.getStartZ(), plot.getEndX(), plot.getEndY(), plot.getEndZ()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "delete",
                permission = "mytown.cmd.everyone.plot.delete",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot delete")
        public static CommandResponse plotDeleteCommand(ICommandSender sender, List<String> args) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Plot plot = getPlotAtResident(res);
            if (!plot.ownersContainer.contains(res) && !plot.getTown().hasPermission(res, "mytown.bypass.plot")) {
                throw new MyTownCommandException("mytown.cmd.err.plot.noPermission");
            }

            getDatasource().deletePlot(plot);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.deleted", plot.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "sell",
                permission = "mytown.cmd.everyone.plot.sell",
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot sell <price>")
        public static CommandResponse plotSellCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            if(!StringUtils.tryParseInt(args.get(0)) || Integer.parseInt(args.get(0)) < 0) {
                throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));
            }

            int price = Integer.parseInt(args.get(0));
            res.toolContainer.set(new PlotSellTool(res, price));
            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "ranks",
            permission = "mytown.cmd.everyone.ranks",
            parentName = "mytown.cmd",
            syntax = "/town ranks <command>")
    public static CommandResponse ranksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.everyone.ranks.list",
            parentName = "mytown.cmd.everyone.ranks",
            syntax = "/town ranks list")
    public static CommandResponse listRanksCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.ranks", town.ranksContainer.toString());
        return CommandResponse.DONE;
    }

    @Command(
            name = "borders",
            permission = "mytown.cmd.everyone.borders",
            parentName = "mytown.cmd",
            syntax = "/town borders <command>")
    public static CommandResponse bordersCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "show",
            permission = "mytown.cmd.everyone.borders.show",
            parentName = "mytown.cmd.everyone.borders",
            syntax = "/town borders show")
    public static CommandResponse bordersShowCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        town.townBlocksContainer.show(res);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.borders.show", town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "hide",
            permission = "mytown.cmd.everyone.borders.hide",
            parentName = "mytown.cmd.everyone.borders",
            syntax = "/town borders hide")
    public static CommandResponse bordersHideCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        town.townBlocksContainer.hide(res);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.borders.hide"));
        return CommandResponse.DONE;
    }

    @Command(
            name = "bank",
            permission = "mytown.cmd.everyone.bank",
            parentName = "mytown.cmd",
            syntax = "/town bank <command>")
    public static CommandResponse bankCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "info",
            permission = "mytown.cmd.everyone.bank.info",
            parentName = "mytown.cmd.everyone.bank",
            syntax = "/town bank info")
    public static CommandResponse bankAmountCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if(town instanceof AdminTown)
            throw new MyTownCommandException("mytown.cmd.err.adminTown", town.getName());

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.bank.info", EconomyProxy.getCurrency(town.bank.getAmount()), EconomyProxy.getCurrency(town.bank.getNextPaymentAmount())));
        return CommandResponse.DONE;
    }

    @Command(
            name = "deposit",
            permission = "mytown.cmd.everyone.bank.deposit",
            parentName = "mytown.cmd.everyone.bank",
            syntax = "/town bank deposit <amount>")
    public static CommandResponse bankPayCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        if(!StringUtils.tryParseInt(args.get(0)))
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));

        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if(town instanceof AdminTown)
            throw new MyTownCommandException("mytown.cmd.err.adminTown", town.getName());

        int amount = Integer.parseInt(args.get(0));
        makePayment(res.getPlayer(), amount);
        town.bank.addAmount(amount);
        getDatasource().saveTownBank(town.bank);
        return CommandResponse.DONE;
    }

    @Command(
            name = "wild",
            permission = "mytown.cmd.everyone.wild",
            parentName = "mytown.cmd",
            syntax = "/town wild <command>")
    public static CommandResponse permWildCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "perm",
            permission = "mytown.cmd.everyone.wild.perm",
            parentName = "mytown.cmd.everyone.wild",
            syntax = "/town wild perm")
    public static CommandResponse permWildListCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        res.sendMessage(Wild.instance.flagsContainer.toStringForWild());
        return CommandResponse.DONE;
    }
}