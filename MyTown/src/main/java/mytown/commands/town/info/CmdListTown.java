package mytown.commands.town.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.comparator.TownComparator;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.ICommandSender;

import com.google.common.base.Joiner;

// TODO Move cache to another class?

@Permission("mytown.cmd.outsider.list")
public class CmdListTown extends CommandBase {
	private static TownComparator townNameComparator = new TownComparator(TownComparator.Order.Name);
	private static List<Town> sortedTownCache = new ArrayList<Town>();
	
	public CmdListTown(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canConsoleUse() {
		return true;
	};

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		String townList = "\n" + Joiner.on("\n").join(sortedTownCache);
		ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.list", townList);
	}
	
	public static void updateTownSortCache() {
		sortedTownCache.clear();
		sortedTownCache.addAll(DatasourceProxy.getDatasource().getTowns(true));
		Collections.sort(sortedTownCache, townNameComparator);
	}
}