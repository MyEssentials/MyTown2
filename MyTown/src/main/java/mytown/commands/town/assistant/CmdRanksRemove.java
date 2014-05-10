package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission(node="mytown.cmd.assistant.ranks.remove")
public class CmdRanksRemove extends CommandBase{

	public CmdRanksRemove(String name, CommandBase parent)
	{
		super(name, parent);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception
	{
		if(args.length < 1)
			throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.ranks"));
		Town town = MyTown.instance.datasource.getResident(sender.getCommandSenderName()).getSelectedTown();
		if(town == null)
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.partOfTown"));
		if(!town.hasRankName(args[0]))
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.ranks.rem.notexist", args[0], town.getName()));
		
		if(MyTown.instance.datasource.deleteRank(town.getRank(args[0])))
			ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.ranks.rem", args[0], town.getName());
		else
			ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.cmd.err.ranks.rem.notallowed", args[0]);
	}
}
