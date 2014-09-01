package mytown.commands;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.Assert;
import mytown.core.utils.command.CommandManager;
import mytown.core.utils.command.CommandNode;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

/**
 * Created by AfterWind on 8/29/2014.
 * All commands that require the assistant permission go here
 */
public class CommandsAssistant extends Commands {

    @CommandNode(
            name = "setspawn",
            permission = "mytown.cmd.assistant.setspawn",
            parentName = "mytown.cmd")
    public static void setSpawnCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(player);
        if (res == null)
            throw new CommandException("Failed to get/make Resident"); // TODO Localize
        Town town = res.getSelectedTown();
        if (!town.isChunkInTown(player.dimension, player.chunkCoordX, player.chunkCoordZ))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.setspawn.notintown", town.getName()));

        town.getSpawn().setDim(player.dimension).setPosition((float) player.posX, (float) player.posY, (float) player.posZ).setRotation(player.cameraYaw, player.cameraPitch);
        getDatasource().saveTown(town);

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.setspawn"));
    }

    @CommandNode(
            name = "claim",
            permission = "mytown.cmd.assistant.claim",
            parentName = "mytown.cmd")
    public static void claimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(player);
        if (res == null)
            throw new CommandException("Failed to get/make resident"); // TODO Localize
        Town town = res.getSelectedTown();
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.claim.already"));
        if (checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town)) // Checks if the player can claim far
            Assert.Perm(player, "mytown.cmd.assistant.claim.far");
        Block block = getDatasource().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, town);
        if (block == null)
            throw new CommandException("Failed to create Block"); // TODO Localize
        getDatasource().saveBlock(block);
        res.sendMessage(getLocal().getLocalization("mytown.notification.townblock.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
    }

    @CommandNode(
            name = "unclaim",
            permission = "mytown.cmd.assistant.unclaim",
            parentName = "mytown.cmd")
    public static void unclaimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer pl = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(pl);
        if (res == null)
            throw new CommandException("Failed to get/make Resident"); // TODO Localize
        Block block = getDatasource().getBlock(pl.dimension, pl.chunkCoordX, pl.chunkCoordZ);
        if (block == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.claim.notexist"));
        Town town = block.getTown();
        if (!town.hasResident(res))
            throw new CommandException("Your not part of that town!"); // TODO Localize
        /*
        No reason to be here
        if (res.getTownRank(town).hasPermission(permNode))
            throw new CommandException("commands.generic.permission");
            */
        if (!block.isPointIn(town.getSpawn().getDim(), town.getSpawn().getX(), town.getSpawn().getZ())) {
            getDatasource().deleteBlock(block);
            res.sendMessage(getLocal().getLocalization("mytown.notification.block.removed", block.getX() << 4, block.getZ() << 4, block.getX() << 4 + 15, block.getZ() << 4 + 15, town.getName()));
        } else {
            throw new CommandException("You cannot delete the Block containing the spawn point!");
        }

    }

    @CommandNode(
            name = "blocks",
            permission = "mytown.cmd.assistant.blocks",
            parentName = "mytown.cmd")
    public static void blocksCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.assistant.blocks");
    }

    @CommandNode(
            name = "invite",
            permission = "mytown.cmd.assistant.invite",
            parentName = "mytown.cmd")
    public static void inviteCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res == null)
            throw new CommandException("Failed to get/make Resident"); // TODO Localize
        if (res.getTowns().size() == 0)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        /*
        if (!res.getTownRank().hasPermission(permNode))
            throw new CommandException("commands.generic.permission");
            */
        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.invite"));
        if (!getDatasource().hasResident(args.get(0)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.resident.notexist", args.get(0)));

        Town town = res.getSelectedTown();
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        if (town.hasResident(args.get(0)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.invite.already", args.get(0), town.getName()));

        Resident target = getDatasource().getOrMakeResident(args.get(0));
        target.addInvite(town);
        target.sendMessage(getLocal().getLocalization("mytown.notification.town.invited", town.getName()));
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.sent", args.get(0)));
    }

    @CommandNode(
            name = "perm",
            permission = "mytown.cmd.assistant.perm",
            parentName = "mytown.cmd")
    public static void permCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.assistant.perm");
    }

    @CommandNode(
            name = "town",
            permission = "mytown.cmd.assistant.perm.town",
            parentName = "mytown.cmd.assistant.perm")
    public static void permTownCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.assistant.perm.town");
    }

    @CommandNode(
            name = "set",
            permission = "mytown.cmd.assistant.perm.town.set",
            parentName = "mytown.cmd.assistant.perm.town")
    public static void permSetTownCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 2)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.err.perm.set.usage"));
        Town town = getDatasource().getOrMakeResident(sender).getSelectedTown();
        Flag flag = town.getFlag(args.get(0));
        if (flag == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.flagNotExists", args.get(0)));
        if (flag.setValueFromString(args.get(1))) {
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.perm.set.success", args.get(0), args.get(1));
        } else
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.perm.valueNotValid", args.get(1)));
        getDatasource().saveFlag(flag, town);
    }

    @CommandNode(
            name = "promote",
            permission = "mytown.cmd.assistant.promote",
            parentName = "mytown.cmd")
    public static void promoteCommand(ICommandSender sender, List<String> args) {

        // /t promote <user> <rank>
        if (args.size() < 2)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.promote"));
        Resident resSender = getDatasource().getOrMakeResident(sender);
        Resident resTarget = getDatasource().getOrMakeResident(args.get(0));
        Town town = resSender.getSelectedTown();

        if (resTarget == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.resident.notexist", args.get(0)));
        if (!resTarget.getTowns().contains(town))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.resident.notsametown", args.get(0), town.getName()));
        if (!resSender.getSelectedTown().hasRankName(args.get(1)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.rank.notexist", args.get(1), town.getName()));

        //TODO: implement this properly
        if (args.get(1).equalsIgnoreCase("mayor"))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.promote.notMayor"));
        Rank rank = town.getRank(args.get(1));
        if(getDatasource().updateResidentToTownLink(resTarget, town, rank)) {
            resSender.sendMessage(getLocal().getLocalization("mytown.cmd.promote.success.sender"));
            resTarget.sendMessage(getLocal().getLocalization("mytown.cmd.promote.success.target", rank.getName(), town.getName()));
        }

    }
    @CommandNode(
            name = "add",
            permission = "mytown.cmd.assistant.ranks.add",
            parentName = "mytown.cmd.everyone.ranks")
    public static void ranksAddCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.ranks"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = res.getSelectedTown();

        if (town.hasRankName(args.get(0)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.add.already", args.get(0)));
        if (!town.hasRankName(args.get(1)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.add.notexist", args.get(1)));


        Rank rank = new Rank(args.get(0), town.getRank(args.get(1)).getPermissions(), town);
        getDatasource().saveRank(rank, false); // TODO: Set default properly?
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.add", args.get(0), town.getName()));
    }

    @CommandNode(
            name = "remove",
            permission = "mytown.cmd.assistant.ranks.remove",
            parentName = "mytown.cmd.everyone.ranks")
    public static void ranksRemoveCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.ranks"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = res.getSelectedTown();
        if (!town.hasRankName(args.get(0)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.rem.notexist", args.get(0), town.getName()));

        if (getDatasource().deleteRank(town.getRank(args.get(0)))) {
            res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.rem", args.get(0), town.getName()));
        } else {
            res.sendMessage(getLocal().getLocalization("mytown.cmd.err.ranks.rem.notallowed", args.get(0)));
        }

    }

    @CommandNode(
            name = "perm",
            permission = "mytown.cmd.assistant.ranks.perm",
            parentName = "mytown.cmd.everyone.ranks")
    public static void ranksPermCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.assistant.ranks.perm");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.assistant.ranks.perm.list",
            parentName = "mytown.cmd.assistant.ranks.perm")
    public static void ranksPermListCommand(ICommandSender sender, List<String> args) {

        Rank rank;
        Resident res = getDatasource().getOrMakeResident(sender);
        if (args.size() == 0) {
            rank = res.getTownRank();
        } else {
            rank = getUniverse().getRanksMap().get(args.get(0));
        }

        if (rank == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.notexist", args.get(0), res.getSelectedTown().getName()));

        String msg = "";
        for (String s : rank.getPermissions()) {
            msg += '\n' + s;
        }

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.list", rank.getName(), rank.getTown().getName(), msg));

    }

    @CommandNode(
            name = "add",
            permission = "mytown.cmd.assistant.ranks.perm.add",
            parentName = "mytown.cmd.assistant.ranks.perm")
    public static void ranksPermAddCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 2)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.ranks.perm"));

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = res.getSelectedTown();

        Rank rank = town.getRank(args.get(0));

        if (rank == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.rem.notexist", args.get(0), town.getName()));

        if (!CommandManager.commandList.keySet().contains(args.get(1)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.perm.notexist", args.get(1)));


        // Adding permission if everything is alright
        if (rank.addPermission(args.get(1))) {
            getDatasource().saveRank(rank, rank.getTown().getDefaultRank().equals(rank));
            res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.add", args.get(1), args.get(0)));
        } else
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.perm.add.failed", args.get(1)));
    }

    @CommandNode(
            name = "remove",
            permission = "mytown.cmd.assistant.ranks.perm.remove",
            parentName = "mytown.cmd.assistant.ranks.perm")
    public static void ranksPermRemoveCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 2)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.ranks.perm"));

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = res.getSelectedTown();

        Rank rank = town.getRank(args.get(0));

        if (rank == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.rem.notexist", args.get(0), town.getName()));

        if (!CommandManager.commandList.keySet().contains(args.get(1)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.perm.notexist", args.get(1)));

        // Removing permission if everything is alright
        if (rank.removePermission(args.get(1))) {
            getDatasource().saveRank(rank, rank.getTown().getDefaultRank().equals(rank));
            res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.remove", args.get(1), args.get(0)));
        } else
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.ranks.perm.remove.failed", args.get(1)));
    }

    // Temporary here, might integrate in the methods
    private static boolean checkNearby(int dim, int x, int z, Town town) {
        int[] dx = {1, 0, -1, 0};
        int[] dz = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++)
            if (getDatasource().hasBlock(dim, x + dx[i], z + dz[i], true, town))
                return true;
        return false;
    }
}
