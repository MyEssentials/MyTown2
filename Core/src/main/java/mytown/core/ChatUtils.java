package mytown.core;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;

/**
 * Useful methods for Chat
 * 
 * @author Joe Goett
 */
public class ChatUtils {
	/**
	 * Sends msg to sender
	 * 
	 * @param sender
	 * @param msg
	 * @param args
	 */
	public static void sendChat(ICommandSender sender, String msg, Object... args) {
		sender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format(msg, args)));
	}

	/**
	 * Sends a localized msg to sender
	 * 
	 * @param sender
	 * @param local
	 * @param key
	 * @param args
	 */
	public static void sendLocalizedChat(ICommandSender sender, Localization local, String key, Object... args) {
		ChatUtils.sendChat(sender, local.getLocalization(key), args);
	}
	
	/**
	 * Returns true if arg equals on, enable, true, or t. False otherwise.
	 * 
	 * @param arg
	 * @param caseSensitive
	 * @return
	 */
	// TODO Change name/change location?
	public static boolean equalsOn(String arg, boolean caseSensitive) {
		if (!caseSensitive) arg = arg.toLowerCase();
		return arg == "on" || arg == "enable" || arg == "true" || arg == "t";
	}
	
	/**
	 * Same as {@link ChatUtils#equalsOn(String, boolean)}, but is not case sensitive
	 * @param arg
	 * @return
	 */
	public static boolean equalsOn(String arg) {
		return equalsOn(arg, false);
	}
}