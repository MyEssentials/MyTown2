package mytown.chat.format;

import net.minecraft.command.ICommandSender;

/**
 * Default ChatFormatter for MyTownChat
 * 
 * @author Joe Goett
 */
public class ChatFormatter implements IChatFormatter {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String format(ICommandSender sender, String format, String message) {
		format = format.replace("$username$", sender.getCommandSenderName()).replace("$msg$", message);
		return format;
	}
}