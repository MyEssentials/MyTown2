package mytown.chat.format;

import net.minecraft.command.ICommandSender;

/**
 * Formats a chat message
 * @author Joe Goett
 */
public interface IChatFormatter {
	/**
	 * Formats the message to match the given format
	 * @param sender
	 * @param format
	 * @param message
	 * @return
	 */
	public String format(ICommandSender sender, String format, String message);
}