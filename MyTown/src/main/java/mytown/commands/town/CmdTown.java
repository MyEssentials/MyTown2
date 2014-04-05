package mytown.commands.town;

import java.util.List;

import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandHandler;

@Permission(node = "mytown.cmd.town")
public class CmdTown extends SubCommandHandler {
	public CmdTown() {
		super("town");
	}

	@Override
	public List<?> getCommandAliases() {
		return null; // TODO Add aliases!
	}
}