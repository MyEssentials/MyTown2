package mytown.chat.channels;

import java.util.List;

import mytown.core.utils.ChatUtils;
import net.minecraft.command.ICommandSender;

/**
 * Defines a Chat Channel
 * @author Joe Goett
 */
public class Channel {
	public String name;
	public String abbreviation;
	public String format;
	public String permission;
	public int radius;
	public IChannelType type;
	
	/**
	 * Constructs a channel with the given name, abbreviation, format, radius, and type
	 * @param name
	 * @param abbreviation
	 * @param format
	 * @param radius
	 * @param type
	 */
	public Channel(String name, String abbreviation, String format, int radius, IChannelType type){
		this.name = name;
		this.abbreviation = abbreviation;
		this.format = format;
		this.radius = radius;
		this.type = type;
	}
	
	/**
	 * Gets the format for the channel.
	 * Replaces $name$ and $abbreviation$ with the channel's name and abbreviation
	 * @return
	 */
	public String getFormat(){
		return format.replace("$name$", name).replace("$abbreviation$", abbreviation);
	}
	
	/**
	 * Sends a message to everyone in the senders channel
	 * @param sender
	 * @param message
	 */
	public void sendMessage(ICommandSender sender, String message){
		List<ICommandSender> recipients = type.getRecipients(sender, this);
		for (ICommandSender recipient : recipients){
			ChatUtils.sendChat(recipient, message);
		}
	}
}