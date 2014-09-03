package mytown.commands;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.utils.Assert;
import mytown.core.utils.command.Command;
import mytown.core.utils.command.CommandNode;
import mytown.core.utils.config.ConfigProcessor;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.handlers.SafemodeHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * Created by AfterWind on 8/29/2014.
 * All commands for admins go here
 */
public class CommandsAdmin extends Commands {

    @Command(
            name = "townadmin",
            permission = "mytown.adm.cmd",
            alias = {"ta"})
    public static void townAdminCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.adm.cmd");
    }

    @CommandNode(
            name = "config",
            permission = "mytown.adm.cmd.config",
            parentName = "mytown.adm.cmd")
    public static void configCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.adm.cmd.config");
    }

    @CommandNode(
            name = "load",
            permission = "mytown.adm.cmd.config.load",
            parentName = "mytown.adm.cmd.config")
    public static void configLoadCommand(ICommandSender sender, List<String> args) {
        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.cmd.config.load.start");
        ConfigProcessor.load(MyTown.instance.config, Config.class);
        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.cmd.config.load.stop");
    }

    @CommandNode(
            name = "save",
            permission = "mytown.adm.cmd.config.save",
            parentName = "mytown.adm.cmd.config")
    public static void configSaveCommand(ICommandSender sender, List<String> args) {
        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.cmd.config.save.start");
        ConfigProcessor.save(MyTown.instance.config, Config.class);
        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.cmd.config.save.stop");
    }

    @CommandNode(
            name = "add",
            permission = "mytown.adm.cmd.add",
            parentName = "mytown.adm.cmd")
    public static void addCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            throw new WrongUsageException(getLocal().getLocalization("mytown.adm.cmd.usage.add"));

        Resident target = getResidentFromName(args.get(0));
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromName(args.get(1));

        if (town.hasResident(target))
            throw new CommandException(getLocal().getLocalization("mytown.adm.cmd.err.add.already", args.get(0), args.get(1)));

        Rank rank;

        if (args.size() > 2) {
            rank = getRankFromTown(town, args.get(2));
        } else {
            rank = town.getDefaultRank();
        }

        getDatasource().linkResidentToTown(target, town, rank);
        // TODO: add failed message ... too lazy right now :P
        // TODO: maybe too much info here
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.resident.add", args.get(0), args.get(1), args.size() > 2 ? args.get(2) : town.getDefaultRank().getName()));
        target.sendMessage(getLocal().getLocalization("mytown.notification.town.added", town.getName()));
    }
    @CommandNode(
            name = "delete",
            permission = "mytown.adm.cmd.delete",
            parentName = "mytown.adm.cmd")
    public static void deleteCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.adm.cmd.delete.usage"));

        Resident res = getDatasource().getOrMakeResident(sender);

        for (String s : args) {
            if (!getDatasource().hasTown(s))
                throw new CommandException(getLocal().getLocalization("mytown.cmd.err.town.notexist"), s);
        }
        for (String s : args) {
            if (getDatasource().deleteTown(getUniverse().getTownsMap().get(s))) {
                res.sendMessage(getLocal().getLocalization("mytown.notification.town.deleted", s));
            }
        }

    }

    @CommandNode(
            name = "new",
            permission = "mytown.adm.cmd.new",
            parentName = "mytown.adm.cmd")
    public static void newCommand(ICommandSender sender, List<String> args) {

        //TODO: make adminTown work properly

        EntityPlayer player = (EntityPlayer) sender;
        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.newtown"));
        if (getDatasource().hasTown(args.get(0))) // Is the town name already in use?
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.newtown.nameinuse", args.get(0)));
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ)) // Is the Block already claimed?   TODO Bit-shift the coords?
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.newtown.positionError"));

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getDatasource().newAdminTown(args.get(0), res); // Attempt to create the Town
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.newtown.failed"));

        if (!getDatasource().saveTown(town))
            throw new CommandException("Failed to save Town"); // TODO Localize!

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.created", town.getName()));
    }

    @CommandNode(
            name = "rem",
            permission = "mytown.adm.cmd.rem",
            parentName = "mytown.adm.cmd")
    public static void remCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 2)
            throw new WrongUsageException(getLocal().getLocalization("mytown.adm.cmd.usage.rem"));

        Resident res = getDatasource().getOrMakeResident(sender);
        Resident target = getResidentFromName(args.get(0));
        Town town = getTownFromName(args.get(1));

         if (!town.hasResident(target))
            throw new CommandException(getLocal().getLocalization("mytown.adm.cmd.err.rem.resident", args.get(0), args.get(1)));

        getDatasource().unlinkResidentFromTown(target, town);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.resident.remove", args.get(0), args.get(1)));
    }

    @CommandNode(
            name = "safemode",
            permission = "mytown.adm.cmd.safemode",
            parentName = "mytown.adm.cmd")
    public static void safemodeCommand(ICommandSender sender, List<String> args) {
        boolean safemode = false;
        if (args.size() < 1) { // Toggle safemode
            safemode = !SafemodeHandler.isInSafemode();
        } else { // Set safemode
            safemode = ChatUtils.equalsOn(args.get(0));
        }
        Assert.Perm(sender, "mytown.adm.cmd.safemode." + (safemode ? "on" : "off"));
        SafemodeHandler.setSafemode(safemode);
        SafemodeHandler.kickPlayers();
    }




}
