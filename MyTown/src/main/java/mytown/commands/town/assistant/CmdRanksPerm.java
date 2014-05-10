package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.entities.Rank;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission(node="mytown.cmd.assistant.ranks.perm")
public class CmdRanksPerm extends CommandHandler{

	public CmdRanksPerm(String name, CommandBase parent)
	{
		super(name, parent);
		
		addSubCommand(new CmdRanksPermAdd("add", this));
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception
	{
		if(args.length >= 1 && subCommands.keySet().contains(args[0]))
			super.process(sender, args);
		else
		{
			Rank rank;
			if(MyTown.instance.datasource.getResident(sender.getCommandSenderName()).getSelectedTown() == null)
				throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.partOfTown"));
			if(args.length == 0)
				rank = MyTown.instance.datasource.getResident(sender.getCommandSenderName()).getTownRank();
			else
				rank = MyTown.instance.datasource.getRank(args[0], MyTown.instance.datasource.getResident(sender.getCommandSenderName()).getSelectedTown());
			String msg = "";
			for(String s : rank.getPermissions())
				msg += '\n' + s;
			ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.ranks.perm.list", rank.getName(), rank.getTown().getName(), msg);
		}
	}
			
}
