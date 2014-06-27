package mytown.chat.cmd;

import java.util.List;

import mytown.chat.channels.ChannelHandler;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.chat.cmd.channel.join")
public class CmdJoin extends CommandBase {
	public CmdJoin(CommandBase parent) {
		super("join", parent);
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "join [channel]";
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		return ChannelHandler.getChannelNames();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1)
			throw new WrongUsageException(getCommandUsage(sender));
		ChannelHandler.joinChannel(sender, args[0]);
	}
}