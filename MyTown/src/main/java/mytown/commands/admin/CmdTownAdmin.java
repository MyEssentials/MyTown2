package mytown.commands.admin;

import java.util.ArrayList;
import java.util.List;

import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;

@Permission(node = "mytown.adm.cmd")
public class CmdTownAdmin extends CommandHandler {

	List<String> aliases = new ArrayList<String>();

	public CmdTownAdmin(String name) {
		super(name, null);

		// Subcommands
		addSubCommand(new CmdReload("reload", this));
		addSubCommand(new CmdSafeMode("safemode", this));
		addSubCommand(new CmdDelete("delete", this));
		addSubCommand(new CmdRem("rem", this));
		addSubCommand(new CmdAdd("add", this));
		
		// Add aliases
		aliases.add("ta");
	}

	@Override
	public List<?> getCommandAliases() {
		return aliases;
	}
}