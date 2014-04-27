package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.entities.Resident;
import net.minecraft.command.ICommandSender;

@Permission(node = "mytown.cmd.map")
public class CmdMap extends SubCommandBase {
	
	public CmdMap(String name)
	{
		super(name);
	}
			
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		Resident res = MyTown.instance.datasource.getOrMakeResident(sender.getCommandSenderName());
		if (args.length == 0) {
			res.sendMap();
		} else {
			res.setMapOn((args[1] == "on" || args[1] == "enable" || args[1] == "true"));
		}
	}
}