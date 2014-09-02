package mytown.commands;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.Command;
import mytown.core.utils.command.CommandNode;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.handlers.VisualsTickHandler;
import mytown.proxies.DatasourceProxy;
import mytown.util.Constants;
import mytown.util.Formatter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 8/28/2014.
 * Process methods for all commands that can be used by everyone
 */
public class CommandsEveryone extends Commands{

    @Command(
            name = "town",
            permission = "mytown.cmd",
            alias = {"t"})
    public static void townCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd");
    }

    @CommandNode(
            name = "info",
            permission = "mytown.cmd.everyone.info",
            parentName = "mytown.cmd")
    public static void infoCommand(ICommandSender sender, List<String> args) {
        List<Town> towns = new ArrayList<Town>();

        Resident res = getDatasource().getOrMakeResident(sender);
        if (res == null)
            throw new CommandException("Failed to get/make Resident"); // TODO Localize
        if (args.size() < 1) {
            if (res.getSelectedTown() != null) {
                towns.add(res.getSelectedTown());
            } else {
                throw new CommandException(getLocal().getLocalization("mytown.cmd.err.info.notpart"));
            }
        } else {
            if (args.get(0).equals("@a")) {
                towns = new ArrayList<Town>(getUniverse().getTownsMap().values());
                // TODO Sort
            } else if (getDatasource().hasTown(args.get(0))) {
                towns.add(getUniverse().getTownsMap().get(args.get(0)));
            } else {
                throw new CommandException(getLocal().getLocalization("mytown.cmd.err.town.notexist", args.get(0)));
            }
        }

        for (Town town : towns) {

            res.sendMessage(town.getTownInfo());
        }
    }

    @CommandNode(
            name = "leave",
            permission = "mytown.cmd.everyone.leave",
            parentName = "mytown.cmd")
    public static void leaveCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res == null)
            throw new CommandException("Resident is null"); // TODO Localize!

        Town town = res.getSelectedTown();
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));

        getDatasource().unlinkResidentFromTown(res, town);

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.left.self", town.getName()));

        for (Resident r : town.getResidents()) {
            r.sendMessage(getLocal().getLocalization("mytown.notification.town.left", res.getPlayerName(), town.getName()));
        }
    }

    @CommandNode(
            name = "new",
            permission = "mytown.cmd.everyone.new",
            parentName = "mytown.cmd")
    public static void newTownCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.newtown"));
        if (getDatasource().hasTown(args.get(0))) // Is the town name already in use?
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.newtown.nameinuse", args.get(0)));
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ)) // Is the Block already claimed?   TODO Bit-shift the coords?
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.newtown.positionError"));

        Resident res = getDatasource().getOrMakeResident(player); // Attempt to get or make the Resident
        if (res == null)
            throw new CommandException("Failed to get or save resident"); // TODO Localize!

        Town town = getDatasource().newTown(args.get(0), res); // Attempt to create the Town
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.newtown.failed"));

        /*
        // Moved to town constructor

        Rank onCreationDefaultRank = null;

        // Saving town to database
        if (!getDatasource().saveTown(town))
            throw new CommandException("Failed to save Town"); // TODO Localize!

        // Saving all ranks to database and town
        for(String rankName : Rank.defaultRanks.keySet()) {
            Rank rank = new Rank(rankName, Rank.defaultRanks.get(rankName), town);
            getDatasource().saveRank(rank);
            if(rankName.equals(Rank.theDefaultRank)) {
                town.setDefaultRank(rank);
            }
            if(rankName.equals(Rank.theMayorDefaultRank)) {
                onCreationDefaultRank = rank;
            }
        }

        //Claiming first block
        Block block = getDatasource().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, town);

        // Saving block to db and town
        getDatasource().saveBlock(block);

        // Linking resident to town
        if(!getDatasource().linkResidentToTown(res, town, onCreationDefaultRank))
            MyTown.instance.log.error("Problem linking resident " + res.getPlayerName() + " to town " + town.getName());
        */


        // TODO Town Flags
        // TODO Set Town spawn

        res.sendMessage(getLocal().getLocalization("mytown.notification.town.created", town.getName()));
    }

    @CommandNode(
            name = "spawn",
            permission = "mytown.cmd.everyone.spawn",
            parentName = "mytown.cmd")
    public static void spawnCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res == null)
            throw new CommandException("Failed to get or make Resident"); // TODO Localize!
        Town town = null;

        if (args.size() == 0) {
            town = res.getSelectedTown();
        } else {
            town = getUniverse().getTownsMap().get(args.get(0));
        }

        if (town == null) {
            throw new CommandException("Town doesn't exist!"); // TODO localize!
        } else { // TODO Check if the Resident is allowed to go to spawn
            if (!town.hasSpawn())
                throw new CommandException(getLocal().getLocalization("mytown.cmd.err.spawn.notexist", town.getName()));
            town.sendToSpawn(res);
        }
    }

    @CommandNode(
            name = "select",
            permission = "mytown.cmd.everyone.select",
            parentName = "mytown.cmd")
    public static void selectCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.select"));
        if (!getDatasource().hasTown(args.get(0)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.town.notexist", args.get(0)));
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res == null)
            throw new CommandException("Failed to get or make Resident"); // TODO Localize
        Town town = getUniverse().getTownsMap().get(args.get(0));
        if (!town.hasResident(res))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.select.notpart", args.get(0)));
        res.selectTown(town);
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.select", args.get(0)));
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.blocks.list",
            parentName = "mytown.cmd.assistant.blocks")
    public static void blocksListCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town;
        if (args.size() == 0)
            if (res.getSelectedTown() == null)
                throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
            else {
                town = res.getSelectedTown();
            }
        else if (!DatasourceProxy.getDatasource().hasTown(args.get(0)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.town.notexist", args.get(0)));
        else {
            town = res.getSelectedTown();
        }

        String s = null;
        for(Block block : town.getBlocks()) {
            if(s == null)
                s = block.toString();
            else
                s += "\n" + block.toString();
        }

        res.sendMessage(getLocal().getLocalization("mytown.notification.block.list", town.getName(), "\n" + s));
    }

    @CommandNode(
            name = "map",
            permission = "mytown.cmd.everyone.map",
            parentName = "mytown.cmd")
    public static void mapCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (args.size() == 0) {
            Formatter.sendMap(res);
        } else {
            res.setMapOn(ChatUtils.equalsOn(args.get(0)));
        }
    }

    @CommandNode(
            name = "accept",
            permission = "mytown.cmd.everyone.accept",
            parentName = "mytown.cmd")
    public static void acceptCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res.getInvites().isEmpty())
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.invite.noinvitations"));
        if (getUniverse().getTownsMap().get(args.get(0)) == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.town.notexist", args.get(0)));
        String townName;
        if (args.size() == 0) {
            townName = res.getInvites().get(0).getName();
        } else {
            townName = args.get(0);
        }
        if (!res.getInvites().contains(getUniverse().getTownsMap().get(townName)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.usage.invite"));
        Town t = res.getInvite(townName);
        res.removeInvite(t);

        // Notify everyone
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.invited.accept", townName));
        t.notifyResidentJoin(res);

        // Link Resident to Town
        t.addResident(res);
        res.addTown(t);
        getDatasource().saveTown(t);
    }

    @CommandNode(
            name = "refuse",
            permission = "mytown.cmd.everyone.refuse",
            parentName = "mytown.cmd")
    public static void refuseCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res.getInvites().size() == 0)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.invite.noinvitations"));
        if (args.size() == 0)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.invite.accept"));
        if (getUniverse().getTownsMap().get(args.get(0)) != null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.town.notexist", args.get(0)));
        String townName;
        if (args.size() == 0) {
            townName = res.getInvites().get(0).getName();
        } else {
            townName = args.get(0);
        }
        if (!res.getInvites().contains(getUniverse().getTownsMap().get(townName)))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.invite.accept"));
        res.sendMessage(getLocal().getLocalization("mytown.notification.town.invited.refuse", townName));
        res.removeInvite(townName);
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.perm.town.list",
            parentName = "mytown.cmd.assistant.perm.town")
    public static void listPermCommand(ICommandSender sender, List<String> args) {

        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = res.getSelectedTown();

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
            permission = "mytown.cmd.everyone.perm.plot.set",
            parentName = "mytown.cmd.everyone.perm.plot")
    public static void permSetPlotCommand(ICommandSender sender, List<String> args) {

        if (args.size() < 2)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.err.perm.set.usage"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Plot plot = res.getPlotAtPlayerPosition();
        if(plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.notInPlot"));
        if(!plot.hasOwner(res))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.perm.set.noPermission"));

        Flag flag = plot.getFlag(args.get(0));

        if (flag == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.flagNotExists", args.get(0)));

        if (flag.setValueFromString(args.get(1))) {
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.perm.set.success", args.get(0), args.get(1));
        } else
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.perm.valueNotValid", args.get(1)));

        getDatasource().saveFlag(flag, plot);
    }

    @CommandNode(
            name = "plot",
            permission = "mytown.cmd.everyone.perm.plot",
            parentName = "mytown.cmd.assistant.perm")
    public static void permPlotCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.perm.plot");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.perm.plot.list",
            parentName = "mytown.cmd.everyone.perm.plot")
    public static void permPlotListCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = res.getSelectedTown();
        Plot plot = town.getPlotAtCoords(player.dimension, (int)player.posX, (int)player.posY, (int)player.posZ);
        if(plot == null) {
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.notInPlot"));
        }
        String formattedFlagList = null;
        for (Flag flag : plot.getFlags()) {
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
            name = "plot",
            permission = "mytown.cmd.everyone.plot",
            parentName = "mytown.cmd")
    public static void plotCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.plot");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.plot.list",
            parentName = "mytown.cmd")
    public static void plotListCommand(ICommandSender sender, List<String> args) {
        //TODO: check if this works
        Resident resident = getDatasource().getOrMakeResident(sender);
        Town town = resident.getSelectedTown();
        if (town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));

        String formattedPlotsList = "";
        for (Plot plot : town.getPlots()) {
            formattedPlotsList += "\n";
            formattedPlotsList += plot;
        }
        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.plots", town, formattedPlotsList);
    }

    @CommandNode(
            name = "make",
            permission = "mytown.cmd.everyone.plot.make",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotMakeCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        String plotName = "NoName";

        if (args.size() > 0) {
            plotName = args.get(0);
        }

        // This handles all the db stuff... Not sure if I should change :/
        boolean result = res.makePlotFromSelection(plotName);

        if (result) {
            ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.plot.created");
        } else
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.failed"));

    }

    @CommandNode(
            name = "rename",
            permission = "mytown.cmd.everyone.plot.rename",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotRenameCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.plot.rename"));

        EntityPlayer player = (EntityPlayer) sender;
        Town town = getUniverse().getResidentsMap().get(sender.getCommandSenderName()).getSelectedTown();

        Plot plot = town.getPlotAtCoords(player.dimension, (int) player.posX, (int) player.posY, (int) player.posZ);
        if (plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.rename.notexist"));

        plot.setName(args.get(0));
        getDatasource().savePlot(plot);


        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.renamed"); // Maybe give more info about the plot?

    }

    @CommandNode(
            name = "select",
            permission = "mytown.cmd.everyone.plot.select",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotSelectCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        MyTown.instance.log.info("Select for plots command here");

        if(args.size() == 0) {
            ItemStack selectionTool = new ItemStack(Items.wooden_hoe);
            selectionTool.setStackDisplayName(Constants.EDIT_TOOL_NAME);
            NBTTagList lore = new NBTTagList();
            lore.appendTag(new NBTTagString(EnumChatFormatting.DARK_AQUA + "Select 2 blocks to make a plot."));
            lore.appendTag(new NBTTagString(EnumChatFormatting.DARK_AQUA + "Uses: 1"));
            selectionTool.getTagCompound().getCompoundTag("display").setTag("Lore", lore);

            EntityPlayer player = (EntityPlayer) sender;

            boolean ok = !player.inventory.hasItemStack(selectionTool);
            boolean result = false;
            if (ok) {
                result = player.inventory.addItemStackToInventory(selectionTool);
            }
            if (result) {
                ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.plot.start");
            } else if (ok) {
                ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.cmd.err.plot.start.failed");
            }
        } else {
            callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.plot.select");
        }
    }
    @CommandNode(
            name = "expandVert",
            permission = "mytown.cmd.everyone.plot.select.expand",
            parentName = "mytown.cmd.everyone.plot.select")
    public static void plotSelectExpandCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);

        if (!(res.isFirstPlotSelectionActive() && res.isSecondPlotSelectionActive()))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.notSelected"));

        res.expandSelectionVert();

        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.plot.expanded");

    }

    @CommandNode(
            name = "reset",
            permission = "mytown.cmd.everyone.plot.select.reset",
            parentName = "mytown.cmd.everyone.plot.select")
    public static void plotSelectResetCommand(ICommandSender sender, List<String> args) {

        Resident res = getDatasource().getOrMakeResident(sender);
        res.resetSelection();

        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.plot.selectionReset");
    }

    @CommandNode(
            name = "show",
            permission = "mytown.cmd.everyone.plot.show",
            parentName = "mytown.cmd.everyone.plot")
        public static void plotShowCommand(ICommandSender sender, List<String> args) {
        Town town = getDatasource().getOrMakeResident(sender).getSelectedTown();
        for (Plot plot : town.getPlots()) {
            VisualsTickHandler.instance.markPlotBorders(plot);
        }
        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.showing");
    }

    @CommandNode(
            name = "vanish",
            permission = "mytown.cmd.everyone.plot.vanish",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotVanishCommand(ICommandSender sender, List<String> args) {

        Town town = getDatasource().getOrMakeResident(sender).getSelectedTown();
        for (Plot plot : town.getPlots()) {
            VisualsTickHandler.instance.unmarkPlotBorders(plot);
        }
        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.plot.vanished");
    }

    @CommandNode(
            name = "add",
            permission = "mytown.cmd.everyone.plot.add",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotAddCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.plot.add");
    }

    @CommandNode(
            name = "owner",
            permission = "mytown.cmd.everyone.plot.add.owner",
            parentName = "mytown.cmd.everyone.plot.add")
    public static void plotAddOwnerCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        if(args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.plot.add"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident target = getDatasource().getOrMakeResident(args.get(0));
        if(target == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.resident.notexist", args.get(0)));
        Town town = res.getSelectedTown();
        if(town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        Plot plot = town.getPlotAtResident(res);
        if(plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.notInPlot"));

        getDatasource().linkResidentToPlot(target, plot, true);

        res.sendMessage(getLocal().getLocalization("mytown.notification.plot.owner.sender.added"));
        target.sendMessage(getLocal().getLocalization("mytown.notification.plot.owner.target.added"));
    }

    @CommandNode(
            name = "member",
            permission = "mytown.cmd.everyone.plot.add.member",
            parentName = "mytown.cmd.everyone.plot.add")
    public static void plotAddMemberCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        if(args.size() < 1)
            throw new WrongUsageException(getLocal().getLocalization("mytown.cmd.usage.plot.add"));
        Resident res = getDatasource().getOrMakeResident(sender);
        Resident target = getDatasource().getOrMakeResident(args.get(0));
        if(target == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.resident.notexist", args.get(0)));
        Town town = res.getSelectedTown();
        if(town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        Plot plot = town.getPlotAtResident(res);
        if(plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.notInPlot"));

        getDatasource().linkResidentToPlot(target, plot, false);

        res.sendMessage(getLocal().getLocalization("mytown.notification.plot.member.sender.added"));
        target.sendMessage(getLocal().getLocalization("mytown.notification.plot.member.target.added"));
    }

    @CommandNode(
            name = "info",
            permission = "mytown.cmd.everyone.plot.info",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotInfoCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = res.getSelectedTown();
        if(town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        Plot plot = town.getPlotAtResident(res);
        if(plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.notInPlot"));

        res.sendMessage(plot.getPlotInfo());
    }
    @CommandNode(
            name = "delete",
            permission = "mytown.cmd.everyone.plot.delete",
            parentName = "mytown.cmd.everyone.plot")
    public static void plotDeleteCommand(ICommandSender sender, List<String> args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        Town town = res.getSelectedTown();
        if(town == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        Plot plot = town.getPlotAtResident(res);
        if(plot == null)
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.notInPlot"));
        if(!plot.hasOwner(res))
            throw new CommandException(getLocal().getLocalization("mytown.cmd.err.plot.notOwner"));

        getDatasource().deletePlot(plot);
        res.sendMessage(getLocal().getLocalization("mytown.notification.plot.deleted"));
    }
    @CommandNode(
            name = "ranks",
            permission = "mytown.cmd.everyone.ranks",
            parentName = "mytown.cmd")
    public static void ranksCommand(ICommandSender sender, List<String> args, List<String> subCommands) {
        callSubFunctions(sender, args, subCommands, "mytown.cmd.everyone.ranks");
    }

    @CommandNode(
            name = "list",
            permission = "mytown.cmd.everyone.ranks.list",
            parentName = "mytown.cmd.everyone.ranks")
    public static void listRanksCommand(ICommandSender sender, List<String> args) {
        Town temp = null;
        if (args.size() < 1) {
            temp = getDatasource().getOrMakeResident(sender).getSelectedTown();
            if (temp == null)
                throw new CommandException(getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        }
        if (args.size() >= 1) {
            temp = getUniverse().getTownsMap().get(args.get(0));
            if (temp == null)
                throw new CommandException(getLocal().getLocalization("mytown.cmd.err.town.notexist", args.get(0)));
        }

        ChatUtils.sendLocalizedChat(sender, getLocal(), "mytown.notification.town.ranks", Formatter.formatRanksToString(temp.getRanks()));
    }










}
