package mytown.commands;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandNode;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.util.Formatter;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
import mytown.util.exceptions.MyTownWrongUsageException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 9/9/2014.
 * All commands that can be accessed by everyone whether or not he's in a town
 */
public class CommandsOutsider extends Commands {
    @CommandNode(
            name = "info",
            permission = "mytown.cmd.outsider.info",
            parentName = "mytown.cmd",
            nonPlayers = true,
            completionKeys = {"townCompletionAndAll"})
    public static void infoCommand(ICommandSender sender, List<String> args) {
        List<Town> towns = new ArrayList<Town>();

        if (args.size() < 1) {
            if (sender instanceof EntityPlayer) {
                Resident res = getDatasource().getOrMakeResident(sender);
                towns.add(getTownFromResident(res));
            } else {
                throw new MyTownCommandException("You are not a player!");
            }
        } else {
            if (args.get(0).equals("@a")) {
                towns = new ArrayList<Town>(getUniverse().getTownsMap().values());
                // TODO Sort
            } else {
                towns.add(getTownFromName(args.get(0)));
            }
        }

        for (Town town : towns) {
            sendMessageBackToSender(sender, Formatter.formatTownInfo(town));
        }
    }

    @CommandNode(
            name = "res",
            permission = "mytown.cmd.outsider.res",
            parentName = "mytown.cmd",
            nonPlayers = true,
            completionKeys = {"residentCompletion"})
    public static void resCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            throw new MyTownWrongUsageException("mytown.cmd.usage.res");
        }
        Resident res = MyTownUniverse.getInstance().getResidentByName(args.get(0));
        if (res == null) {
            throw new MyTownCommandException("mytown.cmd.err.resident.notexist", args.get(0));
        }
        sendMessageBackToSender(sender, Formatter.formatResidentInfo(res));
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.outsider.list",
            parentName = "mytown.cmd",
            nonPlayers = true)
    public static void listCommand(ICommandSender sender, List<String> args) {
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.list", Formatter.formatTownsToString(MyTownUniverse.getInstance().getTownsMap().values())));
    }

    @CommandNode(
            name = "new",
            permission = "mytown.cmd.outsider.new",
            parentName = "mytown.cmd")
    public static void newTownCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.newtown");

        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(sender); // Attempt to get or make the Resident

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.startedCreation", args.get(0)));

        if (res.getTowns().size() >= Config.maxTowns)
            throw new MyTownCommandException("mytown.cmd.err.resident.maxTowns");
         if (getDatasource().hasTown(args.get(0))) // Is the town name already in use?
            throw new MyTownCommandException("mytown.cmd.err.newtown.nameinuse", args.get(0));
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ)) // Is the Block already claimed?
            throw new MyTownCommandException("mytown.cmd.err.newtown.positionError");
        for (int x = player.chunkCoordX - Config.distanceBetweenTowns; x <= player.chunkCoordX + Config.distanceBetweenTowns; x++) {
            for (int z = player.chunkCoordZ - Config.distanceBetweenTowns; z <= player.chunkCoordZ + Config.distanceBetweenTowns; z++) {
                if (MyTownUtils.getTownAtPosition(player.dimension, x, z) != null)
                    throw new MyTownCommandException("mytown.cmd.err.newtown.tooClose", Config.distanceBetweenTowns);
            }
        }

        Town town = getDatasource().newTown(args.get(0), res); // Attempt to create the Town
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.newtown.failed");

        if(!MyTownUtils.takeItemFromPlayer(player, Config.costItemName, Config.costAmountMakeTown))
            throw new MyTownCommandException("mytown.cmd.err.cost", Config.costAmountMakeTown, Config.costItemName);

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.created", town.getName()));
    }

    @CommandNode(
            name = "map",
            permission = "mytown.cmd.outsider.map",
            parentName = "mytown.cmd")
    public static void mapCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (args.size() == 0) {
            Formatter.sendMap(res);
        } else {
            res.setMapOn(ChatUtils.equalsOn(args.get(0)));
        }
    }

    @CommandNode(
            name = "accept",
            permission = "mytown.cmd.outsider.accept",
            parentName = "mytown.cmd",
            completionKeys = {"townCompletion"})
    public static void acceptCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        List<Town> invites = getInvitesFromResident(res);
        Town town;
        if (args.size() == 0)
            town = invites.get(0);
        else {
            town = getTownFromName(args.get(0));
            // Basically true only if player specifies a town that is not in its invites
            if (!invites.contains(town))
                throw new MyTownCommandException("mytown.cmd.err.invite.town.noinvitations");
        }
        if (res.getTowns().size() >= Config.maxTowns)
            throw new MyTownCommandException("mytown.cmd.err.resident.maxTowns");

        getDatasource().deleteTownInvite(res, town, true);

        // Notify everyone
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.invited.accept", town.getName()));
        town.notifyResidentJoin(res);
    }

    @CommandNode(
            name = "refuse",
            permission = "mytown.cmd.outsider.refuse",
            parentName = "mytown.cmd",
            completionKeys = {"townCompletion"})
    public static void refuseCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);

        List<Town> invites = getInvitesFromResident(res);
        Town town;
        if (args.size() == 0)
            town = invites.get(0);
        else
            town = getTownFromName(args.get(0));
        if (!invites.contains(town))
            throw new MyTownCommandException("mytown.cmd.err.invite.town.noinvitations");

        getDatasource().deleteTownInvite(res, town, false);

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.invited.refuse", town.getName()));
    }

    @CommandNode(
            name = "friends",
            permission = "mytown.cmd.outsider.friends",
            parentName = "mytown.cmd")
    public static void friendsCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.outsider.friends");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.outsider.friends.list",
            parentName = "mytown.cmd.outsider.friends")
    public static void friendsListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);

        String friends = null;
        for (Resident friend : res.getFriends()) {
            if (friends == null)
                friends = friend.getPlayerName();
            else
                friends += ", " + friend.getPlayerName();
        }

        res.sendMessage(getLocal().getLocalization("mytown.notification.resident.friends.list", friends));
    }

    @CommandNode(
            name = "add",
            permission = "mytown.cmd.outsider.friends.add",
            parentName = "mytown.cmd.outsider.friends",
            completionKeys = {"residentCompletion"})
    public static void friendsAddCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.friends.add");
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));
        if (res == toAdd)
            throw new MyTownCommandException("mytown.cmd.err.friends.add.self");
        if (!getDatasource().saveFriendRequest(res, toAdd)) {
            throw new MyTownCommandException("mytown.cmd.err.friends.add.already");
        }
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.invitationSent"));
        toAdd.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotInvitation", res.getPlayerName()));
    }

    @CommandNode(
            name = "remove",
            permission = "mytown.cmd.outsider.friends.remove",
            parentName = "mytown.cmd.outsider.friends",
            completionKeys = {"residentCompletion"})
    public static void friendsRemoveCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.friends.remove");
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));
        if (!toAdd.removeFriend(res)) {
            throw new MyTownCommandException("mytown.cmd.err.friends.remove");
        } else {
            res.removeFriend(toAdd);
        }
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.removed"));
        toAdd.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotRemoved", res.getPlayerName()));
    }

    @CommandNode(
            name = "accept",
            permission = "mytown.cmd.outsider.friends.accept",
            parentName = "mytown.cmd.outsider.friends",
            completionKeys = {"residentCompletion"})
    public static void friendsAcceptCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownCommandException("mytown.cmd.usage.friends.accept");
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));

        getDatasource().deleteFriendRequest(res, toAdd, true);
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.accepted", toAdd.getPlayerName()));
        toAdd.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotAccepted", res.getPlayerName()));
    }

    @CommandNode(
            name = "refuse",
            permission = "mytown.cmd.outsider.friends.refuse",
            parentName = "mytown.cmd.outsider.friends",
            completionKeys = {"residentCompletion"})
    public static void friendsRefuseCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownCommandException("mytown.cmd.usage.friends.refuse");
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));

        getDatasource().deleteFriendRequest(res, toAdd, false);
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.refused", toAdd.getPlayerName()));
        toAdd.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotRefused", res.getPlayerName()));
    }

    @CommandNode(
            name = "help",
            permission = "mytown.cmd.outsider.help",
            parentName = "mytown.cmd",
            nonPlayers = true)
    public static void helpCommand(ICommandSender sender, List<String> args) {
        sendHelpMessageWithArgs(sender, args, "mytown.cmd");
    }

    @CommandNode(
            name = "invites",
            permission = "mytown.cmd.outsider.invites",
            parentName = "mytown.cmd")
    public static void invitesCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res.getInvites().size() == 0)
            res.sendMessage(getLocal().getLocalization("mytown.notification.resident.noInvites"));
        else {
            String formattedList = null;
            for (Town town : res.getInvites())
                if (formattedList == null)
                    formattedList = EnumChatFormatting.GREEN + town.getName() + EnumChatFormatting.WHITE;
                else
                    formattedList += ", " + EnumChatFormatting.GREEN + town.getName() + EnumChatFormatting.WHITE;
            res.sendMessage(getLocal().getLocalization("mytown.notification.resident.invites"));
            res.sendMessage(formattedList);
        }
    }
}
