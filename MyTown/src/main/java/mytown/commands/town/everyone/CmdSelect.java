package mytown.commands.town.everyone;

import java.util.ArrayList;
import java.util.List;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.cmd.outsider.select")
public class CmdSelect extends CommandBase {

	public CmdSelect(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1)
			throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.select"));
		if (!getDatasource().hasTown(args[0]))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
		if (!getDatasource().getTown(args[0]).hasResident(getDatasource().getResident(sender.getCommandSenderName())))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.select.notpart", args[0]));

		try {
			Town town = getDatasource().getTown(args[0]);
			Resident res = getDatasource().getOrMakeResident(sender.getCommandSenderName());

			res.setSelectedTown(getDatasource().getTown(args[0]));
			getDatasource().updateLinkResidentToTown(res, town);
			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.select", args[0]);
		} catch (Exception e) {
			MyTown.instance.log.severe(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
			e.printStackTrace();
		}
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		try {
			Resident res = getDatasource().getOrMakeResident(sender.getCommandSenderName());
			List<String> tabComplete = new ArrayList<String>();
			for (Town t : res.getTowns()) {
				if (args.length == 0) {
					tabComplete.add(t.getName());
				} else if (t.getName().startsWith(args[0])) {
					tabComplete.add(t.getName());
				}
			}
			return tabComplete;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null; // If all else fails...
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
