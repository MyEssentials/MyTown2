package mytown.commands.town.everyone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.comparator.TownComparator;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission(node="mytown.cmd.info")
public class CmdInfo extends SubCommandBase{

	public CmdInfo(String name)
	{
		super(name);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception
	{
		Resident res = MyTown.instance.datasource.getResident(sender.getCommandSenderName());
		String[] msg = new String[2];
		
		if(args.length < 1)
			if(res.getSelectedTown() != null)
				msg = prepare(msg, res.getSelectedTown());
			else
				throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.info.notpart"));
		
		if(args.length >= 1)
		{
			// Printing out info for all towns.
			System.out.println(args[0]);
			if(args[0].equals("@a"))
			{
				msg = new String[MyTown.instance.datasource.getTowns().size() * 2];
				List<Town> temp = new ArrayList<Town>(MyTown.instance.datasource.getTowns());
				
				// Using Comparator object to compare names and such
				TownComparator comp = new TownComparator(TownComparator.Order.Name);
				Collections.sort(temp, comp);
				msg = prepare(msg, temp.toArray(new Town[temp.size()]));
			}
			else if(MyTown.instance.datasource.hasTown(args[0]))
			{
				msg = prepare(msg, MyTown.instance.datasource.getTown(args[0]));
			}
			else
				throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.town.notexist", args[0]));
		}
		for(int i = 0; i < msg.length / 2; i++)
			ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.info", msg[i*2], msg[i*2+1]);
	}
	public String[] prepare(String[] msg, Town... towns)
	{
		int i = 0;
		for(Town t : towns)
		{
			msg[i*2] = t.getName() + '\n';
			for(Resident r : t.getResidents())
				if(msg[i*2+1] == null)
					msg[i*2+1] = r.getUUID();
				else
					msg[i*2+1] += ", " + r.getUUID();
			i++;
		}
		
		return msg;
	}
	
}
