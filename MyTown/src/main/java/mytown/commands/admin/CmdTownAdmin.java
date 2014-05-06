package mytown.commands.admin;

import java.util.ArrayList;
import java.util.List;

import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandHandler;

@Permission(node = "mytown.adm.cmd")
public class CmdTownAdmin extends SubCommandHandler {

	List<String> aliases = new ArrayList<String>();

	public CmdTownAdmin(String name) {
		super(name);

		// Subcommands
		addSubCommand(new CmdReload("reload"));
		addSubCommand(new CmdSafeMode("safemode"));
		addSubCommand(new CmdDelete("delete"));
		addSubCommand(new CmdRem("rem"));
		addSubCommand(new CmdAdd("add"));
		
		// Add aliases
		aliases.add("ta");
	}

	@Override
	public List<?> getCommandAliases() {
		return aliases;
	}
}