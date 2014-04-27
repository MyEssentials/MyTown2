package mytown.commands.admin;

import mytown.MyTown;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

// TODO Implement reload

/**
 * Command to reload MyTown
 * @author Joe Goett
 */
@Permission(node="mytown.adm.cmd.reload")
public class CmdReload extends SubCommandBase {
	
	public CmdReload(String name)
	{
		super(name);
		
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.unimplemented", getName()));
	}
}