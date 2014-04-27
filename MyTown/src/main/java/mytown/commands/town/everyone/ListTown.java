package mytown.commands.town.everyone;

import java.util.Collections;
import java.util.List;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.entities.Town;
import net.minecraft.command.ICommandSender;

import com.google.common.base.Joiner;

public class ListTown extends SubCommandBase {
	@Override
	public String getName() {
		return "list";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.town.list";
	}
	
	@Override
	public boolean canUseByConsole() {
		return true;
	};
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		List<Town> sortedTowns = (List<Town>)MyTown.instance.datasource.getTowns();
		Collections.sort(sortedTowns); // TODO Cache the sort?
		String townList = Joiner.on(", ").join(sortedTowns);
		ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.list", townList);
	}
}