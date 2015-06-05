package mytown.commands;


import java.util.ArrayList;
import java.util.List;

import mytown.config.Config;
import mytown.core.utils.ChatUtils;
import mytown.core.command.CommandNode;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.util.Formatter;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
import mytown.util.exceptions.MyTownWrongUsageException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

/**
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
            if ("@a".equals(args.get(0))) {
                towns = new ArrayList<Town>(getUniverse().getTownsMap().values());
                // TODO Sort
            } else {
                if(getTownFromName(args.get(0)) != null)
                    towns.add(getTownFromName(args.get(0)));
            }
        }

        for (Town town : towns) {
            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.info", town.getName(), town.getResidents().size(), town.getBlocks().size(), town.getMaxBlocks(), town.getPlots().size(), Formatter.formatResidentsToString(town), Formatter.formatRanksToString(town.getRanks())));
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
        Resident res = getResidentFromName(args.get(0));
        if (res == null) {
            throw new MyTownCommandException("mytown.cmd.err.resident.notexist", args.get(0));
        }
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.resident.info", res.getPlayerName(), Formatter.formatTownsToString(res), Formatter.formatDate(res.getJoinDate()), Formatter.formatDate(res.getLastOnline())));
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.outsider.list",
            parentName = "mytown.cmd",
            nonPlayers = true)
    public static void listCommand(ICommandSender sender, List<String> args) {
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.list", Formatter.formatTownsToString(getUniverse().getTownsMap().values())));
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
                Town nearbyTown = MyTownUtils.getTownAtPosition(player.dimension, x, z);
                if (nearbyTown != null && !(Boolean)nearbyTown.getValue(FlagType.NEARBY))
                    throw new MyTownCommandException("mytown.cmd.err.newtown.tooClose", nearbyTown.getName(), Config.distanceBetweenTowns);
            }
        }

        makePayment(player, Config.costAmountMakeTown + Config.costAmountClaim);

        Town town = getDatasource().newTown(args.get(0), res); // Attempt to create the Town
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.newtown.failed");

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
        if(invites.size() > 1)
            throw new MyTownCommandException("mytown.cmd.err.invite.accept");

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
    }

    @CommandNode(
            name = "friends",
            permission = "mytown.cmd.outsider.friends",
            parentName = "mytown.cmd")
    public static void friendsCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.outsider.friends");
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
        if(res.hasFriendRequest(toAdd))
            throw new MyTownCommandException("mytown.cmd.err.friends.add.already");

        getDatasource().saveFriendRequest(res, toAdd);
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
        Resident toRemove = getResidentFromName(args.get(0));
        if (!toRemove.removeFriend(res)) {
            throw new MyTownCommandException("mytown.cmd.err.friends.remove", toRemove.getPlayerName());
        } else {
            res.removeFriend(toRemove);
        }
        getDatasource().deleteFriendLink(res, toRemove);
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.removed"));
        toRemove.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotRemoved", res.getPlayerName()));
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

        getDatasource().deleteFriendRequest(toAdd, res);
        getDatasource().saveFriendLink(res, toAdd);
        toAdd.sendMessage(getLocal().getLocalization("mytown.notification.friends.accepted", res.getPlayerName()));
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotAccepted", toAdd.getPlayerName()));
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

        getDatasource().deleteFriendRequest(toAdd, res);
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.refused", toAdd.getPlayerName()));
        toAdd.sendMessage(getLocal().getLocalization("mytown.notification.friends.gotRefused", res.getPlayerName()));
    }

    @CommandNode(
            name = "help",
            permission = "mytown.cmd.outsider.help",
            parentName = "mytown.cmd",
            nonPlayers = true)
    public static void helpCommand(ICommandSender sender, List<String> args) {
        sendHelpMessage(sender, "mytown.cmd", args);
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
