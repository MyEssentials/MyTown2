package mytown.commands.town;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.sub.SubCommandBase;
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
		String townList = Joiner.on(", ").join(MyTown.instance.datasource.getTowns());
		ChatUtils.sendLocalizedChat(sender, MyTown.instance.local, "mytown.notification.town.list", townList);
	}

}
