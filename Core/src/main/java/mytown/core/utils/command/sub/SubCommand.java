package mytown.core.utils.command.sub;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/**
 * Interface for SubCommands
 * @author Joe Goett
 */
public interface SubCommand {
	/**
	 * Returns the name of the command. Used to determine which sub-command to
	 * run.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Returns the permission node related to this sub command
	 * 
	 * @return
	 */
	public String getPermNode();

	/**
	 * Returns if the command can be used by the console
	 * 
	 * @return
	 */
	public boolean canUseByConsole();

	/**
	 * Checks if the sender can use the sub-command
	 * 
	 * @param sender
	 * @throws CommandException
	 * @throws NoAccessException
	 */
	public void canUse(ICommandSender sender) throws CommandException;

	/**
	 * Processes the sub-command
	 * 
	 * @param sender
	 * @param args
	 * @throws CommandException
	 * @throws NoAccessException
	 */
	public void process(ICommandSender sender, String[] args) throws CommandException;

	/**
	 * Returns the tab completion for the sub command
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	public List<String> tabComplete(ICommandSender sender, String[] args);

	public String getArgs(ICommandSender sender);

	public String getDesc(ICommandSender sender);
}
