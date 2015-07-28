package mytown.commands;

import myessentials.command.CommandManagerNew;
import myessentials.command.CommandResponse;
import myessentials.command.annotation.Command;
import mytown.config.Config;
import myessentials.entities.ChunkPos;
import myessentials.utils.ChatUtils;
import myessentials.utils.MathUtils;
import myessentials.utils.StringUtils;
import myessentials.utils.WorldUtils;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.entities.tools.WhitelisterTool;
import mytown.proxies.EconomyProxy;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
import mytown.util.exceptions.MyTownWrongUsageException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Iterator;
import java.util.List;

/**
 * All commands that require the assistant permission go here
 */
public class CommandsAssistant extends Commands {

    @Command(
            name = "setspawn",
            permission = "mytown.cmd.assistant.setspawn",
            parentName = "mytown.cmd")
    public static CommandResponse setSpawnCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(player);
        Town town = getTownFromResident(res);

        if (!town.isPointInTown(player.dimension, (int) player.posX, (int) player.posZ))
            throw new MyTownCommandException(getLocal().getLocalization("mytown.cmd.err.setspawn.notintown", town.getName()));

        makePayment(player, Config.costAmountSetSpawn);

        town.getSpawn().setDim(player.dimension).setPosition((float) player.posX, (float) player.posY, (float) player.posZ).setRotation(player.cameraYaw, player.cameraPitch);
        getDatasource().saveTown(town);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.setspawn"));
        return CommandResponse.DONE;
    }

    @Command(
            name = "claim",
            permission = "mytown.cmd.assistant.claim",
            parentName = "mytown.cmd")
    public static CommandResponse claimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(player);
        Town town = getTownFromResident(res);

        boolean isFarClaim = false;

        if (args.size() < 1) {
            if (town.getBlocks().size() >= town.getMaxBlocks())
                throw new MyTownCommandException("mytown.cmd.err.town.maxBlocks", 1);
            if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ))
                throw new MyTownCommandException("mytown.cmd.err.claim.already");
            if (!checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town)) {
                if (town.getFarClaims() >= town.getMaxFarClaims())
                    throw new MyTownCommandException("mytown.cmd.err.claim.far.notAllowed");
                isFarClaim = true;
            }
            for (int x = player.chunkCoordX - Config.distanceBetweenTowns; x <= player.chunkCoordX + Config.distanceBetweenTowns; x++) {
                for (int z = player.chunkCoordZ - Config.distanceBetweenTowns; z <= player.chunkCoordZ + Config.distanceBetweenTowns; z++) {
                    Town nearbyTown = MyTownUtils.getTownAtPosition(player.dimension, x, z);
                    if (nearbyTown != null && nearbyTown != town && !(Boolean) nearbyTown.getValue(FlagType.NEARBY))
                        throw new MyTownCommandException("mytown.cmd.err.claim.tooClose", nearbyTown.getName(), Config.distanceBetweenTowns);
                }
            }

            if (isFarClaim && town.getFarClaims() + 1 > town.getMaxFarClaims())
                throw new MyTownCommandException("mytown.cmd.err.claim.far.notAllowed");

            int price = (isFarClaim ? Config.costAmountClaimFar : Config.costAmountClaim) + Config.costAdditionClaim * town.getBlocks().size();

            makeBankPayment(player, town, price);

            TownBlock block = getDatasource().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, isFarClaim, price, town);
            if (block == null)
                throw new MyTownCommandException("mytown.cmd.err.claim.failed");

            getDatasource().saveBlock(block);
            res.sendMessage(getLocal().getLocalization("mytown.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
        } else {
            if (!StringUtils.tryParseInt(args.get(0)))
                throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));

            int radius = Integer.parseInt(args.get(0));
            List<ChunkPos> chunks = WorldUtils.getChunksInBox((int) (player.posX - radius * 16), (int) (player.posZ - radius * 16), (int) (player.posX + radius * 16), (int) (player.posZ + radius * 16));
            isFarClaim = true;

            for (Iterator<ChunkPos> it = chunks.iterator(); it.hasNext(); ) {
                ChunkPos chunk = it.next();
                if (checkNearby(player.dimension, chunk.getX(), chunk.getZ(), town)) {
                    isFarClaim = false;
                }
                if (getDatasource().hasBlock(player.dimension, chunk.getX(), chunk.getZ()))
                    it.remove();

                for (int x = chunk.getX() - Config.distanceBetweenTowns; x <= chunk.getX() + Config.distanceBetweenTowns; x++) {
                    for (int z = chunk.getZ() - Config.distanceBetweenTowns; z <= chunk.getZ() + Config.distanceBetweenTowns; z++) {
                        Town nearbyTown = MyTownUtils.getTownAtPosition(player.dimension, x, z);
                        if (nearbyTown != null && nearbyTown != town && !(Boolean) nearbyTown.getValue(FlagType.NEARBY))
                            throw new MyTownCommandException("mytown.cmd.err.claim.tooClose", nearbyTown.getName(), Config.distanceBetweenTowns);
                    }
                }
            }

            if (town.getBlocks().size() + chunks.size() > town.getMaxBlocks())
                throw new MyTownCommandException("mytown.cmd.err.town.maxBlocks", chunks.size());

            if (isFarClaim && town.getFarClaims() + 1 > town.getMaxFarClaims())
                throw new MyTownCommandException("mytown.cmd.err.claim.far.notAllowed");

            makeBankPayment(player, town, (isFarClaim ? Config.costAmountClaimFar + Config.costAmountClaim * (chunks.size() - 1) : Config.costAmountClaim * chunks.size())
                    + MathUtils.sumFromNtoM(town.getBlocks().size(), town.getBlocks().size() + chunks.size() - 1) * Config.costAdditionClaim);

            for (ChunkPos chunk : chunks) {
                int price = (isFarClaim ? Config.costAmountClaimFar : Config.costAmountClaim) + Config.costAdditionClaim * town.getBlocks().size();
                TownBlock block = getDatasource().newBlock(player.dimension, chunk.getX(), chunk.getZ(), isFarClaim, price, town);
                // Only one of the block will be a farClaim, rest will be normal claim
                isFarClaim = false;
                getDatasource().saveBlock(block);
                res.sendMessage(getLocal().getLocalization("mytown.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
            }
        }
        return CommandResponse.DONE;
    }

    @Command(
            name = "unclaim",
            permission = "mytown.cmd.assistant.unclaim",
            parentName = "mytown.cmd")
    public static CommandResponse unclaimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(sender);
        TownBlock block = getBlockAtResident(res);
        Town town = res.getSelectedTown();

        if (town != block.getTown())
            throw new MyTownCommandException("mytown.cmd.err.unclaim.notInTown");
        if (block.isPointIn(town.getSpawn().getDim(), town.getSpawn().getX(), town.getSpawn().getZ()))
            throw new MyTownCommandException("mytown.cmd.err.unclaim.spawnPoint");
        if (!checkNearby(block.getDim(), block.getX(), block.getZ(), town) && town.getBlocks().size() <= 1) {
            throw new MyTownCommandException("mytown.cmd.err.unclaim.lastClaim");
        }

        getDatasource().deleteBlock(block);
        res.sendMessage(getLocal().getLocalization("mytown.notification.block.removed", block.getX() << 4, block.getZ() << 4, block.getX() << 4 + 15, block.getZ() << 4 + 15, town.getName()));
        makeBankRefund(player, town, block.getPricePaid());
        return CommandResponse.DONE;
    }

    @Command(
            name = "invite",
            permission = "mytown.cmd.assistant.invite",
            parentName = "mytown.cmd",
            completionKeys = {"residentCompletion"})
    public static CommandResponse inviteCommand(ICommandSender sender, List<String> args) {
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
        return CommandResponse.DONE;
    }

    @Command(
            name = "set",
            permission = "mytown.cmd.assistant.perm.set",
            parentName = "mytown.cmd.everyone.perm",
            completionKeys = "flagCompletion")
    public static CommandResponse permSetCommand(ICommandSender sender, List<String> args) {

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
        return CommandResponse.DONE;
    }

    @Command(
            name = "whitelist",
            permission = "mytown.cmd.assistant.perm.whitelist",
            parentName = "mytown.cmd.everyone.perm",
            completionKeys = {"flagCompletionWhitelist"})
    public static CommandResponse permWhitelistCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        res.setCurrentTool(new WhitelisterTool(res));
        return CommandResponse.DONE;
    }

    @Command(
            name = "promote",
            permission = "mytown.cmd.assistant.promote",
            parentName = "mytown.cmd",
            completionKeys = {"residentCompletion", "rankCompletion"})
    public static CommandResponse promoteCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            throw new MyTownWrongUsageException("mytown.cmd.usage.promote");
        Resident resSender = getDatasource().getOrMakeResident(sender);
        Resident resTarget = getResidentFromName(args.get(0));
        Town town = getTownFromResident(resSender);

        if (!resTarget.getTowns().contains(town))
            throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", args.get(0), town.getName());

        if (args.get(1).equalsIgnoreCase(Rank.theMayorDefaultRank))
            throw new MyTownCommandException("mytown.cmd.err.promote.notMayor");
        Rank rank = getRankFromTown(town, args.get(1));
        if (getDatasource().updateResidentToTownLink(resTarget, town, rank)) {
            resSender.sendMessage(getLocal().getLocalization("mytown.cmd.promote.success.sender", resTarget.getPlayerName(), rank.getName()));
            resTarget.sendMessage(getLocal().getLocalization("mytown.cmd.promote.success.target", rank.getName(), town.getName()));
        }
        return CommandResponse.DONE;
    }

    public static class ModifyRanks {

        private ModifyRanks() {
        }

        @Command(
                name = "add",
                permission = "mytown.cmd.assistant.ranks.add",
                parentName = "mytown.cmd.everyone.ranks",
                completionKeys = {"-", "ranksCompletion"})
        public static CommandResponse ranksAddCommand(ICommandSender sender, List<String> args) {

            if (args.size() < 2)
                throw new MyTownWrongUsageException("mytown.cmd.usage.ranks");
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            if (town.hasRankName(args.get(0)))
                throw new MyTownCommandException("mytown.cmd.err.ranks.add.already", args.get(0));
            if (!town.hasRankName(args.get(1)))
                throw new MyTownCommandException("mytown.cmd.err.ranks.add.notexist", args.get(1));


            Rank rank = new Rank(args.get(0), town.getRank(args.get(1)).getPermissions(), town);
            getDatasource().saveRank(rank, false);
            res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.add", args.get(0), town.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "mytown.cmd.assistant.ranks.remove",
                parentName = "mytown.cmd.everyone.ranks",
                completionKeys = {"rankCompletion"})
        public static CommandResponse ranksRemoveCommand(ICommandSender sender, List<String> args) {

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
            return CommandResponse.DONE;
        }


        @Command(
                name = "add",
                permission = "mytown.cmd.assistant.ranks.perm.add",
                parentName = "mytown.cmd.assistant.ranks.perm")
        public static CommandResponse ranksPermAddCommand(ICommandSender sender, List<String> args) {

            if (args.size() < 2)
                throw new MyTownWrongUsageException("mytown.cmd.usage.ranks.perm");

            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            Rank rank = getRankFromTown(town, args.get(0));

            if (!CommandManagerNew.getTree("mytown.cmd").hasCommandNode(args.get(1)))
                throw new MyTownCommandException("mytown.cmd.err.ranks.perm.notexist", args.get(1));

            // Adding permission if everything is alright
            if (rank.addPermission(args.get(1))) {
                getDatasource().saveRank(rank, rank.getTown().getDefaultRank().equals(rank));
                res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.add", args.get(1), args.get(0)));
            } else
                throw new MyTownCommandException("mytown.cmd.err.ranks.perm.add.failed", args.get(1));
            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "mytown.cmd.assistant.ranks.perm.remove",
                parentName = "mytown.cmd.assistant.ranks.perm")
        public static CommandResponse ranksPermRemoveCommand(ICommandSender sender, List<String> args) {

            if (args.size() < 2)
                throw new MyTownWrongUsageException("mytown.cmd.usage.ranks.perm");

            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            Rank rank = getRankFromTown(town, args.get(0));

            if (!CommandManagerNew.getTree("mytown.cmd").hasCommandNode(args.get(1)))
                throw new MyTownCommandException("mytown.cmd.err.ranks.perm.notexist", args.get(1));

            // Removing permission if everything is alright
            if (rank.removePermission(args.get(1))) {
                getDatasource().saveRank(rank, rank.getTown().getDefaultRank().equals(rank));
                res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.remove", args.get(1), args.get(0)));
            } else
                throw new MyTownCommandException("mytown.cmd.err.ranks.perm.remove.failed", args.get(1));
            return CommandResponse.DONE;
        }
    }


    @Command(
            name = "perm",
            permission = "mytown.cmd.assistant.ranks.perm",
            parentName = "mytown.cmd.everyone.ranks")
    public static CommandResponse ranksPermCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.assistant.ranks.perm.list",
            parentName = "mytown.cmd.assistant.ranks.perm")
    public static CommandResponse ranksPermListCommand(ICommandSender sender, List<String> args) {

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
        return CommandResponse.DONE;
    }

    @Command(
            name = "pass",
            permission = "mytown.cmd.mayor.pass",
            parentName = "mytown.cmd",
            completionKeys = {"residentCompletion"})
    public static CommandResponse passCommand(ICommandSender sender, List<String> args) {
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
        return CommandResponse.DONE;
    }

    public static class Plots {
        @Command(
                name = "limit",
                permission = "mytown.cmd.assistant.plot.limit",
                parentName = "mytown.cmd.everyone.plot")
        public static CommandResponse plotLimitCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "show",
                permission = "mytown.cmd.assistant.plot.limit.show",
                parentName = "mytown.cmd.assistant.plot.limit")
        public static CommandResponse plotLimitShowCommand(ICommandSender sender, List<String> args) {
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.limit", town.getMaxPlots()));
            return CommandResponse.DONE;
        }


        @Command(
                name = "set",
                permission = "mytown.cmd.assistant.plot.limit.set",
                parentName = "mytown.cmd.assistant.plot.limit")
        public static CommandResponse plotLimitSetCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                throw new MyTownWrongUsageException("mytown.cmd.usage.plot.limit.set");
            }
            if (StringUtils.tryParseInt(args.get(0)) || Integer.parseInt(args.get(0)) < 1) {
                throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger");
            }
            int limit = Integer.parseInt(args.get(0));
            Resident res = getDatasource().getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            town.setMaxPlots(limit);
            getDatasource().saveTown(town);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.limit", town.getMaxPlots()));
            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "kick",
            permission = "mytown.cmd.assistant.kick",
            parentName = "mytown.cmd",
            completionKeys = {"residentCompletion"})
    public static CommandResponse kickCommand(ICommandSender sender, List<String> args) {
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
        return CommandResponse.DONE;
    }

    @Command(
            name = "delete",
            permission = "mytown.cmd.mayor.leave.delete",
            parentName = "mytown.cmd.everyone.leave")
    public static CommandResponse leaveDeleteCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        EntityPlayer player = (EntityPlayer) sender;

        if (town.getResidentRank(res).getName().equals(Rank.theMayorDefaultRank)) {
            town.notifyEveryone(getLocal().getLocalization("mytown.notification.town.deleted", town.getName(), res.getPlayerName()));
            int refund = 0;
            for (TownBlock block : town.getBlocks()) {
                refund += block.getPricePaid();
            }

            makeRefund(player, refund);
            getDatasource().deleteTown(town);
        }
        return CommandResponse.DONE;
    }


    @Command(
            name = "rename",
            permission = "mytown.cmd.assistant.rename",
            parentName = "mytown.cmd")
    public static CommandResponse renameCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.rename");

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if (getDatasource().hasTown(args.get(0))) // Is the town name already in use?
            throw new MyTownCommandException("mytown.cmd.err.newtown.nameinuse", args.get(0));

        town.rename(args.get(0));
        getDatasource().saveTown(town);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.renamed"));
        return CommandResponse.DONE;
    }

    @Command(
            name = "withdraw",
            permission = "mytown.cmd.assistant.bank.withdraw",
            parentName = "mytown.cmd.everyone.bank")
    public static CommandResponse bankPayCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            throw new MyTownWrongUsageException("mytown.cmd.usage.bank.withdraw");

        if(!StringUtils.tryParseInt(args.get(0)))
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if(town instanceof AdminTown)
            throw new MyTownCommandException("mytown.cmd.err.adminTown", town.getName());

        int amount = Integer.parseInt(args.get(0));
        if(town.getBankAmount() < amount)
            throw new MyTownCommandException("mytown.cmd.err.bank.withdraw", EconomyProxy.getCurrency(town.getBankAmount()));

        makeRefund(res.getPlayer(), amount);
        getDatasource().updateTownBank(town, town.getBankAmount() - amount);
        return CommandResponse.DONE;
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
