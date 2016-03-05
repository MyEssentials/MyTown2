package mytown.commands;


import mypermissions.command.api.CommandManager;
import mypermissions.command.api.CommandResponse;
import mypermissions.command.api.annotation.Command;
import mypermissions.command.core.entities.CommandTree;
import mypermissions.command.core.entities.CommandTreeNode;
import myessentials.utils.StringUtils;
import mytown.commands.format.ChatComponentPriceInfo;
import mytown.commands.format.ChatComponentResidentInfo;
import mytown.commands.format.ChatComponentTownInfo;
import mytown.commands.format.ChatComponentTownList;
import mytown.config.Config;
import mytown.new_datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.proxies.EconomyProxy;
import mytown.util.Formatter;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * All commands that can be accessed by everyone whether or not he's in a town
 */
public class CommandsOutsider extends Commands {
    @Command(
            name = "info",
            permission = "mytown.cmd.outsider.info",
            parentName = "mytown.cmd",
            syntax = "/town info [town]",
            completionKeys = {"townCompletionAndAll"},
            console = true)
    public static CommandResponse infoCommand(ICommandSender sender, List<String> args) {
        List<Town> towns = new ArrayList<Town>();

        if (args.size() < 1) {
            if (sender instanceof EntityPlayer) {
                Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
                towns.add(getTownFromResident(res));
            } else {
                throw new MyTownCommandException("You are not a player!");
            }
        } else {
            if ("@a".equals(args.get(0))) {
                towns = new ArrayList<Town>(getUniverse().towns);
                // TODO Sort
            } else {
                if(getTownFromName(args.get(0)) != null)
                    towns.add(getTownFromName(args.get(0)));
            }
        }

        for (Town town : towns) {
            new ChatComponentTownInfo(town).send(sender);
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "res",
            permission = "mytown.cmd.outsider.res",
            parentName = "mytown.cmd",
            syntax = "/town res <resident>",
            completionKeys = {"residentCompletion"},
            console = true)
    public static CommandResponse resCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Resident res = getResidentFromName(args.get(0));
        if (res == null) {
            throw new MyTownCommandException("mytown.cmd.err.resident.notexist", args.get(0));
        }

        new ChatComponentResidentInfo(res).send(sender);
        return CommandResponse.DONE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.outsider.list",
            parentName = "mytown.cmd",
            syntax = "/town list [page]",
            console = true)
    public static CommandResponse listCommand(ICommandSender sender, List<String> args) {
        int page = 1;
        if (args.size() >= 1) {
            page = Integer.parseInt(args.get(0));
        }
        if (page <= 0) page = 1;

        // TODO Find a way to cache this?
        ChatComponentTownList townList = new ChatComponentTownList(9, getUniverse().towns);
        townList.sendPage(sender, page);
        return CommandResponse.DONE;
    }

