package mytown.chat.channels;

import java.util.List;

import net.minecraft.command.ICommandSender;

/**
 * Defines a Channel Type
 * @author Joe Goett
 */
public interface IChannelType {
	/**
	 * Returns the name of the IChannelType, used for quick lookup
	 * @return
	 */
	public String name();
	
	/**
	 * Returns a List of all ICommandSenders that will receive the senders message
	 * @param sender
	 * @param channel
	 * @return
	 */
	public List<ICommandSender> getRecipients(ICommandSender sender, Channel channel);
}