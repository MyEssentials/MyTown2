package mytown.chat.channels;

import java.util.List;

import net.minecraft.command.ICommandSender;

/**
 * Defines a Channel Type
 * @author Joe Goett
 */
public interface IChannelType {
	public List<ICommandSender> getRecipients(ICommandSender sender, Channel channel);
}