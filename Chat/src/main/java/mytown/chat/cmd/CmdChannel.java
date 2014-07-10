package mytown.chat.cmd;

import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;

@Permission("mytown.chat.cmd.channel")
public class CmdChannel extends CommandHandler {
	public CmdChannel() {
		super("ch", null);

		// Add sub commands
		addSubCommand(new CmdJoin(this));
		addSubCommand(new CmdLeave(this));
		addSubCommand(new CmdFocus(this));
	}
}