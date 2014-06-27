package mytown.chat.channels;

import java.util.List;

import mytown.chat.MyTownChat;
import mytown.chat.api.IChannelType;
import mytown.core.ChatUtils;
import mytown.core.utils.Assert;
import mytown.core.utils.Log;
import net.minecraft.command.ICommandSender;

/**
 * Defines a Chat Channel
 * 
 * @author Joe Goett
 */
public class Channel {
	private static Log log = MyTownChat.Instance.chatLog.createChild("Channel");
	
	public String name;
	public String abbreviation;
	public String format;
	public int radius;
	public IChannelType type;

	/**
	 * Constructs a channel with the given name, abbreviation, format, radius, and type
	 * 
	 * @param name
	 * @param abbreviation
	 * @param format
	 * @param radius
	 * @param type
	 */
	public Channel(String name, String abbreviation, String format, int radius, IChannelType type) {
		this.name = name;
		this.abbreviation = abbreviation;
		this.format = format;
		this.radius = radius;
		this.type = type;
	}

	/**
	 * Gets the format for the channel. Replaces $name$ and $abbreviation$ with the channel's name and abbreviation
	 * 
	 * @return
	 */
	public String getFormat() {
		return format.replace("$chName$", name).replace("$chAbbreviation$", abbreviation);
	}

	/**
	 * Sends a message to everyone in the senders channel
	 * 
	 * @param sender
	 * @param message
	 */
	public void sendMessage(ICommandSender sender, String message) {
		try {
			Assert.Perm(sender, "mytown.chat.channel." + name + ".send");
			List<ICommandSender> recipients = type.getRecipients(sender, this);
			for (ICommandSender recipient : recipients) {
				try {
					Assert.Perm(recipient, "mytown.chat.channel." + name + ".receive");
					ChatUtils.sendChat(recipient, message);
				} catch(Exception ex) {
					// Ignore since this is just to stop the msg from being sent
				}
			}
		} catch(Exception ex) {
			log.fine("[%s] %s doesn't have the permission to send to this channel", name, sender.getCommandSenderName());
			// TODO Tell player they can't send to this channel
		}
	}

	@Override
	public String toString() {
		return String.format("%s;%s;%s;%s;%s", name, abbreviation, format, radius, type);
	}
}