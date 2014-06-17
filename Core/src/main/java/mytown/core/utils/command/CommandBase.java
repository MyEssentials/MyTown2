package mytown.core.utils.command;

import java.util.List;

import mytown.core.utils.Assert;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

public class CommandBase implements ICommand {
	protected String permNode;
	protected String name;
	protected String parentName;
	
	public CommandBase(String name) {
		this(name, null);
	}

	protected CommandBase parent;

	public CommandBase(String name, CommandBase parent) {
		this.name = name;
		this.parent = parent;

		Permission permAnnot = getClass().getAnnotation(Permission.class);
		if (permAnnot != null) {
			permNode = permAnnot.value();
		} else {
			permNode = ""; // TODO Maybe change to use classpath by default instead?
		}
		String temp;
		if (parent != null) {
			this.parentName = parent.getCommandName();
			temp = parentName + '.' + name;
		} else {
			this.parentName = "";
			temp = name;
		}
		if (permNode.startsWith("mytown.adm"))
			CommandUtils.permissionListAdmin.put(temp, permNode);
		else
			CommandUtils.permissionList.put(temp, permNode);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		Assert.Perm(sender, getPermNode(), canConsoleUse());
		return true;
	}

	public String getPermNode() {
		return permNode;
	}

	@Override
	public List<?> getCommandAliases() {
		return null;
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		return null;
	}

	public boolean canConsoleUse() {
		return false;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		return false;
	}

	public int compareTo(ICommand sender) {
		return getCommandName().compareTo(sender.getCommandName());
	}

	@Override
	public int compareTo(Object obj) {
		return this.compareTo((ICommand) obj);
	}

	@Override
	public String getCommandName() {
		return this.name;
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return null;
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
	}

	public void process(ICommandSender sender, String[] args) throws Exception {
	}

	public String getParentName() {
		return this.parentName;
	}
	
	public CommandBase getParent() {
		return this.parent;
	}
}