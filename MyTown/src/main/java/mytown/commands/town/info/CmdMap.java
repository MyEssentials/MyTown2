package mytown.commands.town.info;

import java.util.ArrayList;
import java.util.List;

import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.x_entities.Resident;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.outsider.map")
public class CmdMap extends CommandBase {
	private List<String> tabCompletionOptions;
	
	public CmdMap(String name, CommandBase parent) {
		super(name, parent);
		
		// Setup tab completion
		tabCompletionOptions = new ArrayList<String>();
		tabCompletionOptions.add("on");
		tabCompletionOptions.add("true");
		tabCompletionOptions.add("enable");
		tabCompletionOptions.add("off");
		tabCompletionOptions.add("false");
		tabCompletionOptions.add("disable");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		if (args.length == 0) {
			res.sendMap();
		} else {
			res.setMapOn(ChatUtils.equalsOn(args[0]));
		}
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		return tabCompletionOptions;
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