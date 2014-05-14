package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission(node="mytown.cmd.outsider.invite.accept")
public class CmdInviteAccept extends CommandBase {
	
	public CmdInviteAccept(String name, CommandBase parent)
	{
		super(name, parent);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception 
	{
		Resident res = MyTown.instance.datasource.getResident(sender.getCommandSenderName());
		if(res.getInvitations().size() == 0) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.invite.noinvitations"));
		if(res.getInvitations().size() != 1 && args.length == 0) throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.invite.accept"));
		if(res.getInvitations().size() != 1 && MyTown.instance.datasource.getTown(args[0]) != null) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.town.notexist", args[0]));
		String townName;
		if(args.length == 0) townName = res.getInvitations().get(0).getName();
		else townName = args[0];
		if(!res.getInvitations().contains(MyTown.instance.datasource.getTown(townName))) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.invite.accept"));
		res.confirmForm(true, townName);
	}

}
