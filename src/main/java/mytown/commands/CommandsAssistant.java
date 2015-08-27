package mytown.commands;

import myessentials.entities.ChunkPos;
import myessentials.utils.ChatUtils;
import myessentials.utils.MathUtils;
import myessentials.utils.StringUtils;
import myessentials.utils.WorldUtils;
import mypermissions.api.command.CommandResponse;
import mypermissions.api.command.annotation.Command;
import mytown.config.Config;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.entities.tools.WhitelisterTool;
import mytown.proxies.EconomyProxy;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
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
            parentName = "mytown.cmd",
            syntax = "/town setspawn")
    public static CommandResponse setSpawnCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = MyTownUniverse.instance.getOrMakeResident(player);
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
            parentName = "mytown.cmd",
            syntax = "/town claim [range]")
    public static CommandResponse claimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = MyTownUniverse.instance.getOrMakeResident(player);
        Town town = getTownFromResident(res);

        boolean isFarClaim = false;

        if (args.size() < 1) {
            if (town.townBlocksContainer.size() >= town.getMaxBlocks())
                throw new MyTownCommandException("mytown.cmd.err.town.maxBlocks", 1);
            if (getUniverse().blocks.contains(player.dimension, player.chunkCoordX, player.chunkCoordZ))
                throw new MyTownCommandException("mytown.cmd.err.claim.already");
            if (!checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town)) {
                if (town.townBlocksContainer.getFarClaims() >= town.townBlocksContainer.getMaxFarClaims())
                    throw new MyTownCommandException("mytown.cmd.err.claim.far.notAllowed");
                isFarClaim = true;
            }
            for (int x = player.chunkCoordX - Config.distanceBetweenTowns; x <= player.chunkCoordX + Config.distanceBetweenTowns; x++) {
                for (int z = player.chunkCoordZ - Config.distanceBetweenTowns; z <= player.chunkCoordZ + Config.distanceBetweenTowns; z++) {
                    Town nearbyTown = MyTownUtils.getTownAtPosition(player.dimension, x, z);
                    if (nearbyTown != null && nearbyTown != town && !(Boolean) nearbyTown.flagsContainer.getValue(FlagType.NEARBY))
                        throw new MyTownCommandException("mytown.cmd.err.claim.tooClose", nearbyTown.getName(), Config.distanceBetweenTowns);
                }
            }

            if (isFarClaim && town.townBlocksContainer.getFarClaims() + 1 > town.townBlocksContainer.getMaxFarClaims())
                throw new MyTownCommandException("mytown.cmd.err.claim.far.notAllowed");

            int price = (isFarClaim ? Config.costAmountClaimFar : Config.costAmountClaim) + Config.costAdditionClaim * town.townBlocksContainer.size();

            makeBankPayment(player, town, price);

            TownBlock block = getUniverse().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, isFarClaim, price, town);
            if (block == null)
                throw new MyTownCommandException("mytown.cmd.err.claim.failed");

            getDatasource().saveBlock(block);
            res.sendMessage(getLocal().getLocalization("mytown.notification.block.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));
        } else {
            if (!StringUtils.tryParseInt(args.get(0)))
                throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));

            int radius = Integer.parseInt(args.get(0));
            List<ChunkPos> chunks = WorldUtils.getChunksInBox(player.dimension, (int) (player.posX - radius * 16), (int) (player.posZ - radius * 16), (int) (player.posX + radius * 16), (int) (player.posZ + radius * 16));
            isFarClaim = true;

            for (Iterator<ChunkPos> it = chunks.iterator(); it.hasNext(); ) {
                ChunkPos chunk = it.next();
                if (checkNearby(player.dimension, chunk.getX(), chunk.getZ(), town)) {
                    isFarClaim = false;
                }
                if (getUniverse().blocks.contains(player.dimension, chunk.getX(), chunk.getZ()))
                    it.remove();

                for (int x = chunk.getX() - Config.distanceBetweenTowns; x <= chunk.getX() + Config.distanceBetweenTowns; x++) {
                    for (int z = chunk.getZ() - Config.distanceBetweenTowns; z <= chunk.getZ() + Config.distanceBetweenTowns; z++) {
                        Town nearbyTown = MyTownUtils.getTownAtPosition(player.dimension, x, z);
                        if (nearbyTown != null && nearbyTown != town && !(Boolean) nearbyTown.flagsContainer.getValue(FlagType.NEARBY))
                            throw new MyTownCommandException("mytown.cmd.err.claim.tooClose", nearbyTown.getName(), Config.distanceBetweenTowns);
                    }
                }
            }

            if (town.townBlocksContainer.size() + chunks.size() > town.getMaxBlocks())
                throw new MyTownCommandException("mytown.cmd.err.town.maxBlocks", chunks.size());

            if (isFarClaim && town.townBlocksContainer.getFarClaims() + 1 > town.townBlocksContainer.getMaxFarClaims())
                throw new MyTownCommandException("mytown.cmd.err.claim.far.notAllowed");

            makeBankPayment(player, town, (isFarClaim ? Config.costAmountClaimFar + Config.costAmountClaim * (chunks.size() - 1) : Config.costAmountClaim * chunks.size())
                    + MathUtils.sumFromNtoM(town.townBlocksContainer.size(), town.townBlocksContainer.size() + chunks.size() - 1) * Config.costAdditionClaim);

            for (ChunkPos chunk : chunks) {
                int price = (isFarClaim ? Config.costAmountClaimFar : Config.costAmountClaim) + Config.costAdditionClaim * town.townBlocksContainer.size();
                TownBlock block = getUniverse().newBlock(player.dimension, chunk.getX(), chunk.getZ(), isFarClaim, price, town);
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
            parentName = "mytown.cmd",
            syntax = "/town unclaim")
    public static CommandResponse unclaimCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        TownBlock block = getBlockAtResident(res);
        Town town = getTownFromResident(res);

        if (town != block.getTown())
            throw new MyTownCommandException("mytown.cmd.err.unclaim.notInTown");
        if (block.isPointIn(town.getSpawn().getDim(), town.getSpawn().getX(), town.getSpawn().getZ()))
            throw new MyTownCommandException("mytown.cmd.err.unclaim.spawnPoint");
        if (!checkNearby(block.getDim(), block.getX(), block.getZ(), town) && town.townBlocksContainer.size() <= 1) {
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
            syntax = "/town invite <resident>",
            completionKeys = {"residentCompletion"})
    public static CommandResponse inviteCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;
        Resident target = getResidentFromName(args.get(0));
        if (town.residentsMap.contains(args.get(0)))
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
            syntax = "/town perm set <flag> <value>",
            completionKeys = "flagCompletion")
    public static CommandResponse permSetCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        Flag flag = getFlagFromName(town.flagsContainer, args.get(0));

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
            syntax = "/town perm whitelist")
    public static CommandResponse permWhitelistCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        res.toolContainer.set(new WhitelisterTool(res));
        return CommandResponse.DONE;
    }

    @Command(
            name = "promote",
            permission = "mytown.cmd.assistant.promote",
            parentName = "mytown.cmd",
            syntax = "/town promote <resident> <rank>",
            completionKeys = {"residentCompletion", "rankCompletion"})
    public static CommandResponse promoteCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 2)
            return CommandResponse.SEND_SYNTAX;
        Resident resSender = MyTownUniverse.instance.getOrMakeResident(sender);
        Resident resTarget = getResidentFromName(args.get(0));
        Town town = getTownFromResident(resSender);

        if (!resTarget.townsContainer.contains(town))
            throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", args.get(0), town.getName());

        Rank mayorRank = town.ranksContainer.getMayorRank();
        if (args.get(1).equalsIgnoreCase(mayorRank.getName()))
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
                syntax = "/town ranks add <name> [templateRank]",
                completionKeys = {"-", "ranksCompletion"})
        public static CommandResponse ranksAddCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                return CommandResponse.SEND_SYNTAX;

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            if (town.ranksContainer.contains(args.get(0)))
                throw new MyTownCommandException("mytown.cmd.err.ranks.add.already", args.get(0));

            Rank rank = new Rank(args.get(0), town, Rank.Type.REGULAR);
            if(args.size() == 2) {
                Rank template = getRankFromTown(town, args.get(1));
                rank.permissionsContainer.addAll(template.permissionsContainer);
            }

            getDatasource().saveRank(rank);

            res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.add", args.get(0), town.getName()));
            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "mytown.cmd.assistant.ranks.remove",
                parentName = "mytown.cmd.everyone.ranks",
                syntax = "/town ranks remove <rank>",
                completionKeys = {"rankCompletion"})
        public static CommandResponse ranksRemoveCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                return CommandResponse.SEND_SYNTAX;

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            Rank rank = getRankFromTown(town, args.get(0));

            if (rank.getType().unique) {
                throw new MyTownCommandException("mytown.cmd.err.ranks.cantDelete");
            }

            for(Rank residentRank : town.residentsMap.values()) {
                if(residentRank == rank) {
                    throw new MyTownCommandException("mytown.cmd.err.ranks.assigned");
                }
            }

            getDatasource().deleteRank(rank);
            res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.rem", args.get(0), town.getName()));

            return CommandResponse.DONE;
        }

        @Command(
                name = "set",
                permission = "mytown.cmd.assistant.ranks.set",
                parentName = "mytown.cmd.everyone.ranks",
                syntax = "/town ranks set <rank> <type>",
                completionKeys = {"rankCompletion"})
        public static CommandResponse ranksSetCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            Rank rank = getRankFromTown(town, args.get(0));
            Rank.Type type = getRankTypeFromString(args.get(1));

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
                if(rank.getType().unique) {
                    throw new MyTownCommandException("mytown.cmd.err.ranks.set.unique", rank.getName());
                }

                rank.setType(type);

                getDatasource().saveRank(rank);
            }

            res.sendMessage(getLocal().getLocalization("mytown.notification.ranks.set.successful", rank.getName(), type.toString()));
            return CommandResponse.DONE;
        }


        @Command(
                name = "add",
                permission = "mytown.cmd.assistant.ranks.perm.add",
                parentName = "mytown.cmd.assistant.ranks.perm",
                syntax = "/town ranks perm add <rank> <perm>",
                completionKeys = {"rankCompletion"})
        public static CommandResponse ranksPermAddCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2)
                return CommandResponse.SEND_SYNTAX;

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            Rank rank = getRankFromTown(town, args.get(0));

            getDatasource().saveRankPermission(rank, args.get(1));
            res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.add", args.get(1), args.get(0)));

            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "mytown.cmd.assistant.ranks.perm.remove",
                parentName = "mytown.cmd.assistant.ranks.perm",
                syntax = "/town ranks perm remove <rank> <perm>",
                completionKeys = {"rankCompletion"})
        public static CommandResponse ranksPermRemoveCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2)
                return CommandResponse.SEND_SYNTAX;

            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            Rank rank = getRankFromTown(town, args.get(0));

            getDatasource().deleteRankPermission(rank, args.get(1));
            res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.remove", args.get(1), args.get(0)));

            return CommandResponse.DONE;
        }

        @Command(
                name = "reset",
                permission = "mytown.cmd.assistant.ranks.reset",
                parentName = "mytown.cmd.everyone.ranks",
                syntax = "/town ranks reset")
        public static CommandResponse ranksResetCommand(ICommandSender sender, List<String> args) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);

            for(Rank defaultRank : Rank.defaultRanks) {
                Rank rank = town.ranksContainer.get(defaultRank.getName());

                rank.permissionsContainer.clear();
                rank.permissionsContainer.addAll(defaultRank.permissionsContainer);
                rank.setType(defaultRank.getType());

                getDatasource().saveRank(rank);
            }

            for(int i = 0; i < town.ranksContainer.size(); i++) {
                Rank rank = town.ranksContainer.get(i);
                if(!Rank.defaultRanks.contains(rank.getName())) {
                    getDatasource().deleteRank(rank);
                    i--;
                }
            }

            res.sendMessage(getLocal().getLocalization("mytown.notification.ranks.reset"));

            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "perm",
            permission = "mytown.cmd.assistant.ranks.perm",
            parentName = "mytown.cmd.everyone.ranks",
            syntax = "/town ranks perm <command>")
    public static CommandResponse ranksPermCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "mytown.cmd.assistant.ranks.perm.list",
            parentName = "mytown.cmd.assistant.ranks.perm",
            syntax = "/town ranks perm list [rank]")
    public static CommandResponse ranksPermListCommand(ICommandSender sender, List<String> args) {
        Rank rank;
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        if (args.isEmpty()) {
            rank = getRankFromResident(res);
        } else {
            rank = getRankFromTown(town, args.get(0));
        }

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.ranks.perm.list", rank.getName(), town.getName(), rank.permissionsContainer.toString()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "pass",
            permission = "mytown.cmd.mayor.pass",
            parentName = "mytown.cmd",
            syntax = "/town pass <resident>",
            completionKeys = {"residentCompletion"})
    public static CommandResponse passCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Resident target = getResidentFromName(args.get(0));

        if (res == target) {
            throw new MyTownCommandException("mytown.cmd.err.resident.same");
        }

        Town town = getTownFromResident(res);

        if (!town.residentsMap.containsKey(target)) {
            throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", target.getPlayerName(), town.getName());
        }
        if (town.residentsMap.get(res).getType() == Rank.Type.MAYOR) {
            getDatasource().updateResidentToTownLink(target, town, town.ranksContainer.getMayorRank());
            target.sendMessage(getLocal().getLocalization("mytown.notification.town.mayorShip.passed"));
            getDatasource().updateResidentToTownLink(res, town, town.ranksContainer.getDefaultRank());
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
                parentName = "mytown.cmd.everyone.plot",
                syntax = "/town plot limit <command>")
        public static CommandResponse plotLimitCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "show",
                permission = "mytown.cmd.assistant.plot.limit.show",
                parentName = "mytown.cmd.assistant.plot.limit",
                syntax = "/town plot limit show")
        public static CommandResponse plotLimitShowCommand(ICommandSender sender, List<String> args) {
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.limit", town.plotsContainer.getMaxPlots()));
            return CommandResponse.DONE;
        }


        @Command(
                name = "set",
                permission = "mytown.cmd.assistant.plot.limit.set",
                parentName = "mytown.cmd.assistant.plot.limit",
                syntax = "/town plot limit set <limit>")
        public static CommandResponse plotLimitSetCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1)
                return CommandResponse.SEND_SYNTAX;

            if (!StringUtils.tryParseInt(args.get(0)) || Integer.parseInt(args.get(0)) < 1) {
                throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));
            }
            int limit = Integer.parseInt(args.get(0));
            Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
            Town town = getTownFromResident(res);
            town.plotsContainer.setMaxPlots(limit);
            getDatasource().saveTown(town);
            res.sendMessage(getLocal().getLocalization("mytown.notification.plot.limit.set", town.plotsContainer.getMaxPlots()));
            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "kick",
            permission = "mytown.cmd.assistant.kick",
            parentName = "mytown.cmd",
            syntax = "/town kick <resident>",
            completionKeys = {"residentCompletion"})
    public static CommandResponse kickCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Resident target = getResidentFromName(args.get(0));
        Town town = getTownFromResident(res);
        if (!target.townsContainer.contains(town)) {
            throw new MyTownCommandException("mytown.cmd.err.resident.notsametown", args.get(0), town.getName());
        }
        if (target == res) {
            throw new MyTownCommandException("mytown.cmd.err.kick.self");
        }

        if(town.residentsMap.get(target) == town.ranksContainer.getMayorRank()) {
            throw new MyTownCommandException("mytown.cmd.err.kick.mayor");
        }

        getDatasource().unlinkResidentFromTown(target, town);
        target.sendMessage(getLocal().getLocalization("mytown.notification.town.kicked", town.getName()));
        town.notifyEveryone(getLocal().getLocalization("mytown.notification.town.left", target.getPlayerName(), town.getName()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "delete",
            permission = "mytown.cmd.mayor.leave.delete",
            parentName = "mytown.cmd.everyone.leave",
            syntax = "/town leave delete")
    public static CommandResponse leaveDeleteCommand(ICommandSender sender, List<String> args) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);
        EntityPlayer player = (EntityPlayer) sender;

        if (town.residentsMap.get(res).getType() == Rank.Type.MAYOR) {
            town.notifyEveryone(getLocal().getLocalization("mytown.notification.town.deleted", town.getName(), res.getPlayerName()));
            int refund = 0;
            for (TownBlock block : town.townBlocksContainer) {
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
            parentName = "mytown.cmd",
            syntax = "/town rename <name>")
    public static CommandResponse renameCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if (getUniverse().towns.contains(args.get(0))) // Is the town name already in use?
            throw new MyTownCommandException("mytown.cmd.err.newtown.nameinuse", args.get(0));

        town.rename(args.get(0));
        getDatasource().saveTown(town);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.renamed"));
        return CommandResponse.DONE;
    }

    @Command(
            name = "withdraw",
            permission = "mytown.cmd.assistant.bank.withdraw",
            parentName = "mytown.cmd.everyone.bank",
            syntax = "/town bank withdraw <amount>")
    public static CommandResponse bankWithdrawCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1)
            return CommandResponse.SEND_SYNTAX;

        if(!StringUtils.tryParseInt(args.get(0)))
            throw new MyTownCommandException("mytown.cmd.err.notPositiveInteger", args.get(0));

        Resident res = MyTownUniverse.instance.getOrMakeResident(sender);
        Town town = getTownFromResident(res);

        if(town instanceof AdminTown)
            throw new MyTownCommandException("mytown.cmd.err.adminTown", town.getName());

        int amount = Integer.parseInt(args.get(0));
        if(town.bank.getAmount() < amount)
            throw new MyTownCommandException("mytown.cmd.err.bank.withdraw", EconomyProxy.getCurrency(town.bank.getAmount()));

        makeRefund(res.getPlayer(), amount);
        town.bank.addAmount(-amount);
        getDatasource().saveTownBank(town.bank);
        return CommandResponse.DONE;
    }

    // Temporary here, might integrate in the methods
    protected static boolean checkNearby(int dim, int x, int z, Town town) {
        int[] dx = {1, 0, -1, 0};
        int[] dz = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++) {
            TownBlock block = getUniverse().blocks.get(dim, x + dx[i], z + dz[i]);
            if (block != null && block.getTown() == town) {
                return true;
            }
        }
        return false;
    }
}
