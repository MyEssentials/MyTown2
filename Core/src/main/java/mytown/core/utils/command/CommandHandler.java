package mytown.core.utils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.core.utils.chat.HelpMenu;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

import com.google.common.base.Joiner;

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
	 * Adds the CommandBase to this handler (Allows for aliases!)
	 * 
	 * @param subCmd
	 */
	public void addSubCommand(CommandBase subCmd) {
		subCommands.put(subCmd.getCommandName(), subCmd);
		if (subCmd.getCommandAliases() != null) {
			for (Object alias : subCmd.getCommandAliases()) {
				String aliasStr = (String) alias;
				subCommands.put(aliasStr, subCmd);
			}
		}
	}

	/**
	 * Removes the SubCommand from this handler (Allows for aliases!)
	 * 
	 * @param subCmd
	 */
	public void removeSubCommand(CommandBase subCmd) {
		subCommands.remove(subCmd.getCommandName());
		if (subCmd.getCommandAliases() != null) {
			for (Object alias : subCmd.getCommandAliases()) {
				String aliasStr = (String) alias;
				subCommands.remove(aliasStr);
			}
		}
	}

	@Override
	public String getCommandName() {
		return name;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
			sendHelp(sender, (args.length < 2 ? 1 : Integer.parseInt(args[1])));
			return;
		}
		ICommand cmd = subCommands.get(args[0]);
		if (cmd == null)
			throw new CommandNotFoundException();
		cmd.canCommandSenderUseCommand(sender);
		cmd.processCommand(sender, Arrays.copyOfRange(args, 1, args.length));
	}

	public Map<String, ICommand> getSubCommands() {
		return subCommands;
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		if (args.length == 0)
			return (List<?>) subCommands.keySet();
		else if (args.length == 1) {
			List<Object> tabCompletion = new ArrayList<Object>();
			for (String str : subCommands.keySet()) {
				if (str.startsWith(args[0])) {
					tabCompletion.add(str);
				}
			}
			return tabCompletion;
		} else if (args.length > 1) {
			ICommand subCmd = subCommands.get(args[0]);
			if (subCmd != null)
				return subCmd.addTabCompletionOptions(sender, args);
		}

		return null; // If all else fails...
	}
	
	public void sendHelp(ICommandSender sender, int page) {
		HelpMenu helpMenu = new HelpMenu(getCommandName());
		List<String> lines = new ArrayList<String>();
		
		for (ICommand cmd : subCommands.values()) {
			String line = cmd.getCommandUsage(sender); // TODO Send description of command if available? (annotation?)
			if (cmd instanceof CommandHandler && ((CommandHandler) cmd).subCommands.size() > 0) {
				line += " [" + Joiner.on("|").join(((CommandHandler) cmd).subCommands.keySet()) + "]";
			}
			lines.add(line);
		}
		
		helpMenu.setLines(lines);
		helpMenu.send(sender, page);
	}
}