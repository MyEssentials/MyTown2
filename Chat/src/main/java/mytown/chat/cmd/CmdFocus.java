package mytown.chat.cmd;

import java.util.List;

import mytown.chat.channels.ChannelHandler;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.chat.cmd.channel.focus")
public class CmdFocus extends CommandBase {
	public CmdFocus(CommandBase parent) {
		super("focus", parent);
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "focus [channel]";
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		return ChannelHandler.getChannels();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1)
			throw new WrongUsageException(getCommandUsage(sender));
		ChannelHandler.setActiveChannel(sender, args[0]);
	}
}