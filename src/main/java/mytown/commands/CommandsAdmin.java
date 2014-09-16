package mytown.commands;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.utils.Assert;
import mytown.core.utils.command.Command;
import mytown.core.utils.command.CommandNode;
import mytown.core.utils.config.ConfigProcessor;
import mytown.entities.Block;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.handlers.SafemodeHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Iterator;
import java.util.List;

/**
 * Created by AfterWind on 8/29/2014.
 * All commands for admins go here
 */
public class CommandsAdmin extends Commands {

    @Command(
            name = "townadmin",
            permission = "mytown.adm.cmd",
            alias = {"ta"},
            opsOnlyAccess = true)
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
            parentName = "mytown.adm.cmd",
            completionKeys = {"residentCompletion", "townCompletion"})
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
            parentName = "mytown.adm.cmd",
            completionKeys = {"townCompletion"})
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
            parentName = "mytown.adm.cmd",
            completionKeys = {"residentCompletion", "townCompletion"})
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

    @CommandNode(
            name = "db",
            permission = "mytown.adm.cmd.db",
            parentName = "mytown.adm.cmd")
    public static void dbCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.adm.cmd.db");
    }

    @CommandNode(
            name = "purge",
            permission = "mytown.adm.cmd.db.purge",
            parentName = "mytown.adm.cmd.db")
    public static void dbCommandPurge(ICommandSender sender, List<String> args) {
        for(Iterator<Town> it = getUniverse().getTownsMap().values().iterator(); it.hasNext(); ) {
            getDatasource().deleteTown(it.next());
        }
        for(Iterator<Resident> it = getUniverse().getResidentsMap().values().iterator(); it.hasNext(); ) {
            getDatasource().deleteResident(it.next());
        }
        Resident res = getDatasource().getOrMakeResident(sender);
        res.sendMessage(getLocal().getLocalization("mytown.notification.db.purging"));
    }

    @CommandNode(
            name = "perm",
            permission = "mytown.adm.cmd.perm",
            parentName = "mytown.adm.cmd")
    public static void permCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.adm.cmd.perm");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.adm.cmd.perm.list",
            parentName = "mytown.adm.cmd.perm",
            completionKeys = {"townCompletion"})
    public static void permListCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1) {
            throw new WrongUsageException(getLocal().getLocalization("mytown.adm.cmd.usage.perm.list"));
        }
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));

        String formattedFlagList = null;
        for (Flag flag : town.getFlags()) {
            if (formattedFlagList == null) {
                formattedFlagList = "";
            } else {
                formattedFlagList += '\n';
            }
            formattedFlagList += flag;
        }
        if(formattedFlagList != null)
            res.sendMessage(formattedFlagList);
        else
            res.sendMessage(getLocal().getLocalization("mytown.cmd.err.flag.list"));
    }

    @CommandNode(
            name = "set",
            permission = "mytown.adm.cmd.perm.set",
            parentName = "mytown.adm.cmd.perm",
            completionKeys = {"townCompletion", "flagCompletion"})
    public static void permSetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 3) {
            throw new WrongUsageException(getLocal().getLocalization("mytown.adm.cmd.usage.perm.set"));
        }

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));
        Flag flag = getFlagFromTown(town, args.get(1));

        if (flag.setValueFromString(args.get(2))) {
            // Should be okay if it's the same :/
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.perm.set.success", args.get(1), args.get(2));
        } else
            // Same here
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.perm.valueNotValid", args.get(2)));
        getDatasource().saveFlag(flag, town);

    }

    @CommandNode(
            name = "whitelist",
            permission = "mytown.adm.cmd.perm.whitelist",
            parentName = "mytown.adm.cmd.perm",
            completionKeys = {"townCompletion", "flagCompletionWhitelist"})
    public static void permWhitelistCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        if(args.size() < 2)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.usage.plot.whitelist.add"));

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));
        FlagType flagType = getFlagTypeFromName(args.get(1));

        if(flagType.isWhitelistable())
            res.startBlockSelection(flagType, town.getName(), false);
        else
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.flag.notForWhitelist"));
    }

    @CommandNode(
            name = "claim",
            permission = "mytown.adm.cmd.claim",
            parentName = "mytown.adm.cmd",
            completionKeys = {"townCompletion"})
    public static void claimCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.adm.cmd.usage.claim"));
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(player);
        Town town = getTownFromName(args.get(0));

        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.claim.already"));
        if (CommandsAssistant.checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town)) // Checks if the player can claim far
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.farClaim.unimplemented"));
            //Assert.Perm(player, "mytown.cmd.assistant.claim.far");
        Block block = getDatasource().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, town);
        if (block == null)
            throw new CommandException("Failed to create Block"); // TODO Localize
        getDatasource().saveBlock(block);
        res.sendMessage(getLocal().getLocalization("mytown.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
    }

    @CommandNode(
            name = "unclaim",
            permission = "mytown.adm.cmd.unclaim",
            parentName = "mytown.adm.cmd")
    public static void unclaimCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.adm.cmd.usage.claim"));

        EntityPlayer pl = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(pl);
        Block block = getBlockAtResident(res);
        Town town = block.getTown();

        if (!block.isPointIn(town.getSpawn().getDim(), town.getSpawn().getX(), town.getSpawn().getZ())) {
            getDatasource().deleteBlock(block);
            res.sendMessage(getLocal().getLocalization("mytown.notification.block.removed", block.getX() << 4, block.getZ() << 4, block.getX() << 4 + 15, block.getZ() << 4 + 15, town.getName()));
        } else {
            throw new CommandException("§cYou cannot delete the Block containing the spawn point!");
        }

    }


}
