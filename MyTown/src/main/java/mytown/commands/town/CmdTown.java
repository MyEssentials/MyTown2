package mytown.commands.town;

import java.util.ArrayList;
import java.util.List;

import mytown.commands.town.assistant.CmdClaim;
import mytown.commands.town.assistant.CmdInvite;
import mytown.commands.town.everyone.CmdInfo;
import mytown.commands.town.everyone.CmdListTown;
import mytown.commands.town.everyone.CmdMap;
import mytown.commands.town.everyone.CmdNewTown;
import mytown.commands.town.everyone.CmdRanks;
import mytown.commands.town.everyone.CmdSelect;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;

/**
 * MyTown command
 * 
 * @author Joe Goett
 */
@Permission(node = "mytown.cmd")
public class CmdTown extends CommandHandler {
	List<String> aliases = new ArrayList<String>();

	public CmdTown(String name) {
		super(name, null);

		// Add commands
		addSubCommand(new CmdNewTown("new", this));
		addSubCommand(new CmdClaim("claim", this));
		addSubCommand(new CmdMap("map", this));
		addSubCommand(new CmdListTown("list", this));
		addSubCommand(new CmdInvite("invite", this));
		addSubCommand(new CmdInfo("info", this));
		addSubCommand(new CmdSelect("select", this));
		addSubCommand(new CmdRanks("ranks", this));
		
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
}