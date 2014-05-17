package mytown.commands.town.everyone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Town;
import mytown.entities.comparator.TownComparator;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.ICommandSender;

import com.google.common.base.Joiner;

@Permission("mytown.cmd.outsider.list")
public class CmdListTown extends CommandBase {
	public CmdListTown(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canConsoleUse() {
		return true;
	};

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		List<Town> sortedTowns = new ArrayList<Town>(getDatasource().getTowns());
		TownComparator comp = new TownComparator(TownComparator.Order.Name);
		Collections.sort(sortedTowns, comp); // TODO Cache the sort?
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