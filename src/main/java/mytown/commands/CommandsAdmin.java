package mytown.commands;

import myessentials.entities.ChunkPos;
import myessentials.utils.ChatUtils;
import myessentials.utils.ColorUtils;
import myessentials.utils.StringUtils;
import myessentials.utils.WorldUtils;
import mypermissions.api.command.CommandManager;
import mypermissions.api.command.CommandResponse;
import mypermissions.api.command.annotation.Command;
import mypermissions.command.CommandTree;
import mypermissions.command.CommandTreeNode;
import mytown.MyTown;
import mytown.config.json.FlagsConfig;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.entities.tools.WhitelisterTool;
import mytown.handlers.SafemodeHandler;
import mytown.handlers.VisualsHandler;
import mytown.util.exceptions.MyTownCommandException;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * All commands for admins go here
 */
public class CommandsAdmin extends Commands {

    private CommandsAdmin() {

    }

    @Command(
            name = "mytownadmin",
            permission = "mytown.adm.cmd",
            syntax = "/townadmin <command>",
            alias = {"ta", "townadmin"})
    public static CommandResponse townAdminCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "config",
            permission = "mytown.adm.cmd.config",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin config <command>",
            console = true)
    public static CommandResponse configCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "reload",
            permission = "mytown.adm.cmd.config.reload",
            parentName = "mytown.adm.cmd.config",
            syntax = "/townadmin config reload",
            console = true)
    public static CommandResponse configReloadCommand(ICommandSender sender, List<String> args) {
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.config.load.start"));
        MyTown.instance.loadConfigs();
        getDatasource().checkAllOnStart();
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.config.load.stop"));
        return CommandResponse.DONE;
    }

    @Command(
            name = "reset",
            permission = "mytown.adm.cmd.config.reset",
            parentName = "mytown.adm.cmd.config",
            syntax = "/townadmin config reset <command>",
            console = true)
    public static CommandResponse configResetCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "defaultRanks",
            permission = "mytown.adm.cmd.config.reset.defaultRanks",
            parentName = "mytown.adm.cmd.config.reset",
            syntax = "/townadmin config reset ranks",
            console = true)
    public static CommandResponse configResetRanksCommand(ICommandSender sender, List<String> args) {
        MyTown.instance.getRanksConfig().create(new ArrayList<Rank>());
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.config.reset", MyTown.instance.getRanksConfig().getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "wild",
            permission = "mytown.adm.cmd.config.reset.wildPerms",
            parentName = "mytown.adm.cmd.config.reset",
            syntax = "/townadmin config reset wild",
            console = true)
    public static CommandResponse configResetWildCommand(ICommandSender sender, List<String> args) {
        MyTown.instance.getWildConfig().create(new ArrayList<Flag>());
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.config.reset", MyTown.instance.getWildConfig().getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "defaultFlags",
            permission = "mytown.adm.cmd.config.reset.defaultFlags",
            parentName = "mytown.adm.cmd.config.reset",
            syntax = "/townadmin config reset defaultFlags",
            console = true)
    public static CommandResponse configResetFlagsCommand(ICommandSender sender, List<String> args) {
        MyTown.instance.getFlagsConfig().create(new ArrayList<FlagsConfig.Wrapper>());
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.cmd.config.reset", MyTown.instance.getFlagsConfig().getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "update",
            permission = "mytown.adm.cmd.update",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin update <command>",
            console = true)
    public static CommandResponse updateCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "ranks",
            permission = "mytown.adm.cmd.update.ranks",
            parentName = "mytown.adm.cmd.update",
            syntax = "/townadmin update ranks",
            console = true)
    public static CommandResponse updateRanksCommand(ICommandSender sender, List<String> args) {
        MyTown.instance.getRanksConfig().create(new ArrayList<Rank>());
        for(Town town : getUniverse().towns) {
            getDatasource().resetRanks(town);
        }
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.update.ranks"));
        return CommandResponse.DONE;
    }

    @Command(
            name = "add",
            permission = "mytown.adm.cmd.add",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin add <resident> <town> [rank]",
            console = true,
            completionKeys = {"residentCompletion", "townCompletion"})
    public static CommandResponse addCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            return CommandResponse.SEND_SYNTAX;

        Resident target = getResidentFromName(args.get(0));
        Town town = getTownFromName(args.get(1));

        if (town.residentsMap.containsKey(target))
            throw new MyTownCommandException("mytown.adm.cmd.err.add.already", args.get(0), args.get(1));

        Rank rank;

        if (args.size() > 2) {
            rank = getRankFromTown(town, args.get(2));
        } else {
            rank = town.ranksContainer.getDefaultRank();
        }


        getDatasource().linkResidentToTown(target, town, rank);

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.resident.add", args.get(0), args.get(1), args.size() > 2 ? args.get(2) : town.ranksContainer.getDefaultRank().getName()));
        target.sendMessage(getLocal().getLocalization("mytown.notification.town.added", town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "delete",
            permission = "mytown.adm.cmd.delete",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin delete <town>",
            console = true,
            completionKeys = {"townCompletion"})
    public static CommandResponse deleteCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        for (String s : args) {
            if (!getUniverse().towns.contains(s))
                throw new MyTownCommandException("mytown.cmd.err.town.notexist", s);
        }
        for (String s : args) {
            if (getDatasource().deleteTown(getUniverse().towns.get(s))) {
                sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.deleted", s));
            }
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "new",
            permission = "mytown.adm.cmd.new",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin new <name>")
    public static CommandResponse newCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.startedCreation", args.get(0)));

        EntityPlayer player = (EntityPlayer) sender;
        if (getUniverse().towns.contains(args.get(0))) // Is the town name already in use?
            throw new MyTownCommandException("mytown.cmd.err.newtown.nameinuse", args.get(0));
        if (getUniverse().blocks.contains(player.dimension, player.chunkCoordX, player.chunkCoordZ)) // Is the Block already claimed?
            throw new MyTownCommandException("mytown.cmd.err.newtown.positionError");

        Town town = getUniverse().newAdminTown(args.get(0), res); // Attempt to create the Town
        if (town == null)
            throw new MyTownCommandException("mytown.cmd.err.newtown.failed");

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.created", town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "kick",
            permission = "mytown.adm.cmd.kick",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin kick <resident> <town>",
            console = true,
            completionKeys = {"residentCompletion", "townCompletion"})
    public static CommandResponse remCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            return CommandResponse.SEND_SYNTAX;

        Resident target = getResidentFromName(args.get(0));
        Town town = getTownFromName(args.get(1));

        if (!town.residentsMap.containsKey(target)) {
            throw new MyTownCommandException("mytown.adm.cmd.err.kick.resident", args.get(0), args.get(1));
        }

        getDatasource().unlinkResidentFromTown(target, town);
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.resident.remove", args.get(0), args.get(1)));
        return CommandResponse.DONE;
    }

    @Command(
            name = "res",
            permission = "mytown.adm.cmd.res",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin res <command>",
            console = true)
    public static CommandResponse resCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "blocks",
            permission = "mytown.adm.cmd.blocks",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin blocks <command>",
            console = true)
    public static CommandResponse townBlocksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "info",
            permission = "mytown.adm.cmd.blocks.info",
            parentName = "mytown.adm.cmd.blocks",
            syntax = "/townadmin blocks info <town>",
            completionKeys = {"townCompletion"},
            console = true)
    public static CommandResponse blocksInfoCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Town town = getTownFromName(args.get(0));

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

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.blocks.info", blocks, extraBlocks, farBlocks));

        return CommandResponse.DONE;
    }

    @Command(
            name = "extra",
            permission = "mytown.adm.cmd.blocks.extra",
            parentName = "mytown.adm.cmd.blocks",
            syntax = "/townadmin blocks extra <command>",
            console = true)
    public static CommandResponse townBlocksMaxCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "set",
            permission = "mytown.adm.cmd.blocks.extra.set",
            parentName = "mytown.adm.cmd.blocks.extra",
            syntax = "/townadmin blocks extra set <town> <amount>",
            completionKeys = {"townCompletion"},
            console = true)
    public static CommandResponse townBlocksMaxSetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        if(!StringUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

        Town town = getTownFromName(args.get(0));
        town.townBlocksContainer.setExtraBlocks(Integer.parseInt(args.get(1)));
        getDatasource().saveTown(town);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.town.blocks.extra.set", town.townBlocksContainer.getExtraBlocks(), args.get(0)));
        return CommandResponse.DONE;
    }

    @Command(
            name = "add",
            permission = "mytown.adm.cmd.blocks.extra.add",
            parentName = "mytown.adm.cmd.blocks.extra",
            syntax = "/townadmin blocks extra add <town> <amount>",
            completionKeys = {"townCompletion"},
            console = true)
    public static CommandResponse townBlocksMaxAddCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        if(!StringUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

        Town town = getTownFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        town.townBlocksContainer.setExtraBlocks(town.townBlocksContainer.getExtraBlocks() + amount);
        getDatasource().saveTown(town);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.town.blocks.extra.set", town.townBlocksContainer.getExtraBlocks(), args.get(0)));
        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "mytown.adm.cmd.blocks.extra.remove",
            parentName = "mytown.adm.cmd.blocks.extra",
            syntax = "/townadmin blocks extra remove <town> <amount>",
            completionKeys = {"townCompletion"},
            console = true)
    public static CommandResponse townBlocksMaxRemoveCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        if(!StringUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

        Town town = getTownFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        town.townBlocksContainer.setExtraBlocks(town.townBlocksContainer.getExtraBlocks() - amount);
        getDatasource().saveTown(town);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.town.blocks.extra.set", town.townBlocksContainer.getExtraBlocks(), args.get(0)));
        return CommandResponse.DONE;
    }

    @Command(
            name = "far",
            permission = "mytown.adm.cmd.blocks.far",
            parentName = "mytown.adm.cmd.blocks",
            syntax = "/townadmin blocks far <command>")
    public static CommandResponse townBlocksFarClaimsCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "set",
            permission = "mytown.adm.cmd.blocks.far.set",
            parentName = "mytown.adm.cmd.blocks.far",
            syntax = "/townadmin blocks far set <town> <amount>",
            completionKeys = {"townCompletionAndAll"},
            console = true)
    public static CommandResponse townBlocksFarclaimsSetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        if(!StringUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

        Town town = getTownFromName(args.get(0));
        town.townBlocksContainer.setMaxFarClaims(Integer.parseInt(args.get(1)));
        getDatasource().saveTown(town);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.town.blocks.farClaims.set", town.townBlocksContainer.getMaxFarClaims(), args.get(0)));
        return CommandResponse.DONE;
    }

    @Command(
            name = "add",
            permission = "mytown.adm.cmd.blocks.far.add",
            parentName = "mytown.adm.cmd.blocks.far",
            syntax = "/townadmin blocks far add <town> <amount>",
            completionKeys = {"townCompletionAndAll"},
            console = true)
    public static CommandResponse townBlocksFarclaimsAddCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        if(!StringUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

        Town town = getTownFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        town.townBlocksContainer.setMaxFarClaims(town.townBlocksContainer.getMaxFarClaims() + amount);
        getDatasource().saveTown(town);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.town.blocks.farClaims.set", town.townBlocksContainer.getMaxFarClaims(), args.get(0)));
        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "mytown.adm.cmd.blocks.far.remove",
            parentName = "mytown.adm.cmd.blocks.far",
            syntax = "/townadmin blocks far remove <town> <amount>",
            completionKeys = {"townCompletionAndAll"},
            console = true)
    public static CommandResponse townBlocksFarClaimsRemoveCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        if(!StringUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

        Town town = getTownFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        town.townBlocksContainer.setMaxFarClaims(town.townBlocksContainer.getMaxFarClaims() - amount);
        getDatasource().saveTown(town);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.town.blocks.farClaims.set", town.townBlocksContainer.getMaxFarClaims(), args.get(0)));
        return CommandResponse.DONE;
    }


    @Command(
            name = "blocks",
            permission = "mytown.adm.cmd.res.blocks",
            parentName = "mytown.adm.cmd.res",
            syntax = "/townadmin res blocks <command>",
            console = true)
    public static CommandResponse resBlocksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "extra",
            permission = "mytown.adm.cmd.res.blocks.extra",
            parentName = "mytown.adm.cmd.res.blocks",
            syntax = "/townadmin res blocks extra <command>",
            console = true)
    public static CommandResponse resBlocksMaxCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "set",
            permission = "mytown.adm.cmd.res.blocks.extra.set",
            parentName = "mytown.adm.cmd.res.blocks.extra",
            syntax = "/townadmin res blocks extra set <resident> <extraBlocks>",
            completionKeys = {"residentCompletion"},
            console = true)
    public static CommandResponse resBlocksSetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        if(!StringUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

        Resident target = getResidentFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        target.setExtraBlocks(amount);
        getDatasource().saveResident(target);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.res.blocks.extra.set", target.getExtraBlocks(), args.get(0)));
        return CommandResponse.DONE;
    }

    @Command(
            name = "add",
            permission = "mytown.adm.cmd.res.blocks.extra.add",
            parentName = "mytown.adm.cmd.res.blocks.extra",
            syntax = "/townadmin res blocks extra add <resident> <extraBlocks>",
            completionKeys = {"residentCompletion"},
            console = true)
    public static CommandResponse resBlocksAddCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        if(!StringUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

        Resident target = getResidentFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        target.setExtraBlocks(target.getExtraBlocks() + amount);
        getDatasource().saveResident(target);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.res.blocks.extra.set", target.getExtraBlocks(), args.get(0)));
        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "mytown.adm.cmd.res.blocks.extra.remove",
            parentName = "mytown.adm.cmd.res.blocks.extra",
            syntax = "/townadmin res blocks extra remove <resident> <extraBlocks>",
            completionKeys = {"residentCompletion"},
            console = true)
    public static CommandResponse resBlocksRemoveCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        if(!StringUtils.tryParseInt(args.get(1)) || Integer.parseInt(args.get(1)) < 0)
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

        Resident target = getResidentFromName(args.get(0));
        int amount = Integer.parseInt(args.get(1));
        target.setExtraBlocks(target.getExtraBlocks() - amount);
        getDatasource().saveResident(target);
        sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.res.blocks.extra.set", target.getExtraBlocks(), args.get(0)));
        return CommandResponse.DONE;
    }

    @Command(
            name = "ranks",
            permission = "mytown.adm.cmd.ranks",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin ranks <command>",
            console = true)
    public static CommandResponse ranksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "add",
            permission = "mytown.adm.cmd.ranks.add",
            parentName = "mytown.adm.cmd.ranks",
            syntax = "/townadmin ranks add <town> <name> [templateRank]",
            completionKeys = {"townCompletion", "-", "ranksCompletion"},
            console = true)
    public static CommandResponse ranksAddCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            return CommandResponse.SEND_SYNTAX;

        Town town = getTownFromName(args.get(0));

        if (town.ranksContainer.contains(args.get(1))) {
            throw new MyTownCommandException("mytown.cmd.err.ranks.add.already", args.get(1));
        }

        Rank rank = new Rank(args.get(1), town, Rank.Type.REGULAR);
        if(args.size() > 2) {
            Rank template = getRankFromTown(town, args.get(2));
            rank.permissionsContainer.addAll(template.permissionsContainer);
        }

        getDatasource().saveRank(rank);

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.ranks.add", args.get(1), town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "mytown.adm.cmd.ranks.remove",
            parentName = "mytown.adm.cmd.ranks",
            syntax = "/townadmin ranks remove <town> <rank>",
            completionKeys = {"townCompletion", "rankCompletion"},
            console = true)
    public static CommandResponse ranksRemoveCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Town town = getTownFromName(args.get(0));
        Rank rank = getRankFromTown(town, args.get(1));

        if (rank.getType().unique) {
            throw new MyTownCommandException("mytown.cmd.err.ranks.cantDelete");
        }

        for(Rank residentRank : town.residentsMap.values()) {
            if(residentRank == rank) {
                throw new MyTownCommandException("mytown.cmd.err.ranks.assigned");
            }
        }

        getDatasource().deleteRank(rank);
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.ranks.rem", args.get(1), town.getName()));

        return CommandResponse.DONE;
    }

    @Command(
            name = "set",
            permission = "mytown.adm.cmd.ranks.set",
            parentName = "mytown.adm.cmd.ranks",
            syntax = "/townadmin ranks set <town> <rank> <type>",
            completionKeys = {"townCompletion", "rankCompletion"},
            console = true)
    public static CommandResponse ranksSetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 3) {
            return CommandResponse.SEND_SYNTAX;
        }

        Town town = getTownFromName(args.get(0));
        Rank rank = getRankFromTown(town, args.get(1));
        Rank.Type type = getRankTypeFromString(args.get(2));

        if(type.unique) {
            Rank fromRank = town.ranksContainer.get(type);
            if(fromRank == rank) {
                throw new MyTownCommandException("mytown.cmd.err.ranks.set.already", type.toString());
            }
            fromRank.setType(Rank.Type.REGULAR);
            rank.setType(type);

            getDatasource().saveRank(rank);
            getDatasource().saveRank(fromRank);
        } else {
            rank.setType(type);

            getDatasource().saveRank(rank);
        }

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.ranks.set.successful", rank.getName(), type.toString()));
        return CommandResponse.DONE;
    }


    @Command(
            name = "add",
            permission = "mytown.adm.cmd.ranks.perm.add",
            parentName = "mytown.adm.cmd.ranks.perm",
            syntax = "/townadmin ranks perm add <town> <rank> <perm>",
            completionKeys = {"townCompletion", "rankCompletion"},
            console = true)
    public static CommandResponse ranksPermAddCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 3) {
            return CommandResponse.SEND_SYNTAX;
        }

        Town town = getTownFromName(args.get(0));
        Rank rank = getRankFromTown(town, args.get(1));

        getDatasource().saveRankPermission(rank, args.get(2));
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.ranks.perm.add", args.get(1), args.get(0)));

        return CommandResponse.DONE;
    }

    @Command(
            name = "remove",
            permission = "mytown.adm.cmd.ranks.perm.remove",
            parentName = "mytown.adm.cmd.ranks.perm",
            syntax = "/townadmin ranks perm remove <town> <rank> <perm>",
            completionKeys = {"townCompletion", "rankCompletion"},
            console = true)
    public static CommandResponse ranksPermRemoveCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 3) {
            return CommandResponse.SEND_SYNTAX;
        }

        Town town = getTownFromName(args.get(0));
        Rank rank = getRankFromTown(town, args.get(1));

        getDatasource().deleteRankPermission(rank, args.get(2));
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.ranks.perm.remove", args.get(2), args.get(1)));

        return CommandResponse.DONE;
    }

    @Command(
            name = "reset",
            permission = "mytown.adm.cmd.ranks.reset",
            parentName = "mytown.adm.cmd.ranks",
            syntax = "/townadmin ranks reset <town>",
            completionKeys = {"townCompletion"},
            console = true)
    public static CommandResponse ranksResetCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Town town = getTownFromName(args.get(0));
        getDatasource().resetRanks(town);
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.ranks.reset"));

        return CommandResponse.DONE;
    }

    @Command(
            name = "perm",
            permission = "mytown.adm.cmd.ranks.perm",
            parentName = "mytown.adm.cmd.ranks",
            syntax = "/townadmin ranks perm <command>")
    public static CommandResponse ranksPermCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.adm.cmd.ranks.perm.list",
            parentName = "mytown.adm.cmd.ranks.perm",
            syntax = "/townadmin ranks perm list <town> <rank>",
            completionKeys = {"townCompletion", "rankCompletion"})
    public static CommandResponse ranksPermListCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }

        Town town = getTownFromName(args.get(0));
        Rank rank = getRankFromTown(town, args.get(1));

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.ranks.perm.list", rank.getName(), town.getName(), rank.permissionsContainer.toString()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "safemode",
            permission = "mytown.adm.cmd.safemode",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin safemode <on|off>",
            console = true)
    public static CommandResponse safemodeCommand(ICommandSender sender, List<String> args) {
        boolean safemode;
        if (args.size() < 1) { // Toggle safemode
            safemode = !SafemodeHandler.isInSafemode();
        } else { // Set safemode
            safemode = ChatUtils.equalsOn(args.get(0));
        }

        SafemodeHandler.setSafemode(safemode);
        SafemodeHandler.kickPlayers();
        return CommandResponse.DONE;
    }

    @Command(
            name = "db",
            permission = "mytown.adm.cmd.db",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin db <command>",
            console = true)
    public static CommandResponse dbCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "purge",
            permission = "mytown.adm.cmd.db.purge",
            parentName = "mytown.adm.cmd.db",
            syntax = "/townadmin db purge",
            console = true)
    public static CommandResponse dbCommandPurge(ICommandSender sender, List<String> args) {
        for (Town town : getUniverse().towns) {
            getDatasource().deleteTown(town);
        }
        for (Resident resident : getUniverse().residents) {
            getDatasource().deleteResident(resident);
        }

        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.db.purging"));
        return CommandResponse.DONE;
    }

    @Command(
            name = "perm",
            permission = "mytown.adm.cmd.perm",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin perm <command>",
            console = true)
    public static CommandResponse permCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "town",
            permission = "mytown.adm.cmd.perm.town",
            parentName = "mytown.adm.cmd.perm",
            syntax = "/townadmin perm town <command>",
            console = true)
    public static CommandResponse permTownCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.adm.cmd.perm.town.list",
            parentName = "mytown.adm.cmd.perm.town",
            syntax = "/townadmin perm town list <town>",
            completionKeys = {"townCompletion"},
            console = true)
    public static CommandResponse permTownListCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;


        Town town = getTownFromName(args.get(0));
        sendMessageBackToSender(sender, town.flagsContainer.toStringForTowns());
        return CommandResponse.DONE;
    }

    @Command(
            name = "set",
            permission = "mytown.adm.cmd.perm.town.set",
            parentName = "mytown.adm.cmd.perm.town",
            syntax = "/townadmin perm town set <town> <flag> <value>",
            completionKeys = {"townCompletion", "flagCompletion"},
            console = true)
    public static CommandResponse permTownSetCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 3) {
            return CommandResponse.SEND_SYNTAX;
        }

        Town town = getTownFromName(args.get(0));
        Flag flag = getFlagFromName(town.flagsContainer, args.get(1));

        if (flag.setValueFromString(args.get(2))) {
            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.perm.set.success", args.get(1), args.get(2)));
        } else
            // Same here
            throw new MyTownCommandException("mytown.cmd.err.perm.valueNotValid", args.get(2));
        getDatasource().saveFlag(flag, town);
        return CommandResponse.DONE;
    }

    @Command(
            name = "whitelist",
            permission = "mytown.adm.cmd.perm.town.whitelist",
            parentName = "mytown.adm.cmd.perm.town",
            syntax = "/townadmin perm town whitelist <town>",
            completionKeys = {"townCompletion"})
    public static CommandResponse permTownWhitelistCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        res.toolContainer.set(new WhitelisterTool(res));
        return CommandResponse.DONE;
    }

    @Command(
            name = "wild",
            permission = "mytown.adm.cmd.perm.wild",
            parentName = "mytown.adm.cmd.perm",
            syntax = "/townadmin perm wild <command>",
            console = true)
    public static CommandResponse permWildCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.adm.cmd.perm.wild.list",
            parentName = "mytown.adm.cmd.perm.wild",
            syntax = "/townadmin perm wild list",
            completionKeys = {"flagCompletion"},
            console = true)
    public static CommandResponse permWildListCommand(ICommandSender sender, List<String> args) {
        sendMessageBackToSender(sender, Wild.instance.flagsContainer.toStringForWild());
        return CommandResponse.DONE;
    }

    @Command(
            name = "set",
            permission = "mytown.adm.cmd.perm.wild.set",
            parentName = "mytown.adm.cmd.perm.wild",
            syntax = "/townadmin perm wild set <flag> <value>",
            completionKeys = {"flagCompletion"},
            console = true)
    public static CommandResponse permWildSetCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2) {
            return CommandResponse.SEND_SYNTAX;
        }
        FlagType type = getFlagTypeFromName(args.get(0));
        Flag flag = getFlagFromType(Wild.instance.flagsContainer, type);

        if (flag.setValueFromString(args.get(1))) {
            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.wild.perm.set.success", args.get(0), args.get(1)));
        } else
            throw new MyTownCommandException("mytown.cmd.err.perm.valueNotValid", args.get(1));
        //Saving changes to file
        MyTown.instance.getWildConfig().write(Wild.instance.flagsContainer);
        return CommandResponse.DONE;
    }

    @Command(
            name = "claim",
            permission = "mytown.adm.cmd.claim",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin claim <town> [range]",
            completionKeys = {"townCompletion"})
    public static CommandResponse claimCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = MyTownUniverse.instance.getOrMakeResident(player);
        Town town = getTownFromName(args.get(0));

        boolean isFarClaim = false;

        if(args.size() < 2) {

            if (town.townBlocksContainer.size() >= town.getMaxBlocks())
                throw new MyTownCommandException("mytown.cmd.err.town.maxBlocks", 1);
            if (getUniverse().blocks.contains(player.dimension, player.chunkCoordX, player.chunkCoordZ))
                throw new MyTownCommandException("mytown.cmd.err.claim.already");
            if (!CommandsAssistant.checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town)) { // Checks if the player can claim far
                res.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.adm.cmd.far.claim"));
                isFarClaim = true;
            }
            TownBlock block = getUniverse().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, isFarClaim, 0, town);
            if (block == null)
                throw new MyTownCommandException(getLocal().getLocalization("mytown.cmd.err.claim.failed"));
            getDatasource().saveBlock(block);
            res.sendMessage(getLocal().getLocalization("mytown.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
        } else {
            if(!StringUtils.tryParseInt(args.get(1)))
                throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(1));

            int radius = Integer.parseInt(args.get(1));
            List<ChunkPos> chunks = WorldUtils.getChunksInBox(player.dimension, (int) (player.posX - radius * 16), (int) (player.posZ - radius * 16), (int) (player.posX + radius * 16), (int) (player.posZ + radius * 16));
            isFarClaim = true;
            for(Iterator<ChunkPos> it = chunks.iterator(); it.hasNext();) {
                ChunkPos chunk = it.next();
                if(CommandsAssistant.checkNearby(player.dimension, chunk.getX(), chunk.getZ(), town)) {
                    isFarClaim = false;
                }
                if (getUniverse().blocks.contains(player.dimension, chunk.getX(), chunk.getZ()))
                    it.remove();
            }
            if(isFarClaim)
                res.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.adm.cmd.far.claim"));

            if (town.townBlocksContainer.size() + chunks.size() > town.getMaxBlocks())
                throw new MyTownCommandException("mytown.cmd.err.town.maxBlocks", chunks.size());

            for(ChunkPos chunk : chunks) {
                TownBlock block = getUniverse().newBlock(player.dimension, chunk.getX(), chunk.getZ(), isFarClaim, 0, town);
                // Just so that only one of the blocks will be marked as far claim.
                isFarClaim = false;
                getDatasource().saveBlock(block);
                res.sendMessage(getLocal().getLocalization("mytown.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
            }
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "unclaim",
            permission = "mytown.adm.cmd.unclaim",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin unclaim")
    public static CommandResponse unclaimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer pl = (EntityPlayer) sender;
        Resident res = MyTownUniverse.instance.getOrMakeResident(pl);
        TownBlock block = getBlockAtResident(res);
        Town town = block.getTown();

        if (block.isPointIn(town.getSpawn().getDim(), town.getSpawn().getX(), town.getSpawn().getZ()))
            throw new MyTownCommandException("mytown.cmd.err.unclaim.spawnPoint");

        getDatasource().deleteBlock(block);
        res.sendMessage(getLocal().getLocalization("mytown.notification.block.removed", block.getX() << 4, block.getZ() << 4, (block.getX() << 4) + 15, (block.getZ() << 4) + 15, town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "help",
            permission = "mytown.adm.cmd.help",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin help <command>",
            alias = {"?", "h"},
            console = true)
    public static CommandResponse helpCommand(ICommandSender sender, List<String> args) {
        int page = 1;
        if(!args.isEmpty() && StringUtils.tryParseInt(args.get(0))) {
            page = Integer.parseInt(args.get(0));
            args = args.subList(1, args.size());
        }

        CommandTree tree = CommandManager.getTree("mytown.adm.cmd");
        CommandTreeNode node = tree.getNodeFromArgs(args);
        node.sendHelpMessage(sender, page);
        return CommandResponse.DONE;
    }

    @Command(
            name = "syntax",
            permission = "mytown.adm.cmd.syntax",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin syntax <command>",
            console = true)
    public static CommandResponse syntaxCommand(ICommandSender sender, List<String> args) {
        CommandTree tree = CommandManager.getTree("mytown.adm.cmd");
        CommandTreeNode node = tree.getNodeFromArgs(args);
        node.sendSyntax(sender);
        return CommandResponse.DONE;
    }

    @Command(
            name = "debug",
            permission = "mytown.adm.cmd.debug",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin debug <command>",
            console = false)
    public static CommandResponse debugCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "itemClass",
            permission = "mytown.adm.cmd.debug.item",
            parentName = "mytown.adm.cmd.debug",
            syntax = "/townadmin debug itemClass",
            console = false)
    public static CommandResponse debugItemCommand(ICommandSender sender, List<String> args) {
        if(sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)sender;
            List<Class> list = new ArrayList<Class>();
            if(player.inventory.getCurrentItem() != null) {

                if(player.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
                    Block block = ((ItemBlock)player.inventory.getCurrentItem().getItem()).field_150939_a;
                    list.add(block.getClass());
                    if(block instanceof ITileEntityProvider) {
                    	TileEntity te = ((ITileEntityProvider) block).createNewTileEntity(MinecraftServer.getServer().worldServerForDimension(0), 0);
                        list.add(te == null ? TileEntity.class : te.getClass());
                    }
                } else {
                    list.add(player.inventory.getCurrentItem().getItem().getClass());
                }

                sendMessageBackToSender(sender, "For item: " + player.inventory.getCurrentItem().getDisplayName());
                for(Class cls : list) {
                    while (cls != Object.class) {
                        sendMessageBackToSender(sender, cls.getName());
                        cls = cls.getSuperclass();
                    }
                }
            }
        }
        return CommandResponse.DONE;
    }

    public static class Plots {
        @Command(
                name = "plot",
                permission = "mytown.adm.cmd.plot",
                parentName = "mytown.adm.cmd",
                syntax = "/townadmin plot <command>",
                console = true)
        public static CommandResponse plotCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "show",
                permission = "mytown.adm.cmd.plot.show",
                parentName = "mytown.adm.cmd.plot",
                syntax = "/townadmin plot show <town>",
                completionKeys = {"townCompletion"})
        public static CommandResponse plotShowCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                return CommandResponse.SEND_SYNTAX;

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromName(args.get(0));
            town.plotsContainer.show(res);
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.showing");
            return CommandResponse.DONE;
        }

        @Command(
                name = "perm",
                permission = "mytown.adm.cmd.plot.perm",
                parentName = "mytown.adm.cmd.plot",
                syntax = "/townadmin plot perm <command>",
                console = true)
        public static CommandResponse plotPermCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "set",
                permission = "mytown.adm.cmd.plot.perm.set",
                parentName = "mytown.adm.cmd.plot.perm",
                syntax = "/townadmin plot perm set <town> <plot> <flag> <value>",
                completionKeys = {"flagCompletion"},
                console = true)
        public static CommandResponse plotPermSetCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 4)
                return CommandResponse.SEND_SYNTAX;

            Town town = getTownFromName(args.get(0));
            Plot plot = getPlotFromName(town, args.get(1));
            Flag flag = getFlagFromName(plot.flagsContainer, args.get(2));

            if (flag.setValueFromString(args.get(3))) {
                ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.perm.set.success", args.get(0), args.get(1));
            } else
                throw new MyTownCommandException("mytown.cmd.err.perm.valueNotValid", args.get(1));

            getDatasource().saveFlag(flag, plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "list",
                permission = "mytown.adm.cmd.plot.perm.list",
                parentName = "mytown.adm.cmd.plot.perm",
                syntax = "/townadmin plot perm list <town> <plot>",
                completionKeys = {"townCompletion"},
                console = true)
        public static CommandResponse plotPermListCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 2)
                return CommandResponse.SEND_SYNTAX;

            Town town = getTownFromName(args.get(0));
            Plot plot = getPlotFromName(town, args.get(1));
            sendMessageBackToSender(sender, plot.flagsContainer.toStringForPlot(town));
            return CommandResponse.DONE;
        }

        @Command(
                name = "rename",
                permission = "mytown.adm.cmd.plot.rename",
                parentName = "mytown.adm.cmd.plot",
                syntax = "/townadmin plot rename <town> <plot> <name>",
                completionKeys = {"townCompletion", "plotCompletion"},
                console = true)
        public static CommandResponse plotRenameCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 3)
                return CommandResponse.SEND_SYNTAX;

            Town town = getTownFromName(args.get(0));
            Plot plot = getPlotFromName(town, args.get(1));

            plot.setName(args.get(2));
            getDatasource().savePlot(plot);

            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.plot.renamed"));
            return CommandResponse.DONE;
        }

        @Command(
                name = "add",
                permission = "mytown.adm.cmd.plot.add",
                parentName = "mytown.adm.cmd.plot",
                syntax = "/townadmin plot add <command>",
                console = true)
        public static CommandResponse plotAddCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "owner",
                permission = "mytown.adm.cmd.plot.add.owner",
                parentName = "mytown.adm.cmd.plot.add",
                syntax = "/townadmin plot add owner <town> <plot> <resident>",
                completionKeys = {"townCompletion", "plotCompletion", "residentCompletion"},
                console = true)
        public static CommandResponse plotAddOwnerCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 3)
                return CommandResponse.SEND_SYNTAX;

            Resident target = getResidentFromName(args.get(2));

            Town town = getTownFromName(args.get(0));
            if (!target.townsContainer.contains(town))
                throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", target.getPlayerName(), town.getName());

            Plot plot = getPlotFromName(town, args.get(1));

            if(plot.membersContainer.contains(target) || plot.ownersContainer.contains(target))
                throw new MyTownCommandException("mytown.cmd.err.plot.add.alreadyInPlot");

            if (!town.plotsContainer.canResidentMakePlot(target))
                throw new MyTownCommandException("mytown.cmd.err.plot.limit.toPlayer", target.getPlayerName());

            getDatasource().linkResidentToPlot(target, plot, true);

            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.plot.owner.sender.added", target.getPlayerName(), plot.getName()));
            target.sendMessage(getLocal().getLocalization("mytown.notification.plot.owner.target.added", plot.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "member",
                permission = "mytown.adm.cmd.plot.add.member",
                parentName = "mytown.adm.cmd.plot.add",
                syntax = "/townadmin plot add member <town> <plot> <resident>",
                completionKeys = {"townCompletion", "plotCompletion", "residentCompletion"},
                console = true)
        public static CommandResponse plotAddMemberCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 3)
                return CommandResponse.SEND_SYNTAX;

            Resident target = getResidentFromName(args.get(2));
            Town town = getTownFromName(args.get(0));
            Plot plot = getPlotFromName(town, args.get(1));

            if(plot.membersContainer.contains(target) || plot.ownersContainer.contains(target))
                throw new MyTownCommandException("mytown.cmd.err.plot.add.alreadyInPlot");

            getDatasource().linkResidentToPlot(target, plot, false);

            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.plot.member.sender.added", target.getPlayerName(), plot.getName()));
            target.sendMessage(getLocal().getLocalization("mytown.notification.plot.member.target.added", plot.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "mytown.adm.cmd.plot.remove",
                parentName = "mytown.adm.cmd.plot",
                syntax = "/townadmin plot remove <town> <plot> <resident>",
                completionKeys = {"townCompletion", "plotCompletion", "residentCompletion"},
                console = true)
        public static CommandResponse plotRemoveCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 3)
                return CommandResponse.SEND_SYNTAX;

            Resident target = getResidentFromName(args.get(2));
            Town town = getTownFromName(args.get(0));
            Plot plot = getPlotFromName(town, args.get(1));

            if(!plot.membersContainer.contains(target) && !plot.ownersContainer.contains(target))
                throw new MyTownCommandException("mytown.cmd.err.plot.remove.notInPlot");

            getDatasource().unlinkResidentFromPlot(target, plot);

            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.plot.sender.removed", target.getPlayerName(), plot.getName()));
            target.sendMessage(getLocal().getLocalization("mytown.notification.plot.target.removed", plot.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "info",
                permission = "mytown.adm.cmd.plot.info",
                parentName = "mytown.adm.cmd.plot",
                syntax = "/townadmin plot info <town> <plot>",
                completionKeys = {"townCompletion", "plotCompletion"},
                console = true)
        public static CommandResponse plotInfoCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2)
                return CommandResponse.SEND_SYNTAX;

            Town town = getTownFromName(args.get(0));
            Plot plot = getPlotFromName(town, args.get(1));
            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.plot.info", plot.getName(), plot.ownersContainer.toString(), plot.membersContainer.toString(), plot.getStartX(), plot.getStartY(), plot.getStartZ(), plot.getEndX(), plot.getEndY(), plot.getEndZ()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "delete",
                permission = "mytown.adm.cmd.plot.delete",
                parentName = "mytown.adm.cmd.plot",
                syntax = "/townadmin plot delete <town> <plot>",
                completionKeys = {"townCompletion", "plotCompletion"},
                console = true)
        public static CommandResponse plotDeleteCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2)
                return CommandResponse.SEND_SYNTAX;

            Town town = getTownFromName(args.get(0));
            Plot plot = getPlotFromName(town, args.get(1));
            getDatasource().deletePlot(plot);
            sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.plot.deleted", plot.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "hide",
                permission = "mytown.adm.cmd.plot.hide",
                parentName = "mytown.adm.cmd.plot",
                syntax = "/townadmin plot hide")
        public static CommandResponse plotHideCommand(ICommandSender sender, List<String> args) {
            if(sender instanceof EntityPlayerMP) {
                VisualsHandler.instance.unmarkPlots((EntityPlayerMP) sender);
                sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.plot.vanished"));
            }
            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "borders",
            permission = "mytown.adm.cmd.borders",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin borders <command>")
    public static CommandResponse bordersCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "show",
            permission = "mytown.adm.cmd.borders.show",
            parentName = "mytown.adm.cmd.borders",
            syntax = "/townadmin borders show <town>",
            completionKeys = {"townCompletion"})
    public static CommandResponse bordersShowCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            return CommandResponse.SEND_SYNTAX;
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));
        town.townBlocksContainer.show(res);
        res.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.borders.show", town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "hide",
            permission = "mytown.adm.cmd.borders.hide",
            parentName = "mytown.adm.cmd.borders",
            syntax = "/townadmin borders hide")
    public static CommandResponse bordersHideCommand(ICommandSender sender, List<String> args) {
        if(sender instanceof EntityPlayerMP) {
            VisualsHandler.instance.unmarkTowns((EntityPlayerMP)sender);
            sendMessageBackToSender(sender, MyTown.instance.LOCAL.getLocalization("mytown.notification.town.borders.hide"));
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "rename",
            permission = "mytown.adm.cmd.rename",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin rename <town> <name>",
            completionKeys = {"townCompletion"},
            console = true)
    public static CommandResponse renameCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 2)
            return CommandResponse.SEND_SYNTAX;

        Town town = getTownFromName(args.get(0));

        if (getUniverse().towns.contains(args.get(1)))
            throw new MyTownCommandException("mytown.cmd.err.newtown.nameinuse", args.get(1));

        town.rename(args.get(1));
        getDatasource().saveTown(town);
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.renamed"));
        return CommandResponse.DONE;
    }

    @Command(
            name = "spawn",
            permission = "mytown.adm.cmd.spawn",
            parentName = "mytown.adm.cmd",
            syntax = "/townadmin spawn <town>",
            completionKeys = {"townCompletion"},
            console = true)
    public static CommandResponse spawnCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        Resident res = getUniverse().getOrMakeResident(sender);
        Town town = getTownFromName(args.get(0));
        town.sendToSpawn(res);
        return CommandResponse.DONE;
    }
}
