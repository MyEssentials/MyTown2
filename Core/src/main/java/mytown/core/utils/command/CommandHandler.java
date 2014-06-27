package mytown.core.utils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

public abstract class CommandHandler extends CommandBase {
	
	protected Map<String, ICommand> subCommands;
	
	public CommandHandler(String name) {
		this(name, null);
	}

	/**
	 * Creates a CommandHandler with the given name. Checks if it has Permission Annotation and uses that to get the permission node
	 * 
	 * @param name
	 */
	public CommandHandler(String name, ICommand parent) {
		super(name, parent);
		subCommands = new Hashtable<String, ICommand>();
	}

	/**
	 * Adds the CommandBase to this handler
	 * 
	 * @param subCmd
	 */
	public void addSubCommand(CommandBase subCmd) {
		subCommands.put(subCmd.getCommandName(), subCmd);
		//System.out.println("Loaded command " + subCmd.getParentName() + "." + subCmd.getCommandName());
	}

	/**
	 * Removes the SubCommand from this handler
	 * 
	 * @param subCmd
	 */
	public void removeSubCommand(CommandBase subCmd) {
		subCommands.remove(subCmd.getCommandName());
	}

	@Override
	public String getCommandName() {
		return name;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/" + getCommandName();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
			sendHelp(sender);
			return;
		}
		ICommand cmd = subCommands.get(args[0]);
		if (cmd == null) throw new CommandNotFoundException();
		cmd.canCommandSenderUseCommand(sender);
		cmd.processCommand(sender, Arrays.copyOfRange(args, 1, args.length));
	}
	
	public Map<String, ICommand> getSubCommands() {
		return this.subCommands;
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 0) {
			return (List<?>) subCommands.keySet();
		} else if (args.length == 1) {
			List<Object> tabCompletion = new ArrayList<Object>();
			for (String str : subCommands.keySet()) {
				if (str.startsWith(args[0])) {
					tabCompletion.add(str);
				}
			}
			return tabCompletion;
		} else if (args.length > 1) {
			ICommand subCmd = subCommands.get(args[0]);
			if (subCmd != null) {
				return subCmd.addTabCompletionOptions(sender, args);
			}
		}
		
		return null; // If all else fails...
	}
	
	public abstract void sendHelp(ICommandSender sender);
}