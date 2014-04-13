package mytown.commands.admin;

import mytown.MyTown;
import mytown.core.utils.command.sub.SubCommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

// TODO Implement reload

/**
 * Command to reload MyTown
 * @author Joe Goett
 */
public class Reload extends SubCommandBase {
	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.reload";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.unimplemented", getName()));
	}
}