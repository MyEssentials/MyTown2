package mytown.commands.town.assistant;

import java.util.List;

import mytown.MyTown;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

@Permission(node = "mytown.cmd.invite")
public class CmdInvite extends SubCommandBase {

	public CmdInvite(String name)
	{
		super(name);
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