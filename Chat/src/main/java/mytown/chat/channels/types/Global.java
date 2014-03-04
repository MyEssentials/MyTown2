package mytown.chat.channels.types;

import java.util.List;

import mytown.chat.channels.Channel;
import mytown.chat.channels.IChannelType;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class Global implements IChannelType{
	@SuppressWarnings("unchecked")
	@Override
	public List<ICommandSender> getRecipients(ICommandSender sender, Channel channel) {
		return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
	}
}