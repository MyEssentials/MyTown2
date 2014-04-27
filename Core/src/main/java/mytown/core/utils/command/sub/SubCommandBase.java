package mytown.core.utils.command.sub;

import java.util.List;

import mytown.core.utils.Assert;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class SubCommandBase implements SubCommand {
	
	public String permNode;
	public String name;
	
	public SubCommandBase(String name)
	{
		this.name = name;
		
		Permission permAnnot = getClass().getAnnotation(Permission.class);
		if (permAnnot != null) {
			permNode = permAnnot.node();
		} else {
			permNode = "";
		}
		CommandUtils.permissionList.put(name, permNode);
	}
	
	
	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		Assert.Perm(sender, getPermNode(), canUseByConsole());
	}

	@Override
	public boolean canUseByConsole() {
		return false;
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

	public String getDesc(ICommandSender sender) {
		return "";
	}

	public String getArgs(ICommandSender sender) {
		return "";
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getPermNode() {
		String node = this.getClass().getAnnotation(Permission.class).node();
		if(node != null)
			return node;
		else
			return null;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception
	{
		
	}
}