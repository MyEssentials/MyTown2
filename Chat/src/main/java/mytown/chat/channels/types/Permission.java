package mytown.chat.channels.types;

import java.util.ArrayList;
import java.util.List;

import mytown.chat.channels.Channel;
import mytown.chat.channels.IChannelType;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import forgeperms.api.ForgePermsAPI;

/**
 * Permission Channel Type. Sends messages to anyone with the specified permission node
 * 
 * @author Joe Goett
 */
public class Permission implements IChannelType {
	/**
	 * {@inheritDoc}
	 */
	public String name() {
		return "Permission";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ICommandSender> getRecipients(ICommandSender sender, Channel channel) {
		List<ICommandSender> recipients = new ArrayList<ICommandSender>();
		for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			ICommandSender r = (ICommandSender) obj;
			if (ForgePermsAPI.permManager.canAccess(r.getCommandSenderName(), r.getEntityWorld().provider.getDimensionName(), channel.permission)) {
				recipients.add(r);
			}
		}
		return recipients;
	}
}