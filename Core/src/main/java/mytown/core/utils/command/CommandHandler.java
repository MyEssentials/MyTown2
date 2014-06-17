package mytown.core.utils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.core.ChatUtils;
import mytown.core.MyTownCore;
import mytown.core.utils.Log;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;

public abstract class CommandHandler extends CommandBase {
	protected static Log log = MyTownCore.Instance.log.createChild("CommandHandler"); // TODO Remove?
	protected Map<String, CommandBase> subCommands;
	
	public CommandHandler(String name) {
		this(name, null);
	}

	/**
	 * Creates a CommandHandler with the given name. Checks if it has Permission Annotation and uses that to get the permission node
	 * 
	 * @param name
	 */
	public CommandHandler(String name, CommandBase parent) {
		super(name, parent);
		subCommands = new Hashtable<String, CommandBase>();
	}

	/**
	 * Adds the CommandBase to this handler
	 * 
	 * @param subCmd
	 */
	public void addSubCommand(CommandBase subCmd) {
		subCommands.put(subCmd.getCommandName(), subCmd);
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
		try {
			process(sender, args);
		} catch (CommandException ex) {
			throw ex;
		} catch (Throwable ex) {
			log.warning("[%][%] An exception occured!", ex, getCommandName(), sender.getCommandSenderName());
			ex.printStackTrace();
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		canCommandSenderUseCommand(sender);
		if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
			sendHelp(sender);
			return;
		}
		try {
			CommandBase cmd = subCommands.get(args[0]);
			if (cmd == null) throw new CommandNotFoundException();
			cmd.canCommandSenderUseCommand(sender);
			cmd.process(sender, Arrays.copyOfRange(args, 1, args.length));
		} catch (NumberFormatException ex) {
			ChatUtils.sendChat(sender, "Number Format Error");
			log.severe("[%s][%s] Number Format Error", getCommandName(), sender.getCommandSenderName());
			ex.printStackTrace();
		} catch (CommandException ex) {
			throw ex;
		} catch (Throwable ex) {
			ChatUtils.sendChat(sender, ex.toString());
			log.severe("[%s][%s] Command Execution Error", ex, getCommandName(), sender.getCommandSenderName());
			ex.printStackTrace();
		}
	}

	public Map<String, CommandBase> getSubCommands() {
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
			CommandBase subCmd = subCommands.get(args[0]);
			if (subCmd != null) {
				return subCmd.addTabCompletionOptions(sender, args);
			}
		}
		
		return null; // If all else fails...
	}
	
	public abstract void sendHelp(ICommandSender sender);
}