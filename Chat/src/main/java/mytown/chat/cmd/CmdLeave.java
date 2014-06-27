package mytown.chat.cmd;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import mytown.chat.channels.ChannelHandler;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;

@Permission("mytown.chat.cmd.channel.leave")
public class CmdLeave extends CommandBase {
	public CmdLeave(CommandBase parent) {
		super("leave", parent);
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "leave [channel]";
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		String pre = "";
		if (args.length > 1) {
			pre = args[0];
		}
		return ChannelHandler.getChannels(sender, pre);
	}

	@Override
	public void processCommand (ICommandSender sender, String[] args) {
		if (args.length < 1) {
			throw new WrongUsageException(getCommandUsage(sender));
		}
		ChannelHandler.leaveChannel(sender, args[0]);
	}	
}