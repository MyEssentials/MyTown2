package mytown.commands;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.utils.Assert;
import mytown.core.utils.command.Command;
import mytown.core.utils.command.CommandNode;
import mytown.core.utils.config.ConfigProcessor;
import mytown.handlers.SafemodeHandler;
import mytown.proxies.LocalizationProxy;
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
        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.config.load.start");
        ConfigProcessor.load(MyTown.instance.config, Config.class);
        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.config.load.stop");
    }

    @CommandNode(
            name = "save",
            permission = "mytown.adm.cmd.config.save",
            parentName = "mytown.adm.cmd.config")
    public static void configSaveCommand(ICommandSender sender, List<String> args) {
        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.config.save.start");
        ConfigProcessor.save(MyTown.instance.config, Config.class);
        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.config.save.stop");
    }

    @CommandNode(
            name = "add",
            permission = "mytown.adm.cmd.add",
            parentName = "mytown.adm.cmd")
    public static void addCommand(ICommandSender sender, List<String> args) {
        /*
        if (args.size() < 2)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.adm.cmd.usage.add"));
        if (!getDatasource().hasTown(args[1]))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.town.notexist", args[1]));
        if (!getDatasource().hasResident(args.get(0)))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.resident.notexist", args.get(0)));
        if (getDatasource().getTown(args[1]).hasResident(getDatasource().getResident(args.get(0))))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.adm.cmd.err.add.already", (Object[]) args));
        Rank rank;
        if (args.size() > 2) {
            if (!getDatasource().getTown(args[1]).hasRankName(args[2]))
                throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.rank.notexist", args[1], args[2]));
            rank = getDatasource().getRank(args[2], getDatasource().getTown(args[1]));
        } else {
            rank = getDatasource().getRank("Resident", getDatasource().getTown(args[1]));
        }

        try {
            getDatasource().linkResidentToTown(getDatasource().getResident(args.get(0)), getDatasource().getTown(args[1]), rank);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.resident.add", (Object[]) args);
        */
    }
    @CommandNode(
            name = "delete",
            permission = "mytown.adm.cmd.delete",
            parentName = "mytown.adm.cmd")
    public static void deleteCommand(ICommandSender sender, List<String> args) {
        /*
        if (args.size() < 1)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.adm.cmd.delete.usage"));
        if (!getDatasource().hasTown(args.get(0)))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist"), args.get(0));

        try {
            if (args.size() == 1) {
                if (getDatasource().deleteTown(getDatasource().getTown(args.get(0)))) {
                    ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.deleted", args.get(0));
                }
            } else {
                for (String s : args)
                    if (getDatasource().getTown(s) == null)
                        throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist"), s);
                for (String s : args) {
                    if (getDatasource().deleteTown(getDatasource().getTown(s))) {
                        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.deleted", s);
                    }
                }
            }
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
            e.printStackTrace();
        }
        */
    }

    @CommandNode(
            name = "new",
            permission = "mytown.adm.cmd.new",
            parentName = "mytown.adm.cmd")
    public static void newCommand(ICommandSender sender, List<String> args) {
        /*
        if (args.size() < 1)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.newtown"));
        if (getDatasource().hasTown(args.get(0)))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.newtown.nameinuse", (Object[]) args));
        try {
            AdminTown town = new AdminTown(args.get(0));
            Resident res = getDatasource().getOrMakeResident(sender.getCommandSenderName());
            EntityPlayer player = (EntityPlayer) sender;
            getDatasource().insertTown(town);
            getDatasource().insertTownBlock(new TownBlock(town, player.chunkCoordX, player.chunkCoordZ, player.dimension));
            getDatasource().insertTownFlag(town, new TownFlag("mobs", "Controls mobs spawning", true));
            getDatasource().insertTownFlag(town, new TownFlag("breakBlocks", "Controls whether or not non-residents can break blocks", false));
            getDatasource().insertTownFlag(town, new TownFlag("explosions", "Controls if explosions can occur", true));
            getDatasource().insertTownFlag(town, new TownFlag("accessBlocks", "Controls whether or not non-residents can access(right click) blocks", false));
            getDatasource().insertTownFlag(town, new TownFlag("enter", "Controls whether or not a non-resident can enter the town", true));
            getDatasource().insertTownFlag(town, new TownFlag("pickup", "Controls whether or not a non-resident can pick up items", true));

            res.sendLocalizedMessage(MyTown.getLocal(), "mytown.notification.admtown.created", town.getName());
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError")); // Notify players

            e.printStackTrace();
        }
        */
    }

    @CommandNode(
            name = "rem",
            permission = "mytown.adm.cmd.rem",
            parentName = "mytown.adm.cmd")
    public static void remCommand(ICommandSender sender, List<String> args) {
        /*
        if (args.size() < 2)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.adm.cmd.usage.rem"));
        if (!getDatasource().hasResident(args.get(0)))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.resident.notexist", args.get(0)));
        if (!getDatasource().hasTown(args[1]))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[1]));
        if (!getDatasource().getTown(args[1]).hasResident(getDatasource().getResident(args.get(0))))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.adm.cmd.err.rem.resident", (Object[]) args));
        try {
            getDatasource().unlinkResidentFromTown(getDatasource().getResident(args.get(0)), getDatasource().getTown(args[1]));
            ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.resident.remove", (Object[]) args);
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
            e.printStackTrace();
        }
        */
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
