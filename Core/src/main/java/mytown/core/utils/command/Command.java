package mytown.core.utils.command;

import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

// TODO Change name?
public interface Command extends ICommand{
	/**
	 * Dumps this command and any associated sub-commands to a list
	 * 
	 * @return
	 */
	public List<String> dumpCommands();

	/**
	 * Gets the permission node for this command
	 * 
	 * @return
	 */
	public String getPermNode();

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender);

	/**
	 * Returns whether the console can use this or not
	 * 
	 * @return
	 */
	public boolean canConsoleUse();
}