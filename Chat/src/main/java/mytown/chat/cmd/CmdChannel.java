package mytown.chat.cmd;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

// TODO Finish /ch channel command
public class CmdChannel extends CommandBase {
	@Override
	public String getCommandName() {
		return "ch";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {}

	@Override
	public int compareTo(Object o) {
		return 0;
	}
}