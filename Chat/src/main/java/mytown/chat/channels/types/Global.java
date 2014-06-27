package mytown.chat.channels.types;

import java.util.List;

import mytown.chat.api.IChannelType;
import mytown.chat.channels.Channel;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Global Channel Type. Broadcasts to everyone on the server
 * 
 * @author Joe Goett
 */
public class Global implements IChannelType {
	@Override
	public String name() {
		return "Global";
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ICommandSender> getRecipients(ICommandSender sender, Channel channel) {
		return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
	}
}