package mytown.commands.town.everyone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.entities.Town;
import mytown.entities.comparator.TownComparator;
import net.minecraft.command.ICommandSender;

import com.google.common.base.Joiner;

@Permission(node = "mytown.cmd.outsider.list")
public class CmdListTown extends SubCommandBase {
	public CmdListTown(String name) {
		super(name);
	}

	@Override
	public boolean canUseByConsole() {
		return true;
	};

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		List<Town> sortedTowns =  new ArrayList<Town>( MyTown.instance.datasource.getTowns());
		TownComparator comp = new TownComparator(TownComparator.Order.Name);
		Collections.sort(sortedTowns, comp); // TODO Cache the sort?
		String townList = "\n" + Joiner.on("\n").join(sortedTowns);
		ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.list", townList);
	}
}