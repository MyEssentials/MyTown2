package mytown.commands;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandManager;
import mytown.core.utils.command.CommandNode;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.util.ChunkPos;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
import mytown.util.exceptions.MyTownWrongUsageException;
import net.minecraft.command.ICommandSender;
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
        Town town = getTownFromResident(res);

        if(!town.isPointInTown(player.dimension, (int)player.posX, (int)player.posZ))
            throw new MyTownCommandException(getLocal().getLocalization("mytown.cmd.err.setspawn.notintown", town.getName()));

        makePayment(player, Config.costAmountSetSpawn);

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
        Town town = getTownFromResident(res);

        if(args.size() < 1) {

            boolean isClaimFar = false;

            if (town.getBlocks().size() >= town.getMaxBlocks())
                throw new MyTownCommandException("mytown.cmd.err.town.maxBlocks");
            if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ))
                throw new MyTownCommandException("mytown.cmd.err.claim.already");
            if (!checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town)) {
                if (!Config.allowFarClaims)
                    throw new MyTownCommandException("mytown.cmd.err.claim.far.notAllowed");
                isClaimFar = true;
            }

            //Assert.Perm(player, "mytown.cmd.assistant.claim.far");

            makePayment(player, isClaimFar ? Config.costAmountClaimFar : Config.costAmountClaim);

            TownBlock block = getDatasource().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, town);
            if (block == null)
                throw new MyTownCommandException("mytown.cmd.err.claim.failed");

            getDatasource().saveBlock(block);
            res.sendMessage(getLocal().getLocalization("mytown.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
        } else {
            //if(!MyTownUtils.tryParseInt(args.get(0)))
            //    throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));
            //int radius = Integer.parseInt(args.get(0));
            //List<ChunkPos> chunks = MyTownUtils.getChunksInBox((int)(player.posX - radius*16), )

        }
    }

    @CommandNode(
            name = "unclaim",
            permission = "mytown.cmd.assistant.unclaim",
            parentName = "mytown.cmd")
    public static void unclaimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer pl = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(sender);
        TownBlock block = getBlockAtResident(res);
        Town town = res.getSelectedTown();

        if(town != block.getTown())
            throw new MyTownCommandException("mytown.cmd.err.unclaim.notInTown");
        if (block.isPointIn(town.getSpawn().getDim(), town.getSpawn().getX(), town.getSpawn().getZ()))
            throw new MyTownCommandException("mytown.cmd.err.unclaim.spawnPoint");

        getDatasource().deleteBlock(block);
        res.sendMessage(getLocal().getLocalization("mytown.notification.block.removed", block.getX() << 4, block.getZ() << 4, block.getX() << 4 + 15, block.getZ() << 4 + 15, town.getName()));
        makeRefund(pl, Config.costAmountClaim);
        sendMessageBackToSender(sender, getLocal().getLocalization("mytown.notification.town.payReturn", Config.costAmountClaim, Config.costItemName));
    }

    @CommandNode(
            name = "blocks",
            permission = "mytown.cmd.assistant.blocks",
            parentName = "mytown.cmd")
    public static void blocksCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.assistant.blocks");
    }

    @CommandNode(
            name = "invite",
            permission = "mytown.cmd.assistant.invite",
            parentName = "mytown.cmd",
            completionKeys = {"residentCompletion"})
    public static void inviteCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.invite");
        Resident target = getResidentFromName(args.get(0));
        if (town.hasResident(args.get(0)))
            throw new MyTownCommandException("mytown.cmd.err.invite.already", args.get(0), town.getName());

        getDatasource().saveTownInvite(target, town);
        target.sendMessage(getLocal().getLocalization("mytown.notification.town.invited", town.getName()));
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.invite.sent", args.get(0)));
    }

    @CommandNode(
            name = "perm",
            permission = "mytown.cmd.assistant.perm",
            parentName = "mytown.cmd")
    public static void permCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.assistant.perm");
    }

    @CommandNode(
            name = "town",
            permission = "mytown.cmd.assistant.perm.town",
            parentName = "mytown.cmd.assistant.perm")
    public static void permTownCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.assistant.perm.town");
    }

    @CommandNode(
            name = "set",
            permission = "mytown.cmd.assistant.perm.town.set",
            parentName = "mytown.cmd.assistant.perm.town",
            completionKeys = "flagCompletion")
    public static void permSetTownCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 2)
            throw new MyTownWrongUsageException("mytown.cmd.err.perm.set.usage");
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        Flag flag = getFlagFromName(town, args.get(0));

        if (flag.setValueFromString(args.get(1))) {
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.perm.set.success", args.get(0), args.get(1));
        } else
            throw new MyTownCommandException("mytown.cmd.err.perm.valueNotValid", args.get(1));
        getDatasource().saveFlag(flag, town);
    }

    @CommandNode(
            name = "whitelist",
            permission = "mytown.cmd.assistant.perm.town.whitelist",
            parentName = "mytown.cmd.assistant.perm.town",
            completionKeys = {"flagCompletionWhitelist"})
    public static void permTownWhitelistCommand(ICommandSender sender, List<String> args) {
        if (args.size() == 0)
            throw new MyTownCommandException("mytown.cmd.usage.plot.whitelist.add");

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        FlagType flagType = getFlagTypeFromName(args.get(1));

        if (flagType.isWhitelistable())
            res.startBlockSelection(flagType, town.getName());
        else
            throw new MyTownCommandException("mytown.cmd.err.flag.notForWhitelist");

    }

    @CommandNode(
            name = "promote",
            permission = "mytown.cmd.assistant.promote",
            parentName = "mytown.cmd",
            completionKeys = {"residentCompletion", "rankCompletion"})
    public static void promoteCommand(ICommandSender sender, List<String> args) {

        // /t promote <user> <rank>
        if (args.size() < 2)
            throw new MyTownWrongUsageException("mytown.cmd.usage.promote");
        Resident resSender = getDatasource().getOrMakeResident(sender);
        Resident resTarget = getResidentFromName(args.get(0));
        Town town = getTownFromResident(resSender);

        if (!resTarget.getTowns().contains(town))
            throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", args.get(0), town.getName());

        //TODO: implement this properly
        if (args.get(1).equalsIgnoreCase("mayor"))
            throw new MyTownCommandException("mytown.cmd.err.promote.notMayor");
        Rank rank = getRankFromTown(town, args.get(1));
        if (getDatasource().updateResidentToTownLink(resTarget, town, rank)) {
            resSender.sendMessage(getLocal().getLocalization("mytown.cmd.promote.success.sender", resTarget.getPlayerName(), rank.getName()));
            resTarget.sendMessage(getLocal().getLocalization("mytown.cmd.promote.success.target", rank.getName(), town.getName()));
        }

    }

    public static class ModifyRanks {

        @CommandNode(
                name = "add",
                permission = "mytown.cmd.assistant.ranks.add",
                parentName = "mytown.cmd.everyone.ranks",
                completionKeys = {"-", "ranksCompletion"})
        public static void ranksAddCommand(ICommandSender sender, List<String> args) {

            if (args.size() < 2)
                throw new MyTownWrongUsageException("mytown.cmd.usage.ranks");
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            if (town.hasRankName(args.get(0)))
                throw new MyTownCommandException("mytown.cmd.err.ranks.add.already", args.get(0));
            if (!town.hasRankName(args.get(1)))
                throw new MyTownCommandException("mytown.cmd.err.ranks.add.notexist", args.get(1));


            Rank rank = new Rank(args.get(0), town.getRank(args.get(1)).getPermissions(), town);
            getDatasource().saveRank(rank, false); // TODO: Set default properly?
            res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.add", args.get(0), town.getName()));
        }

        @CommandNode(
                name = "remove",
                permission = "mytown.cmd.assistant.ranks.remove",
                parentName = "mytown.cmd.everyone.ranks",
                completionKeys = {"rankCompletion"})
        public static void ranksRemoveCommand(ICommandSender sender, List<String> args) {

            if (args.size() < 1)
                throw new MyTownWrongUsageException("mytown.cmd.usage.ranks");
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = res.getSelectedTown();
            Rank rank = getRankFromTown(town, args.get(0));

            if (town.getDefaultRank().equals(rank) || Rank.theMayorDefaultRank.equals(rank.getName()))
                throw new MyTownCommandException("mytown.cmd.err.ranks.cantDelete");

            if (getDatasource().deleteRank(rank)) {
                res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.rem", args.get(0), town.getName()));
            } else {
                res.sendMessage(getLocal().getLocalization("mytown.cmd.err.ranks.rem.notallowed", args.get(0)));
            }
        }


        @CommandNode(
                name = "add",
                permission = "mytown.cmd.assistant.ranks.perm.add",
                parentName = "mytown.cmd.assistant.ranks.perm")
        public static void ranksPermAddCommand(ICommandSender sender, List<String> args) {

            if (args.size() < 2)
                throw new MyTownWrongUsageException("mytown.cmd.usage.ranks.perm");

            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            Rank rank = getRankFromTown(town, args.get(0));

            if (!CommandManager.commandList.keySet().contains(args.get(1)))
                throw new MyTownCommandException("mytown.cmd.err.ranks.perm.notexist", args.get(1));

            // Adding permission if everything is alright
            if (rank.addPermission(args.get(1))) {
                getDatasource().saveRank(rank, rank.getTown().getDefaultRank().equals(rank));
                res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.add", args.get(1), args.get(0)));
            } else
                throw new MyTownCommandException("mytown.cmd.err.ranks.perm.add.failed", args.get(1));
        }

        @CommandNode(
                name = "remove",
                permission = "mytown.cmd.assistant.ranks.perm.remove",
                parentName = "mytown.cmd.assistant.ranks.perm")
        public static void ranksPermRemoveCommand(ICommandSender sender, List<String> args) {

            if (args.size() < 2)
                throw new MyTownWrongUsageException("mytown.cmd.usage.ranks.perm");

            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            Rank rank = getRankFromTown(town, args.get(0));

            if (!CommandManager.commandList.keySet().contains(args.get(1)))
                throw new MyTownCommandException("mytown.cmd.err.ranks.perm.notexist", args.get(1));

            // Removing permission if everything is alright
            if (rank.removePermission(args.get(1))) {
                getDatasource().saveRank(rank, rank.getTown().getDefaultRank().equals(rank));
                res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.remove", args.get(1), args.get(0)));
            } else
                throw new MyTownCommandException("mytown.cmd.err.ranks.perm.remove.failed", args.get(1));
        }
    }


    @CommandNode(
            name = "perm",
            permission = "mytown.cmd.assistant.ranks.perm",
            parentName = "mytown.cmd.everyone.ranks")
    public static void ranksPermCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.assistant.ranks.perm");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.assistant.ranks.perm.list",
            parentName = "mytown.cmd.assistant.ranks.perm")
    public static void ranksPermListCommand(ICommandSender sender, List<String> args) {

        Rank rank;
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        if (args.size() == 0) {
            rank = getRankFromResident(res);
        } else {
            rank = getRankFromTown(town, args.get(0));
        }

        String msg = "";
        for (String s : rank.getPermissions()) {
            msg += '\n' + s;
        }

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.list", rank.getName(), rank.getTown().getName(), msg));
    }

    @CommandNode(
            name = "pass",
            permission = "mytown.cmd.mayor.pass",
            parentName = "mytown.cmd",
            completionKeys = {"residentCompletion"})
    public static void passCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownCommandException("mytown.cmd.usage.leave.pass");

        Resident res = getDatasource().getOrMakeResident(sender);
        Resident target = getResidentFromName(args.get(0));
        Town town = getTownFromResident(res);

        if (!town.hasResident(target)) {
            throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", target.getPlayerName(), town.getName());
        }
        if (town.getResidentRank(res).getName().equals(Rank.theMayorDefaultRank)) {
            getDatasource().updateResidentToTownLink(target, town, town.getRank(Rank.theMayorDefaultRank));
            target.sendMessage(getLocal().getLocalization("mytown.notification.town.mayorShip.passed"));
            getDatasource().updateResidentToTownLink(res, town, town.getDefaultRank());
            res.sendMessage(getLocal().getLocalization("mytown.notification.town.mayorShip.taken"));
        } else {
            //...
        }
    }

    @CommandNode(
            name = "limit",
            permission = "mytown.cmd.assistant.plot.limit",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotLimitCommand(ICommandSender sender, List<String> args) {
        callSubFunctions(sender, args, "mytown.cmd.assistant.plot.limit");
    }

    @CommandNode(
            name = "show",
            permission = "mytown.cmd.assistant.plot.limit.show",
            parentName = "mytown.cmd.everyone.plot.limit")
    public static void plotLimitShowCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        res.sendMessage(getLocal().getLocalization("mytown.notification.plot.limit", town.getMaxPlots()));
    }


        @CommandNode(
            name = "set",
            permission = "mytown.cmd.assistant.plot.limit.set",
            parentName = "mytown.cmd.assistant.plot.limit")
    public static void plotLimitSetCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            throw new MyTownWrongUsageException("mytown.cmd.usage.plot.limit.set");
        }
        try {
            int limit = Integer.parseInt(args.get(0));
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            town.setMaxPlots(limit);
            getDatasource().saveTown(town);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.limit", town.getMaxPlots()));
        } catch (NumberFormatException ex) {
            throw new MyTownWrongUsageException("mytown.cmd.usage.plot.limit.set");
        }
    }

    @CommandNode(
            name = "kick",
            permission = "mytown.cmd.assistant.kick",
            parentName = "mytown.cmd",
            completionKeys = {"residentCompletion"})
    public static void kickCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            throw new MyTownWrongUsageException("mytown.cmd.usage.kick");
        }
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident target = getResidentFromName(args.get(0));
        Town town = getTownFromResident(res);
        if (!target.getTowns().contains(town)) {
            throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", args.get(0), town.getName());
        }
        if (target == res) {
            throw new MyTownCommandException("mytown.cmd.err.kick.self");
        }

        getDatasource().unlinkResidentFromTown(target, town);
        target.sendMessage(getLocal().getLocalization("mytown.notification.town.kicked", town.getName()));
        town.notifyEveryone(getLocal().getLocalization("mytown.notification.town.left", target.getPlayerName(), town.getName()));
    }

    @CommandNode(
            name = "delete",
            permission = "mytown.cmd.mayor.leave.delete",
            parentName = "mytown.cmd.everyone.leave")
    public static void leaveDeleteCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        EntityPlayer player = (EntityPlayer)sender;

        if (town.getResidentRank(res).getName().equals(Rank.theMayorDefaultRank)) {
            town.notifyEveryone(getLocal().getLocalization("mytown.notification.town.deleted", town.getName(), res.getPlayerName()));
            makeRefund(player, Config.costAmountClaim * town.getBlocks().size());
            getDatasource().deleteTown(town);
        }
    }


    // Temporary here, might integrate in the methods
    protected static boolean checkNearby(int dim, int x, int z, Town town) {
        int[] dx = {1, 0, -1, 0};
        int[] dz = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++)
            if (getDatasource().hasBlock(dim, x + dx[i], z + dz[i], town))
                return true;
        return false;
    }
}
