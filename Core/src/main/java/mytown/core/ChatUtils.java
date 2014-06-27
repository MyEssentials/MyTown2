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
}