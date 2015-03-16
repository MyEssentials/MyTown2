package mytown.commands;

import java.io.File;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.Utils;
import mytown.core.utils.Assert;
import mytown.core.utils.command.Command;
import mytown.core.utils.command.CommandNode;
import mytown.core.utils.config.ConfigProcessor;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.Wild;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.handlers.SafemodeHandler;
import mytown.new_protection.json.JSONParser;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
import mytown.util.exceptions.MyTownWrongUsageException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

/**
 * Created by AfterWind on 8/29/2014.
 * All commands for admins go here
 */
public class CommandsAdmin extends Commands {

    @Command(
            name = "mytownadmin",
            permission = "mytown.adm.cmd",
            alias = {"ta", "townadmin"},
            opsOnlyAccess = true)
    public static void townAdminCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.adm.cmd");
    }

    @CommandNode(
            name = "config",
            permission = "mytown.adm.cmd.config",
            parentName = "mytown.adm.cmd",
            nonPlayers = true)
    public static void configCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.adm.cmd.config");
    }

    @CommandNode(
            name = "load",
            permission = "mytown.adm.cmd.config.load",
            parentName = "mytown.adm.cmd.config",
            nonPlayers = true)
    public static void configLoadCommand(ICommandSender sender, List<String> args) {
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.config.load.start"));
        MyTown.instance.config = new Configuration(new File(Constants.CONFIG_FOLDER, "MyTown.cfg"));
        ConfigProcessor.load(MyTown.instance.config, Config.class);
        JSONParser.start();
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.config.load.stop"));
    }

    @CommandNode(
            name = "save",
            permission = "mytown.adm.cmd.config.save",
            parentName = "mytown.adm.cmd.config",
            nonPlayers = true)
    public static void configSaveCommand(ICommandSender sender, List<String> args) {
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.config.save.start"));
        ConfigProcessor.save(MyTown.instance.config, Config.class);
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.config.save.stop"));
    }

    @CommandNode(
            name = "add",
            permission = "mytown.adm.cmd.add",
            parentName = "mytown.adm.cmd",
            nonPlayers = true,
            completionKeys = {"residentCompletion", "townCompletion"})
    public static void addCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            throw new MyTownWrongUsageException("mytown.adm.cmd.usage.add");

        Resident target = getResidentFromName(args.get(0));
        Town town = getTownFromName(args.get(1));

        if (town.hasResident(target))
            throw new MyTownCommandException("mytown.adm.cmd.err.add.already", args.get(0), args.get(1));

        Rank rank;

        if (args.size() > 2) {
            rank = getRankFromTown(town, args.get(2));
        } else {
            rank = town.getDefaultRank();
        }


        getDatasource().linkResidentToTown(target, town, rank);

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.resident.add", args.get(0), args.get(1), args.size() > 2 ? args.get(2) : town.getDefaultRank().getName()));
        target.sendMessage(getLocal().getLocalization("mytown.notification.town.added", town.getName()));
    }

    @CommandNode(
            name = "delete",
            permission = "mytown.adm.cmd.delete",
            parentName = "mytown.adm.cmd",
            nonPlayers = true,
            completionKeys = {"townCompletion"})
    public static void deleteCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.adm.cmd.delete.usage");

        for (String s : args) {
            if (!getDatasource().hasTown(s))
                throw new MyTownCommandException("mytown.cmd.err.town.notexist", s);
        }
        for (String s : args) {
            if (getDatasource().deleteTown(getUniverse().getTownsMap().get(s))) {
                sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.deleted", s));
            }
        }
    }

    @CommandNode(
            name = "new",
            permission = "mytown.adm.cmd.new",
            parentName = "mytown.adm.cmd")
    public static void newCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.newtown");

        //TODO: make adminTown work properly
        Resident res = getDatasource().getOrMakeResident(sender);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.startedCreation", args.get(0)));

        EntityPlayer player = (EntityPlayer) sender;
        if (getDatasource().hasTown(args.get(0))) // Is the town name already in use?
            throw new MyTownCommandException("mytown.cmd.err.newtown.nameinuse", args.get(0));
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ)) // Is the Block already claimed?
            throw new MyTownCommandException("mytown.cmd.err.newtown.positionError");

        Town town = getDatasource().newAdminTown(args.get(0), res); // Attempt to create the Town
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.newtown.failed");

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.created", town.getName()));
    }

    @CommandNode(
            name = "rem",
            permission = "mytown.adm.cmd.rem",
            parentName = "mytown.adm.cmd",
            nonPlayers = true,
            completionKeys = {"residentCompletion", "townCompletion"})
    public static void remCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 2)
            throw new MyTownWrongUsageException("mytown.adm.cmd.usage.rem");

        Resident target = getResidentFromName(args.get(0));
        Town town = getTownFromName(args.get(1));

        if (!town.hasResident(target))
            throw new MyTownCommandException("mytown.adm.cmd.err.rem.resident", args.get(0), args.get(1));

        getDatasource().unlinkResidentFromTown(target, town);
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.resident.remove", args.get(0), args.get(1)));
    }

    @CommandNode(
            name = "setExtra",
            permission = "mytown.adm.cmd.setextra",
            parentName = "mytown.adm.cmd",
            nonPlayers = true)
    public static void setExtraCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.adm.cmd.setextra");
    }

    @CommandNode(
            name = "town",
            permission = "mytown.adm.cmd.setextra.town",
            parentName = "mytown.adm.cmd.setextra",
            completionKeys = {"townCompletion"},
            nonPlayers = true)
    public static void setExtraTownCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            throw new MyTownWrongUsageException("mytown.adm.cmd.usage.setExtra.town");
        Town town = getUniverse().getTown(args.get(0));
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.town.notexist", args.get(0));
        if(!MyTownUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));
        town.setExtraBlocks(Integer.parseInt(args.get(1)));
        getDatasource().saveTown(town);
        sendMessageBackToSender(sender, LocalizationProxy.getLocalization().getLocalization("mytown.notification.resident.setExtra", args.get(1)));
    }

    @CommandNode(
            name = "res",
            permission = "mytown.adm.cmd.setextra.res",
            parentName = "mytown.adm.cmd.setextra",
            completionKeys = {"residentCompletion"},
            nonPlayers = true)
    public static void setExtraResCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            throw new MyTownWrongUsageException("mytown.adm.cmd.usage.setExtra.res");
        Resident target = getUniverse().getResidentByName(args.get(0));
        if (target == null)
            throw new MyTownCommandException("mytown.cmd.err.resident.notexist", args.get(0));
        if(!MyTownUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));
        target.setExtraBlocks(Integer.parseInt(args.get(1)));
        getDatasource().saveResident(target);
        target.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.resident.setExtra", args.get(1)));
    }

    @CommandNode(
            name = "safemode",
            permission = "mytown.adm.cmd.safemode",
            parentName = "mytown.adm.cmd",
            nonPlayers = true)
    public static void safemodeCommand(ICommandSender sender, List<String> args) {
        boolean safemode;
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
            parentName = "mytown.adm.cmd",
            nonPlayers = true,
            players = false)
    public static void dbCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.adm.cmd.db");
    }

    @CommandNode(
            name = "purge",
            permission = "mytown.adm.cmd.db.purge",
            parentName = "mytown.adm.cmd.db",
            nonPlayers = true,
            players = false)
    public static void dbCommandPurge(ICommandSender sender, List<String> args) {
        for (Town town : getUniverse().getTownsMap().values()) {
            getDatasource().deleteTown(town);
        }
        for (Resident resident : getUniverse().getResidentsMap().values()) {
            getDatasource().deleteResident(resident);
        }

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.db.purging"));
    }

    @CommandNode(
            name = "perm",
            permission = "mytown.adm.cmd.perm",
            parentName = "mytown.adm.cmd",
            nonPlayers = true)
    public static void permCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.adm.cmd.perm");
    }

    @CommandNode(
            name = "town",
            permission = "mytown.adm.cmd.perm.town",
            parentName = "mytown.adm.cmd.perm",
            nonPlayers = true)
    public static void permTownCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.adm.cmd.perm.town");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.adm.cmd.perm.town.list",
            parentName = "mytown.adm.cmd.perm.town",
            nonPlayers = true,
            completionKeys = {"townCompletion"})
    public static void permTownListCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            throw new MyTownWrongUsageException("mytown.adm.cmd.usage.perm.list");
        }

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
        if (formattedFlagList != null)
            sendMessageBackToSender(sender, formattedFlagList);
        else
            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.err.flag.list"));
    }

    @CommandNode(
            name = "set",
            permission = "mytown.adm.cmd.perm.town.set",
            parentName = "mytown.adm.cmd.perm.town",
            nonPlayers = true,
            completionKeys = {"townCompletion", "flagCompletion"})
    public static void permTownSetCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 3) {
            throw new MyTownWrongUsageException("mytown.adm.cmd.usage.perm.town.set");
        }

        Town town = getTownFromName(args.get(0));
        Flag flag = getFlagFromName(town, args.get(1));

        if (flag.setValueFromString(args.get(2))) {
            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.perm.set.success", args.get(1), args.get(2)));
        } else
            // Same here
            throw new MyTownCommandException("mytown.cmd.err.perm.valueNotValid", args.get(2));
        getDatasource().saveFlag(flag, town);

    }

    @CommandNode(
            name = "whitelist",
            permission = "mytown.adm.cmd.perm.town.whitelist",
            parentName = "mytown.adm.cmd.perm.town",
            completionKeys = {"townCompletion", "flagCompletionWhitelist"})
    public static void permTownWhitelistCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            throw new MyTownCommandException("mytown.cmd.usage.plot.whitelist.add");

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));
        FlagType flagType = getFlagTypeFromName(args.get(1));

        if (flagType.isWhitelistable())
            res.startBlockSelection(flagType, town.getName());
        else
            throw new MyTownCommandException("mytown.cmd.err.flag.notForWhitelist");
    }

    @CommandNode(
            name = "wild",
            permission = "mytown.adm.cmd.perm.wild",
            parentName = "mytown.adm.cmd.perm",
            nonPlayers = true)
    public static void permWildCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.adm.cmd.perm.wild");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.adm.cmd.perm.wild.list",
            parentName = "mytown.adm.cmd.perm.wild",
            nonPlayers = true,
            completionKeys = {"flagCompletion"})
    public static void permWildListCommand(ICommandSender sender, List<String> args) {
        String formattedFlagList = null;
        for (Flag flag : Wild.getInstance().getFlags()) {
            if (formattedFlagList == null) {
                formattedFlagList = "";
            } else {
                formattedFlagList += '\n';
            }
            formattedFlagList += flag;
        }
        if (formattedFlagList != null)
            sendMessageBackToSender(sender, formattedFlagList);
        else
            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.err.flag.list"));
    }

    @CommandNode(
            name = "set",
            permission = "mytown.adm.cmd.perm.wild.set",
            parentName = "mytown.adm.cmd.perm.wild",
            nonPlayers = true,
            completionKeys = {"flagCompletion"})
    public static void permWildSetCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            throw new MyTownWrongUsageException("mytown.adm.cmd.usage.perm.wild.set");
        }
        FlagType type = getFlagTypeFromName(args.get(0));
        Flag flag = getFlagFromType(Wild.getInstance(), type);

        if (flag.setValueFromString(args.get(1))) {
            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.wild.perm.set.success", args.get(0), args.get(1)));
        } else
            throw new MyTownCommandException("mytown.cmd.err.perm.valueNotValid", args.get(1));
        //Saving changes to file
        MyTown.instance.wildConfig.saveChanges();
    }

    @CommandNode(
            name = "claim",
            permission = "mytown.adm.cmd.claim",
            parentName = "mytown.adm.cmd",
            completionKeys = {"townCompletion"})
    public static void claimCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.adm.cmd.usage.claim");
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(player);
        Town town = getTownFromName(args.get(0));

        if (town.getBlocks().size() >= town.getMaxBlocks())
            throw new MyTownCommandException("mytown.cmd.err.town.maxBlocks");
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ))
            throw new MyTownCommandException("mytown.cmd.err.claim.already");
        if (!CommandsAssistant.checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town)) // Checks if the player can claim far
            throw new MyTownCommandException("mytown.cmd.err.farClaim.unimplemented");
        //Assert.Perm(player, "mytown.cmd.assistant.claim.far");
        TownBlock block = getDatasource().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, town);
        if (block == null)
            throw new MyTownCommandException("Failed to create Block"); // TODO Localize
        getDatasource().saveBlock(block);
        res.sendMessage(getLocal().getLocalization("mytown.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
    }

    @CommandNode(
            name = "unclaim",
            permission = "mytown.adm.cmd.unclaim",
            parentName = "mytown.adm.cmd")
    public static void unclaimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer pl = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(pl);
        TownBlock block = getBlockAtResident(res);
        Town town = block.getTown();

        if (block.isPointIn(town.getSpawn().getDim(), town.getSpawn().getX(), town.getSpawn().getZ()))
            throw new MyTownCommandException("mytown.cmd.err.unclaim.spawnPoint");

        getDatasource().deleteBlock(block);
        res.sendMessage(getLocal().getLocalization("mytown.notification.block.removed", block.getX() << 4, block.getZ() << 4, block.getX() << 4 + 15, block.getZ() << 4 + 15, town.getName()));
    }

    @CommandNode(
            name = "help",
            permission = "mytown.adm.cmd.help",
            parentName = "mytown.adm.cmd",
            nonPlayers = true)
    public static void helpCommand(ICommandSender sender, List<String> args) {
        sendHelpMessage(sender, "mytown.adm.cmd", args);
    }

    @CommandNode(
            name = "cost",
            permission = "mytown.adm.cmd.cost",
            parentName = "mytown.adm.cmd",
            nonPlayers = true)
    public static void costCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.adm.cmd.cost");
    }

    @CommandNode(
            name = "itemname",
            permission = "mytown.adm.cmd.cost.itemname",
            parentName = "mytown.adm.cmd.cost",
            nonPlayers = false)
    public static void costItemNameCommand(ICommandSender sender, List<String> args) {
        if(sender instanceof EntityPlayer) {
            ItemStack stack = ((EntityPlayer) sender).getHeldItem();
            if(stack == null)
                return;
            String itemName = GameRegistry.findUniqueIdentifierFor(stack.getItem()).toString();
            if(stack.getItemDamage() != 0)
                itemName += ":" + stack.getItemDamage();
            sendMessageBackToSender(sender, LocalizationProxy.getLocalization().getLocalization("mytown.adm.cmd.cost.itemname", itemName));
        }
    }
}
