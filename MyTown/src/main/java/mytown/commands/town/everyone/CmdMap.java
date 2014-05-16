package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import net.minecraft.command.ICommandSender;

@Permission(node = "mytown.cmd.outsider.map")
public class CmdMap extends CommandBase {
	public CmdMap(String name, CommandBase parent) {
		super(name, parent);
	}

	
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		Resident res = MyTown.instance.datasource.getOrMakeResident(sender.getCommandSenderName());
		if (args.length == 0) {
			res.sendMap();
		} else {
			res.setMapOn(args[1] == "on" || args[1] == "enable" || args[1] == "true");
		}
	}
}