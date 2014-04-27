package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import net.minecraft.command.ICommandSender;

import com.google.common.base.Joiner;

@Permission(node="mytown.cmd.town.list")
public class CmdListTown extends SubCommandBase {

	public CmdListTown(String name)
	{
		super(name);
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
