package mytown.commands;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.Assert;
import mytown.core.utils.command.CommandNode;
import mytown.entities.Block;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

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
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.setspawn.notintown", town.getName()));
        town.getSpawn().setDim(player.dimension).setPosition((float) player.posX, (float) player.posY, (float) player.posZ).setRotation(player.cameraYaw, player.cameraPitch);
        getDatasource().saveTown(town);
        ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.setspawn");
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
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.claim.already"));
        if (checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town)) // Checks if the player can claim far
            Assert.Perm(player, "mytown.cmd.assistant.claim.far");
        Block block = getDatasource().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, town);
        if (block == null)
            throw new CommandException("Failed to create Block"); // TODO Localize
        getDatasource().saveBlock(block);
        res.sendMessage(MyTown.getLocal().getLocalization("mytown.notification.townblock.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
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
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.claim.notexist"));
        Town town = block.getTown();
        if (!town.hasResident(res))
            throw new CommandException("Your not part of that town!"); // TODO Localize
        /*
        No reason to be here
        if (res.getTownRank(town).hasPermission(permNode))
            throw new CommandException("commands.generic.permission");
            */
        if (block.isPointIn(town.getSpawn().getDim(), town.getSpawn().getX(), town.getSpawn().getZ())) {
            town.setSpawn(null); // Removes the Town's spawn point if in this Block
        }
        getDatasource().deleteBlock(block);
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
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        /*
        if (!res.getTownRank().hasPermission(permNode))
            throw new CommandException("commands.generic.permission");
            */
        if (args.size() < 1)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.invite"));
        if (!getDatasource().hasResident(args.get(0)))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.resident.notexist", args.get(0)));

        Town town = res.getSelectedTown();
        if (town == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        if (town.hasResident(args.get(0)))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.already", args.get(0), town.getName()));

        Resident target = getDatasource().getOrMakeResident(args.get(0));
        target.addInvite(town);
        target.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.invited", town.getName()));
        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.sent", args.get(0)));
    }

    @CommandNode(
            name = "perm",
            permission = "mytown.cmd.assistant.perm",
            parentName = "mytown.cmd")
    public static void permCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.assistant.perm");
    }

    @CommandNode(
            name = "set",
            permission = "mytown.cmd.assistant.perm.set",
            parentName = "mytown.cmd.assistant.perm")
    public static void permSetCommand(ICommandSender sender, List<String> args) {
        /*
        TODO: Also fix this

        if (args.size() < 2)
            throw new WrongUsageException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.perm.set.usage"));
        Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
        ITownFlag flag = town.getFlag(args.get(0));
        if (flag == null)
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.flagNotExists", args.get(0)));
        try {
            if (args[1].equals("true")) {
                flag.setValue(true);
            } else if (args[1].equals("false")) {
                flag.setValue(false);
            } else
                throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.perm.valueNotValid", args[1]));
            ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.perm.set.success", args.get(0), args[1]);

            getDatasource().updateTownFlag(flag);
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
            e.printStackTrace();
        }
        */
    }

    @CommandNode(
            name = "promote",
            permission = "mytown.cmd.assistant.promote",
            parentName = "mytown.cmd")
    public static void promoteCommand(ICommandSender sender, List<String> args) {
        /*
        // /t promote <user> <rank>
        if (args.size() < 2)
            throw new WrongUsageException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.usage.promote"));
        Resident resSender = getDatasource().getResident(sender.getCommandSenderName());
        Resident resTarget = getDatasource().getResident(args.get(0));
        Town town = resSender.getSelectedTown();

        if (resTarget == null)
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.resident.notexist", args.get(0)));
        if (!resTarget.getTowns().contains(town))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.resident.notsametown", args.get(0), town.getName()));
        if (!resSender.getSelectedTown().hasRankName(args[1]))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.rank.notexist", args[1], town.getName()));
        if (args[1].equalsIgnoreCase("mayor"))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.promote.notMayor"));
        try {
            Rank rank = town.getRank(args[1]);
            town.promoteResident(resTarget, rank);
            getDatasource().updateLinkResidentToTown(resTarget, town);
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
            e.printStackTrace();
        }
        */
    }
    @CommandNode(
            name = "add",
            permission = "cmd.assistant.ranks.add",
            parentName = "cmd.everyone.ranks")
    public static void ranksAddCommand(ICommandSender sender, List<String> args) {
        /*
        if (args.size() < 1)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.ranks"));

        Town town = getUniverse().getResidentsMap().get(sender.getCommandSenderName()).getSelectedTown();

        if (town.hasRankName(args.get(0)))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.add.already", args.get(0)));
        if (!town.hasRankName(args[1]))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.add.notexist", args[1]));

        try {
            Rank rank = new Rank(args.get(0), town.getRank(args[1]).getPermissions(), town);
            getDatasource().insertRank(rank);
            ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.add", args.get(0), town.getName());
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
            e.printStackTrace();
        }
        */
    }

    @CommandNode(
            name = "remove",
            permission = "cmd.assistant.ranks.remove",
            parentName = "cmd.everyone.ranks")
    public static void ranksRemoveCommand(ICommandSender sender, List<String> args) {

        /*
        if (args.size() < 1)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.ranks"));
        Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
        if (!town.hasRankName(args.get(0)))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.rem.notexist", args.get(0), town.getName()));
        try {
            if (getDatasource().deleteRank(town.getRank(args.get(0)))) {
                ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.rem", args.get(0), town.getName());
            } else {
                ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.err.ranks.rem.notallowed", args.get(0));
            }
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
            e.printStackTrace();
        }
        */
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
        /*
        Rank rank;
        if (args.size() == 0) {
            rank = getDatasource().getResident(sender.getCommandSenderName()).getTownRank();
        } else {
            rank = getDatasource().getRank(args.get(0), getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown());
        }

        if (rank == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.notexist", args.get(0), getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown().getName()));

        String msg = "";
        for (String s : rank.getPermissions()) {
            msg += '\n' + s;
        }
        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.perm.list", rank.getName(), rank.getTown().getName(), msg);
        */
    }

    @CommandNode(
            name = "add",
            permission = "mytown.cmd.assistant.ranks.perm.add",
            parentName = "mytown.cmd.assistant.ranks.perm")
    public static void ranksPermAddCommand(ICommandSender sender, List<String> args) {
        /*
        if (args.size() < 2)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.ranks.perm"));

        Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();

        if (getDatasource().getRank(args.get(0), town) == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.rem.notexist", args.get(0), town.getName()));
        /*
        FIXME
        if (!CommandUtils.permissionList.containsValue(args[1]))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.perm.notexist", args[1]));
        */
        /*
        try {
            // Adding permission if everything is alright
            if (town.getRank(args.get(0)).addPermission(args[1])) {
                getDatasource().updateRank(town.getRank(args.get(0)));
                ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.perm.add", args[1], args.get(0));
            } else
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.perm.add.failed", args[1]));
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
            e.printStackTrace();
        }
        */
    }

    @CommandNode(
            name = "remove",
            permission = "mytown.cmd.assistant.ranks.perm.remove",
            parentName = "mytown.cmd.assistant.ranks.perm")
    public static void ranksPermRemoveCommand(ICommandSender sender, List<String> args) {
        /*
                if (args.size() < 2)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.ranks.perm"));

        Town town = X_DatasourceProxy.getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();

        if (X_DatasourceProxy.getDatasource().getRank(args.get(0), town) == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.rem.notexist", args.get(0), town.getName()));
        /*
        FIXME
        if (!CommandUtils.permissionList.containsValue(args[1]))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.perm.notexist", args[1]));
        */
        /*
        try {
            // Removing permission if everything is alright
            if (town.getRank(args.get(0)).removePermission(args[1])) {
                X_DatasourceProxy.getDatasource().updateRank(town.getRank(args.get(0)));
                ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.perm.remove", args[1], args.get(0));
            } else
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.perm.remove.failed", args[1]));
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
            e.printStackTrace();
        }
         */
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
