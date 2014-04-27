package mytown.core.utils.command.sub;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.core.ChatUtils;
import mytown.core.Log;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandNotFoundException;
import net.minecraft.command.ICommandSender;

/**
 * Handler for multiple SubCommands
 * @author Joe Goett
 */
public class SubCommandHandler extends CommandBase {
	protected String name = "";
	protected Map<String, SubCommand> subCommands;
	protected Log log;
	
	
	/**
	 * Creates a SubCommandHandler with the given name. Checks if it has Permission Annotation and uses that to get the permission node
	 * @param name
	 */
	public SubCommandHandler(String name) {
		subCommands = new Hashtable<String, SubCommand>();
		this.name = name;
		log = new Log(name);

		Permission permAnnot = this.getClass().getAnnotation(Permission.class);
		if (permAnnot != null) {
			permNode = permAnnot.node();
		} else {
			permNode = "";
		}
		CommandUtils.permissionList.put(name, permNode);
		
		
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
			ChatUtils.sendChat(sender, "Number Format Error");
		} catch (CommandException ex) {
			throw ex;
		} catch (Throwable ex) {
			ChatUtils.sendChat(sender, ex.toString());
			log.severe("Command execution error by %s", ex, sender);
		}
	}
	
	public Map<String, SubCommand> getSubCommands()
	{
		return this.subCommands;
	}
	
}