package mytown.commands.town;

import java.util.ArrayList;
import java.util.List;

import mytown.commands.town.assistant.CmdClaim;
import mytown.commands.town.assistant.CmdInvite;
import mytown.commands.town.everyone.CmdListTown;
import mytown.commands.town.everyone.CmdMap;
import mytown.commands.town.nonresident.CmdNewTown;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandHandler;

/**
 * MyTown command
 * 
 * @author Joe Goett
 */
@Permission(node = "mytown.cmd.town")
public class CmdTown extends SubCommandHandler {
	List<String> aliases = new ArrayList<String>();

	public CmdTown(String name) {
		super(name);

		// Add commands
		addSubCommand(new CmdNewTown("new"));
		addSubCommand(new CmdClaim("claim"));
		addSubCommand(new CmdMap("map"));
		addSubCommand(new CmdListTown("list"));
		addSubCommand(new CmdInvite("invite"));

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