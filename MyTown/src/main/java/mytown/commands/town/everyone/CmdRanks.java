package mytown.commands.town.everyone;

import mytown.Constants;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.entities.Rank;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

@Permission(node="mytown.cmd.outsider.ranks")
public class CmdRanks extends SubCommandBase{

	public CmdRanks(String name)
	{
		super(name);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception
	{
		Town temp = null;
		if(args.length < 1)
		{
			temp = MyTown.instance.datasource.getResident(sender.getCommandSenderName()).getSelectedTown();
		}
		if(args.length >= 1)
		{
			temp = MyTown.instance.datasource.getTown(args[0]);
		}
		if(temp == null)
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.partOfTown"));
		
		String jupiter = null;
		String color;
		for(Rank r : temp.getRanks())
		{
			if(Constants.DEFAULT_RANK_VALUES.keySet().contains(r.getName()))
				color = EnumChatFormatting.GREEN + "";
			else
				color = "";
			if(jupiter == null)
				jupiter = color + r.getName() + EnumChatFormatting.WHITE;
			else
				jupiter += ", " + color + r.getName() + EnumChatFormatting.WHITE;
		}
		
		
		ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.ranks", jupiter);
	}
}
