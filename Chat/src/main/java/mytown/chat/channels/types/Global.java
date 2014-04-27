package mytown.chat.channels.types;

import java.util.List;

import mytown.chat.channels.Channel;
import mytown.chat.channels.IChannelType;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Global Channel Type. Broadcasts to everyone on the server
 * 
 * @author Joe Goett
 */
public class Global implements IChannelType {
	/**
	 * {@inheritDoc}
	 */
	public String name() {
		return "Global";
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ICommandSender> getRecipients(ICommandSender sender, Channel channel) {
		return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
	}
}