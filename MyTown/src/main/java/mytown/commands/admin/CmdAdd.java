package mytown.commands.admin;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission(node="mytown.adm.cmd.add")
public class CmdAdd extends CommandBase{

	public CmdAdd(String name, CommandBase parent)
	{
		super(name, parent);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception
	{
		if(args.length < 2)
			throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.adm.cmd.usage.add"));
		if(!MyTown.instance.datasource.hasTown(args[1]))
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.town.notexist", args[1]));
		if(!MyTown.instance.datasource.hasResident(args[0]))
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.resident.notexist", args[0]));
		if(MyTown.instance.datasource.getTown(args[1]).hasResident(MyTown.instance.datasource.getResident(args[0])))
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.adm.cmd.err.add.already", (Object[])args));
		MyTown.instance.datasource.linkResidentToTown(MyTown.instance.datasource.getResident(args[0]), MyTown.instance.datasource.getTown(args[1]));
		
		ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.resident.add", (Object[])args);
	}
}
