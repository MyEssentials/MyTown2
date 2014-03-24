package mytown.core;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;

/**
 * Useful methods for Chat
 * @author Joe Goett
 */
public class ChatUtils {
	/**
	 * Sends msg to sender
	 * @param sender
	 * @param msg
	 * @param args
	 */
	public static void sendChat(ICommandSender sender, String msg, Object...args){
		sender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format(msg, args)));
	}
}