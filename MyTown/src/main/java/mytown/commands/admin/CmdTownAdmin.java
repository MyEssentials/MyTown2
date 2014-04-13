package mytown.commands.admin;

import java.util.List;

import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandHandler;

@Permission(node = "mytown.adm.cmd")
public class CmdTownAdmin extends SubCommandHandler {
	public CmdTownAdmin() {
		super("townadmin");
		
		// Subcommands
		addSubCommand(new Reload());
	}

	@Override
	public List<?> getCommandAliases() {
		return null;  // TODO Add Aliases!
	}
}