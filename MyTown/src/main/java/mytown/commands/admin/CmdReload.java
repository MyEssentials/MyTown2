package mytown.commands.admin;

import mytown.MyTown;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

// TODO Implement reload

/**
 * Command to reload MyTown
 * 
 * @author Joe Goett
 */
@Permission("mytown.adm.cmd.reload")
public class CmdReload extends CommandBase {

	public CmdReload(String name, CommandBase parent) {
		super(name, parent);

	}

	@Override
	public void process(ICommandSender sender, String[] args) {
		throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.unimplemented", getCommandName()));
	}
}