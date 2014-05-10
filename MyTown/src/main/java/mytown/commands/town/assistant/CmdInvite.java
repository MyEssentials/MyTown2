package mytown.commands.town.assistant;

import java.util.List;

import mytown.MyTown;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

@Permission(node = "mytown.cmd.assistant.invite")
public class CmdInvite extends CommandBase {

	public CmdInvite(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) throws CommandException {
		super.canCommandSenderUseCommand(sender);
		Resident res = null;
		try {
			res = MyTown.instance.datasource.getOrMakeResident(sender.getCommandSenderName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!res.getTownRank().hasPermission("assistant.invite")) throw new CommandException("commands.generic.permission");
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if (args.length < 1) {
			throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.invite"));
		}
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
		return CommandUtils.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
	}
}