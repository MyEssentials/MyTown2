package mytown.commands.town.assistant;

import java.util.List;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Rank;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission(node="mytown.cmd.assistant.ranks.add")
public class CmdRanksAdd extends CommandBase{

	public CmdRanksAdd(String name, CommandBase parent)
	{
		super(name, parent);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		
		if(args.length < 1)
			throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.ranks"));
		Town town = MyTown.instance.datasource.getResident(sender.getCommandSenderName()).getSelectedTown();
		
		if(town == null)
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.partOfTown"));
		if(town.hasRankName(args[0]))
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.ranks.add.already", args[0]));
		if(!town.hasRankName(args[1]))
		{
			for(Rank r : town.getRanks())
				System.out.println(r.getName());
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.ranks.add.notexist", args[1]));
		}
			
		Rank rank = new Rank(args[0], town.getRank(args[1]).getPermissions(), town);
		MyTown.instance.datasource.insertRank(rank);
		ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.ranks.add", args[0], town.getName());
	}
}