    @Command(
            name = "new",
            permission = "mytown.cmd.outsider.new",
            parentName = "mytown.cmd",
            syntax = "/town new <name>")
    public static CommandResponse newTownCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        EntityPlayer player = (EntityPlayer) sender;
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender); // Attempt to get or make the Resident

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.startedCreation", args.get(0)));

        if (res.townsContainer.size() >= Config.instance.maxTowns.get())
            throw new MyTownCommandException("mytown.cmd.err.resident.maxTowns");
        if (getUniverse().towns.contains(args.get(0))) // Is the town name already in use?
            throw new MyTownCommandException("mytown.cmd.err.newtown.nameinuse", args.get(0));
        if (getUniverse().blocks.contains(player.dimension, (int) player.posX >> 4, (int) player.posZ >> 4)) // Is the Block already claimed?
            throw new MyTownCommandException("mytown.cmd.err.newtown.positionError");
        for (int x = ((int) player.posX >> 4) - Config.instance.distanceBetweenTowns.get(); x <= ((int) player.posX >> 4) + Config.instance.distanceBetweenTowns.get(); x++) {
            for (int z = ((int) player.posZ >> 4) - Config.instance.distanceBetweenTowns.get(); z <= ((int) player.posZ >> 4) + Config.instance.distanceBetweenTowns.get(); z++) {
                Town nearbyTown = MyTownUtils.getTownAtPosition(player.dimension, x, z);
                if (nearbyTown != null && !(Boolean)nearbyTown.flagsContainer.getValue(FlagType.NEARBY))
                    throw new MyTownCommandException("mytown.cmd.err.newtown.tooClose", nearbyTown.getName(), Config.instance.distanceBetweenTowns.get());
            }
        }

        makePayment(player, Config.instance.costAmountMakeTown.get() + Config.instance.costAmountClaim.get());

        Town town = getUniverse().newTown(args.get(0), res); // Attempt to create the Town
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.newtown.failed");

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.created", town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "map",
            permission = "mytown.cmd.outsider.map",
            parentName = "mytown.cmd",
            syntax = "/town map [on|off]")
    public static CommandResponse mapCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        if (args.size() == 0) {
            Formatter.sendMap(res);
        } else {
            //res.setMapOn(args.get(0).equals("on"));
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "accept",
            permission = "mytown.cmd.outsider.accept",
            parentName = "mytown.cmd",
            syntax = "/town accept [town]",
            completionKeys = {"townCompletion"})
    public static CommandResponse acceptCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        List<Town> invites = getInvitesFromResident(res);
        Town town;
        if (args.size() == 0) {
            if(invites.size() > 1)
                throw new MyTownCommandException("mytown.cmd.err.invite.accept");
            town = invites.get(0);
        } else {
            town = getTownFromName(args.get(0));
            // Basically true only if player specifies a town that is not in its invites
            if (!invites.contains(town))
                throw new MyTownCommandException("mytown.cmd.err.invite.town.noinvitations");
        }
        if (res.townsContainer.size() >= Config.instance.maxTowns.get())
            throw new MyTownCommandException("mytown.cmd.err.resident.maxTowns");

        getDatasource().deleteTownInvite(res, town, true);

        // Notify everyone
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.invited.accept", town.getName()));
        town.notifyResidentJoin(res);
        return CommandResponse.DONE;
    }

    @Command(
            name = "refuse",
            permission = "mytown.cmd.outsider.refuse",
            parentName = "mytown.cmd",
            syntax = "/town refuse [town]",
            completionKeys = {"townCompletion"})
    public static CommandResponse refuseCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        List<Town> invites = getInvitesFromResident(res);
        Town town;
        if(invites.size() > 1)
            throw new MyTownCommandException("mytown.cmd.err.invite.refuse");

        if (args.size() == 0)
            town = invites.get(0);
        else
            town = getTownFromName(args.get(0));
        if (!invites.contains(town))
            throw new MyTownCommandException("mytown.cmd.err.invite.town.noinvitations");

        getDatasource().deleteTownInvite(res, town, false);

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.invited.refuse", town.getName()));
        return CommandResponse.DONE;
    }

    /*
    @Command(
            name = "friends",
            permission = "mytown.cmd.outsider.friends",
            parentName = "mytown.cmd",
            syntax = "/town friends <command>")
    public static CommandResponse friendsCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.outsider.friends.list",
            parentName = "mytown.cmd.outsider.friends",
            syntax = "/town friends list")
    public static CommandResponse friendsListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);

        String friends = null;
        for (Resident friend : res.getFriends()) {
            if (friends == null)
                friends = friend.getPlayerName();
            else
                friends += ", " + friend.getPlayerName();
        }

        res.sendMessage(getLocal().getLocalization("mytown.notification.resident.friends.list", friends));
        return CommandResponse.DONE;
    }

    @Command(
            name = "add",
            permission = "mytown.cmd.outsider.friends.add",
            parentName = "mytown.cmd.outsider.friends",
            syntax = "/town friends add <resident>",
            completionKeys = {"residentCompletion"})
    public static CommandResponse friendsAddCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));
        if (res == toAdd)
            throw new MyTownCommandException("mytown.cmd.err.friends.add.self");
        if(res.hasFriendRequest(toAdd))
            throw new MyTownCommandException("mytown.cmd.err.friends.add.already");

        getDatasource().saveFriendRequest(res, toAdd);
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.invitationSent"));
        toAdd.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotInvitation", res.getPlayerName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "mytown.cmd.outsider.friends.remove",
            parentName = "mytown.cmd.outsider.friends",
            syntax = "/town friends remove <resident>",
            completionKeys = {"residentCompletion"})
    public static CommandResponse friendsRemoveCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toRemove = getResidentFromName(args.get(0));
        if (!toRemove.removeFriend(res)) {
            throw new MyTownCommandException("mytown.cmd.err.friends.remove", toRemove.getPlayerName());
        } else {
            res.removeFriend(toRemove);
        }
        getDatasource().deleteFriendLink(res, toRemove);
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.removed"));
        toRemove.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotRemoved", res.getPlayerName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "accept",
            permission = "mytown.cmd.outsider.friends.accept",
            parentName = "mytown.cmd.outsider.friends",
            syntax = "/town friends accept <resident>",
            completionKeys = {"residentCompletion"})
    public static CommandResponse friendsAcceptCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));

        if(!getDatasource().deleteFriendRequest(toAdd, res)) {
            throw new MyTownCommandException("mytown.cmd.err.friends.accept", toAdd.getPlayerName());
        }

        getDatasource().saveFriendLink(res, toAdd);
        toAdd.sendMessage(getLocal().getLocalization("mytown.notification.friends.accepted", res.getPlayerName()));
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotAccepted", toAdd.getPlayerName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "refuse",
            permission = "mytown.cmd.outsider.friends.refuse",
            parentName = "mytown.cmd.outsider.friends",
            syntax = "/town friends refuse <resident>",
            completionKeys = {"residentCompletion"})
    public static CommandResponse friendsRefuseCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));

        getDatasource().deleteFriendRequest(toAdd, res);
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.refused", toAdd.getPlayerName()));
        toAdd.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotRefused", res.getPlayerName()));
        return CommandResponse.DONE;
    }
    */

    @Command(
            name = "help",
            permission = "mytown.cmd.outsider.help",
            parentName = "mytown.cmd",
            syntax = "/town help <command>",
            alias = {"?", "h"},
            console = true)
    public static CommandResponse helpCommand(ICommandSender sender, List<String> args) {
        int page = 1;
        if(!args.isEmpty() && StringUtils.tryParseInt(args.get(0)) && Integer.parseInt(args.get(0)) > 0) {
            page = Integer.parseInt(args.get(0));
            args = args.subList(1, args.size());
        }

        CommandTree tree = CommandManager.getTree("mytown.cmd");
        CommandTreeNode node = tree.getNodeFromArgs(args);
        node.sendHelpMessage(sender, page);
        return CommandResponse.DONE;
    }

    @Command(
            name = "syntax",
            permission = "mytown.cmd.outsider.syntax",
            parentName = "mytown.cmd",
            syntax = "/town syntax <command>",
            console = true)
    public static CommandResponse syntaxCommand(ICommandSender sender, List<String> args) {
        CommandTree tree = CommandManager.getTree("mytown.cmd");
        CommandTreeNode node = tree.getNodeFromArgs(args);
        node.sendSyntax(sender);
        return CommandResponse.DONE;
    }

    @Command(
            name = "invites",
            permission = "mytown.cmd.outsider.invites",
            parentName = "mytown.cmd",
            syntax = "/town invites")
    public static CommandResponse invitesCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        if (res.townInvitesContainer.size() == 0)
            res.sendMessage(getLocal().getLocalization("mytown.notification.resident.noInvites"));
        else {
            String formattedList = null;
            for (Town town : res.townInvitesContainer)
                if (formattedList == null)
                    formattedList = EnumChatFormatting.GREEN + town.getName() + EnumChatFormatting.WHITE;
                else
                    formattedList += ", " + EnumChatFormatting.GREEN + town.getName() + EnumChatFormatting.WHITE;
            res.sendMessage(getLocal().getLocalization("mytown.notification.resident.invites"));
            res.sendMessage(new ChatComponentText(formattedList));
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "prices",
            permission = "mytown.cmd.outsider.prices",
            parentName = "mytown.cmd",
            syntax = "/town prices")
    public static CommandResponse pricesCommand(ICommandSender sender, List<String> args) {
        Resident res = getUniverse().getOrMakeResident(sender);

        new ChatComponentPriceInfo().send(sender);

        return CommandResponse.DONE;
    }

}
