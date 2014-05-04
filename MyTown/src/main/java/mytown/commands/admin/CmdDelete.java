package mytown.commands.admin;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.datasource.MyTownDatasource;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;


@Permission(node="mytown.adm.cmd.delete")
public class CmdDelete extends SubCommandBase{

	public CmdDelete(String name)
	{
		super(name);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if(args.length < 1)
			throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.adm.cmd.delete.usage"));
		if(!getDatasource().hasTown(args[0]))
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.town.notexist"), args[0]);
		
		if(args.length == 1)
		{
			if(getDatasource().deleteTown(getDatasource().getTown(args[0])))
				ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.deleted", args[0]);
		}
		else
		{
			for(String s : args)
				if(getDatasource().getTown(s) == null)
					throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.town.notexist"), s);
			for(String s : args)
			{
				if(getDatasource().deleteTown(getDatasource().getTown(s)))
					ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.deleted", s);
			}
		}
	}
	
	/**
	 * Helper method to return the current MyTownDatasource instance
	 * 
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return MyTown.instance.datasource;
	}
}
