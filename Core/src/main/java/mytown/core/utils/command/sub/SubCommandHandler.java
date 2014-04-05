package mytown.core.utils.command.sub;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;

/**
 * Handler for multiple SubCommands
 * @author Joe Goett
 */
public class SubCommandHandler extends CommandBase {
	private String name = "";
	private Map<String, SubCommand> subCommands;
	
	/**
	 * Creates a SubCommandHandler with the given name. Checks if it has Permission Annotation and uses that to get the permission node
	 * @param name
	 */
	public SubCommandHandler(String name) {
		this.name = name;
		
		Permission permAnnot = getClass().getAnnotation(Permission.class);
		if (permAnnot != null) {
			permNode = permAnnot.node();
		} else {
			permNode = "";
		}
	}
	
	/**
	 * Creates a SubCommandHandler with the given name and permission node
	 * @param name
	 * @param permNode
	 */
	public SubCommandHandler(String name, String permNode) {
		subCommands = new Hashtable<String, SubCommand>();
		this.name = name;
		this.permNode = permNode;
	}
	
	/**
	 * Adds the SubCommand to this handler
	 * @param subCmd
	 */
	public void addSubCommand(SubCommand subCmd) {
		subCommands.put(subCmd.getName(), subCmd);
	}
	
	/**
	 * Removes the SubCommand from this handler
	 * @param subCmd
	 */
	public void removeSubCommand(SubCommand subCmd) {
		subCommands.remove(subCmd.getName());
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
		canCommandSenderUseCommand(sender);
		if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
			// TODO Print help
			return;
		}
		try {
			SubCommand cmd = subCommands.get(args[0]);
			if (cmd == null)
				throw new CommandNotFoundException();
			cmd.canUse(sender);
			cmd.process(sender, Arrays.copyOfRange(args, 1, args.length));
		} catch (NumberFormatException ex) {
//			MyTown.sendChatToPlayer(sender, Formatter.commandError(Level.WARNING, Term.TownErrCmdNumberFormatException.toString()));
		} catch (CommandException ex) {
			throw ex;
		} catch (Throwable ex) {
//			MyTown.instance.coreLog.log(Level.WARNING, String.format("Command execution error by %s", sender), ex);
//			MyTown.sendChatToPlayer(sender, Formatter.commandError(Level.SEVERE, ex.toString()));
		}
	}
}