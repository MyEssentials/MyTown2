package mytown.commands.town;

import java.util.ArrayList;
import java.util.List;

import mytown.commands.town.assistant.CmdClaim;
import mytown.commands.town.assistant.CmdInvite;
import mytown.commands.town.assistant.CmdPerm;
import mytown.commands.town.assistant.CmdPlot;
import mytown.commands.town.assistant.CmdUnclaim;
import mytown.commands.town.everyone.CmdBlocks;
import mytown.commands.town.everyone.CmdInfo;
import mytown.commands.town.everyone.CmdLeave;
import mytown.commands.town.everyone.CmdListTown;
import mytown.commands.town.everyone.CmdMap;
import mytown.commands.town.everyone.CmdNewTown;
import mytown.commands.town.everyone.CmdPlots;
import mytown.commands.town.everyone.CmdRanks;
import mytown.commands.town.everyone.CmdSelect;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import net.minecraft.command.ICommandSender;

/**
 * MyTown command
 * 
 * @author Joe Goett
 */
@Permission("mytown.cmd")
public class CmdTown extends CommandHandler {
	List<String> aliases = new ArrayList<String>();

	public CmdTown(String name) {
		super(name);

		// Add commands
		addSubCommand(new CmdNewTown("new", this));
		addSubCommand(new CmdClaim("claim", this));
		addSubCommand(new CmdMap("map", this));
		addSubCommand(new CmdListTown("list", this));
		addSubCommand(new CmdInvite("invite", this));
		addSubCommand(new CmdInfo("info", this));
		addSubCommand(new CmdSelect("select", this));
		addSubCommand(new CmdRanks("ranks", this));
		addSubCommand(new CmdBlocks("blocks", this));
		addSubCommand(new CmdUnclaim("unclaim", this));
		addSubCommand(new CmdPlot("plot", this));
		addSubCommand(new CmdPlots("plots", this));
		addSubCommand(new CmdPerm("perm", this));
		addSubCommand(new CmdLeave("leave", this));

		// Add Aliases
		aliases.add("t");
	}

	@Override
	public boolean canConsoleUse() {
		return true;
	}

	@Override
	public List<?> getCommandAliases() {
		return aliases; // TODO Add aliases!
	}

	@Override
	public void sendHelp(ICommandSender sender) {
		// TODO Send help to sender
	}
}