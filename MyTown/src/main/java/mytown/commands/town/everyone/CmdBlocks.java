package mytown.commands.town.everyone;

import mytown.Formatter;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;


@Permission(node="mytown.cmd.assistant.blocks")
public class CmdBlocks extends CommandHandler{

	public CmdBlocks(String name, CommandBase parent)
	{
		super(name, parent);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception
	{
		if(args.length == 1 && subCommands.containsKey(args[0]))
			super.process(sender, args);
		else
		{
			Resident res = MyTown.instance.datasource.getResident(sender.getCommandSenderName());
			Town town;
			if(args.length == 0)
				if(res.getSelectedTown() == null)
					throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.partOfTown"));
				else
					town = res.getSelectedTown();
			else
				if(!MyTown.instance.datasource.hasTown(args[0]))
					throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.town.notexist", args[0]));
				else
					town = res.getSelectedTown();
			
			ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.townblock.list", town.getName(), Formatter.formatTownBlocksToString(town.getTownBlocks(), false));
		}
		
	}
	
	
}
