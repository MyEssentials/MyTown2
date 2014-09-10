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
            res.sendMessage(town.getTownInfo());
        }
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

        //TODO: Database stuff...
        res.removeInvite(town);

        // Notify everyone
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.invited.accept", town.getName()));
        town.notifyResidentJoin(res);

        // Link Resident to Town
        getDatasource().linkResidentToTown(res, town, town.getDefaultRank());
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

        // TODO: notify resident refused invite

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.invited.refuse", town.getName()));
        res.removeInvite(town.getName());
    }
}
