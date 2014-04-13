package mytown.commands.town;

import java.util.List;

import mytown.MyTown;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.sub.SubCommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

public class Invite extends SubCommandBase {
	@Override
	public String getName() {
		return "invite";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.invite";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if (args.length < 1) {
			throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.invite"));
		}
	}
	
	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return CommandUtils.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
	}
}