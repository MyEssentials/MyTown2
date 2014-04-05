package mytown.commands.admin;

import java.util.List;

import mytown.core.utils.command.sub.SubCommandHandler;

public class CmdTownAdmin extends SubCommandHandler {
	public CmdTownAdmin() {
		super("townadmin");
	}

	@Override
	public List<?> getCommandAliases() {
		return null;  // TODO Add Aliases!
	}
}