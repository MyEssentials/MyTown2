package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;


@Permission(node="mytown.cmd.outsider.select")
public class CmdSelect extends SubCommandBase{
	
	public CmdSelect(String name)
	{
		super(name);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception
	{
		if(args.length < 1)
			throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.select"));
		if(!MyTown.instance.datasource.hasTown(args[0]))
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.town.notexist", args[0]));
		if(!MyTown.instance.datasource.getTown(args[0]).hasResident(MyTown.instance.datasource.getResident(sender.getCommandSenderName())))
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.select.notpart", args[0]));
		MyTown.instance.datasource.getResident(sender.getCommandSenderName()).setSelectedTown(MyTown.instance.datasource.getTown(args[0]));
		ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.select", args[0]);
	}
}
