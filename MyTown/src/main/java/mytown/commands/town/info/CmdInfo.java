package mytown.commands.town.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.comparator.TownComparator;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.outsider.info")
public class CmdInfo extends CommandBase {

	public CmdInfo(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		List<Town> towns = new ArrayList<Town>();

		if (args.length < 1) {
			if (res.getSelectedTown() != null) {
				towns.add(res.getSelectedTown());
			} else
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.info.notpart"));
		} else {
			// Printing out info for all towns.
			if (args[0].equals("@a")) {

				towns = new ArrayList<Town>(getDatasource().getTowns(false));

				// Using Comparator object to compare names and such
				TownComparator comp = new TownComparator(TownComparator.Order.Name);
				Collections.sort(towns, comp);
			} else if (getDatasource().hasTown(args[0])) {
				towns.add(getDatasource().getTown(args[0]));
			} else
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
		}
		for (Town town : towns) {
			ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.info", (Object[]) town.getInfo());
		}
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		List<String> tabComplete = new ArrayList<String>();
		if (args.length == 0 || args[0].isEmpty()) {
			tabComplete.add("@a"); // Add the "all" selector
			tabComplete.addAll(getDatasource().getTownsMap().keySet());
		} else {
			for (Town t : getDatasource().getTowns(false)) {
				if (t.getName().startsWith(args[0])) {
					tabComplete.add(t.getName());
				}
			}
		}
		return tabComplete;
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