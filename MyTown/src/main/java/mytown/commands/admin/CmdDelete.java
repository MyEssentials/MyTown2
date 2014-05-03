package mytown.commands.admin;

import mytown.MyTown;
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
			throw new WrongUsageException("Wrong usage");
		if(!getDatasource().hasTown(args[0]))
			throw new CommandException("Town non-existant");
		
		if(args.length == 1)
		{
			if(getDatasource().deleteTown(getDatasource().getTown(args[0])))
				System.out.println("Town " + args[0] + " has been deleted.");
		}
		else
			for(String s : args)
			{
				if(getDatasource().deleteTown(getDatasource().getTown(s)))
					System.out.println("Town " + s + " has been deleted.");
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
