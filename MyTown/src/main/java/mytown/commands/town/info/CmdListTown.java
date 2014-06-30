package mytown.commands.town.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.comparator.TownComparator;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.ICommandSender;

import com.google.common.base.Joiner;

@Permission("mytown.cmd.outsider.list")
public class CmdListTown extends CommandBase {
	private static TownComparator townNameComparator = new TownComparator(TownComparator.Order.Name);;
	
	public CmdListTown(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canConsoleUse() {
		return true;
	};

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		List<Town> sortedTowns = new ArrayList<Town>(getDatasource().getTowns(true));
		Collections.sort(sortedTowns, townNameComparator); // TODO Cache the sort?
		String townList = "\n" + Joiner.on("\n").join(sortedTowns);
		ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.list", townList);
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