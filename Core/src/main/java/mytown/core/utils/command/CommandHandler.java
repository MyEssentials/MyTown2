package mytown.core.utils.command;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.core.ChatUtils;
import mytown.core.Log;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;

public abstract class CommandHandler extends CommandBase {
	protected static Log log = new Log("CommandHandler");
	protected Map<String, CommandBase> subCommands;

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
	public List<String> dumpCommands() {
		return null;
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
			System.out.println("caught exception!");
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
			cmd.processCommand(sender, Arrays.copyOfRange(args, 1, args.length));
		} catch (NumberFormatException ex) {
			ChatUtils.sendChat(sender, "Number Format Error");
		} catch (CommandException ex) {
			throw ex;
		} catch (Throwable ex) {
			ChatUtils.sendChat(sender, ex.toString());
			ex.printStackTrace();
			log.severe("[%s]Command execution error by %s", ex, getCommandName(), sender);
		}
	}

	public Map<String, CommandBase> getSubCommands() {
		return this.subCommands;
	}
	
	public abstract void sendHelp(ICommandSender sender);
}