package mytown.commands;

import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandNode;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.util.Formatter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

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
            completionKeys = {"townCompletionAndAll"})
    public static void infoCommand(ICommandSender sender, List<String> args) {
        List<Town> towns = new ArrayList<Town>();

        Resident res = getDatasource().getOrMakeResident(sender);
        if (args.size() < 1) {
            towns.add(getTownFromResident(res));
        } else {
            if (args.get(0).equals("@a")) {
                towns = new ArrayList<Town>(getUniverse().getTownsMap().values());
                // TODO Sort
            } else {
                towns.add(getTownFromName(args.get(0)));
            }
        }

        for (Town town : towns) {
            res.sendMessage(Formatter.formatTownInfo(town));
        }
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.outsider.list",
            parentName = "mytown.cmd")
    public static void listCommand(ICommandSender sender, List<String> args) {
        //TODO: check if this works
        Resident res = getDatasource().getOrMakeResident(sender);

        String formattedTownList = null;
        for(Town town : getUniverse().getTownsMap().values()) {
            if(formattedTownList == null)
                formattedTownList = town.toString();
            else
                formattedTownList += "\n" + town.toString();
        }

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.list", formattedTownList));
    }

    @CommandNode(
            name = "new",
            permission = "mytown.cmd.outsider.new",
            parentName = "mytown.cmd")
    public static void newTownCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.newtown"));
        if (getDatasource().hasTown(args.get(0))) // Is the town name already in use?
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.newtown.nameinuse", args.get(0)));
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ)) // Is the Block already claimed?   TODO Bit-shift the coords?
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.newtown.positionError"));

        Resident res = getDatasource().getOrMakeResident(sender); // Attempt to get or make the Resident

        Town town = getDatasource().newTown(args.get(0), res); // Attempt to create the Town
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.newtown.failed"));

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
        if(args.size() == 0)
            town = invites.get(0);
        else
            town = getTownFromName(args.get(0));

        // Basically true only if player specifies a town that is not in its invites
        if (!invites.contains(town))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.invite.town.noinvitations"));

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
        if(args.size() == 0)
            town = invites.get(0);
        else
            town = getTownFromName(args.get(0));
        if (!invites.contains(town))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.invite.town.noinvitations"));

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
        for(Resident friend : res.getFriends()) {
            if(friends == null)
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
        if(args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.friends.add"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));
        if(!getDatasource().saveFriendRequest(res, toAdd)) {
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.friends.add"));
        }
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.invitationSent"));
    }

    @CommandNode(
            name = "remove",
            permission = "mytown.cmd.outsider.friends.remove",
            parentName = "mytown.cmd.outsider.friends",
            completionKeys = {"residentCompletion"})
    public static void friendsRemoveCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.friends.remove"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));
        if(!toAdd.removeFriend(res)) {
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.friends.add"));
        } else {
            res.removeFriend(toAdd);
        }
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.removed"));
    }

    @CommandNode(
            name = "accept",
            permission = "mytown.cmd.outsider.friends.accept",
            parentName = "mytown.cmd.outsider.friends",
            completionKeys = {"residentCompletion"})
    public static void friendsAcceptCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.friends.accept"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));

        getDatasource().deleteFriendRequest(res, toAdd, true);
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.accepted", toAdd.getPlayerName()));
    }

    @CommandNode(
            name = "refuse",
            permission = "mytown.cmd.outsider.friends.refuse",
            parentName = "mytown.cmd.outsider.friends",
            completionKeys = {"residentCompletion"})
    public static void friendsRefuseCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.friends.refuse"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident toAdd = getResidentFromName(args.get(0));

        getDatasource().deleteFriendRequest(res, toAdd, false);
        res.sendMessage(getLocal().getLocalization("mytown.notification.friends.refused", toAdd.getPlayerName()));
    }
}
