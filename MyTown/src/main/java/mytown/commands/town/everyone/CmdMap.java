package mytown.commands.town.everyone;

import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.outsider.map")
public class CmdMap extends CommandBase {
	public CmdMap(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		if (args.length == 0) {
			res.sendMap();
		} else {
			res.setMapOn(args[1] == "on" || args[1] == "enable" || args[1] == "true");
		}
	}

	/**
	 * Helper method to return the current MyTownDatasource instance
	 * 
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return DatasourceProxy.getDatasource();
	}
}