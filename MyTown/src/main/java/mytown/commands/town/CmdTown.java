package mytown.commands.town;

import java.util.ArrayList;
import java.util.List;

import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandHandler;

/**
 * MyTown command
 * @author Joe Goett
 */
@Permission(node = "mytown.cmd.town")
public class CmdTown extends SubCommandHandler {
	
	List<String> aliases = new ArrayList<String>();
	
	public CmdTown() {
		super("town");
		
		// Add commands
		addSubCommand(new NewTown());
		addSubCommand(new Claim());
		addSubCommand(new Map());
		addSubCommand(new ListTown());
		
		//Add aliases
		aliases.add("t");
		
		
	}
	
	@Override
	public boolean canConsoleUse() {
		return true;
	};

	@Override
	public List<?> getCommandAliases() {
		return aliases;
	}
}