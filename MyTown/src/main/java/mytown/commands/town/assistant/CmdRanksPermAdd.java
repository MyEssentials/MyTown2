package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission(node="mytown.cmd.assistant.ranks.perm.add")
public class CmdRanksPermAdd extends CommandBase{

	public CmdRanksPermAdd(String name, CommandBase parent)
	{
		super(name, parent);
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) throws CommandException
	{
		super.canCommandSenderUseCommand(sender);
		Resident res = MyTown.instance.datasource.getResident(sender.getCommandSenderName());
		
		if(res.getSelectedTown() == null) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.partOfTown"));
		if(!res.getTownRank().hasPermission(permNode)) throw new CommandException("commands.generic.permission");
		
		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception
	{
		if(args.length < 2) throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.ranks.perm"));
		
		Town town = MyTown.instance.datasource.getResident(sender.getCommandSenderName()).getSelectedTown();
		
		if(MyTown.instance.datasource.getRank(args[0], town) == null) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.ranks.rem.notexist", args[0], town.getName()));
		if(!CommandUtils.permissionList.containsValue(args[1])) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.ranks.perm.notexist", args[1]));
	
		// Adding permission if everything is alright
		if(town.getRank(args[0]).addPermission(args[1]))
		{
			MyTown.instance.datasource.updateRank(town.getRank(args[0]));
			ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.ranks.perm.add", args[1], args[0]);
		} else {
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.ranks.perm.add.failed", args[1]));
		}
	}
}
