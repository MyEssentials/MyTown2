package mytown.commands.town;

import java.util.List;

import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandHandler;

/**
 * MyTown command
 * @author Joe Goett
 */
@Permission(node = "mytown.cmd.town")
public class CmdTown extends SubCommandHandler {
	public CmdTown() {
		super("town");
		
		// Add commands
		addSubCommand(new NewTown());
		addSubCommand(new Claim());
		addSubCommand(new Map());
	}

	@Override
	public List<?> getCommandAliases() {
		return null; // TODO Add aliases!
	}
}