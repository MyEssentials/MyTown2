package mytown.chat.format;

import mytown.chat.api.IChatFormatter;
import net.minecraft.command.ICommandSender;

/**
 * Default ChatFormatter for MyTownChat
 * 
 * @author Joe Goett
 */
public class ChatFormatter implements IChatFormatter {
	@Override
	public String format(ICommandSender sender, String format, String message) {
		return format.replace("$username$", sender.getCommandSenderName()).replace("$worldName$", sender.getEntityWorld().getProviderName()).replace("$msg$", message); // TODO Change the way $worldName$ is set 
	}
}