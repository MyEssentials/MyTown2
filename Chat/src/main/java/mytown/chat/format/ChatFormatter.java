package mytown.chat.format;

import net.minecraft.command.ICommandSender;

public class ChatFormatter implements IChatFormatter{
	@Override
	public String format(ICommandSender sender, String format, String message) {
		format = format.replace("$username$", sender.getCommandSenderName()).replace("$msg$", message);
		return format;
	}
}