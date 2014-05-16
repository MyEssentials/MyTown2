package mytown.core.utils.command;

import java.util.List;

import mytown.core.utils.Assert;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

public class CommandBase implements Command {
	protected String permNode;
	protected String name;
	protected String parentName;
	
	public CommandBase(String name, CommandBase parent)
	{
		this.name = name;

		Permission permAnnot = getClass().getAnnotation(Permission.class);
		if (permAnnot != null) {
			permNode = permAnnot.node();
		} else {
			permNode = "";
		}
		String temp;
		if(parent != null)
		{
			this.parentName = parent.getCommandName();
			temp = parentName + '.' + name;
		} else {
			this.parentName = "";
			temp = name;
		}
		if(permNode.startsWith("mytown.adm"))
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

	@Override
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
	public String getCommandName()
	{
		return this.name;
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		// TODO Auto-generated method stub
	}

	public void process(ICommandSender icommandsender, String[] astring) throws Exception {
		
	}
	
	@Override
	public List<String> dumpCommands() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getParentName()
	{
		return this.parentName;
	}
			

}
