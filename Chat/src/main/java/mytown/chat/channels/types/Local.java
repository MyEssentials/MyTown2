package mytown.chat.channels.types;

import java.util.ArrayList;
import java.util.List;

import mytown.chat.api.IChannelType;
import mytown.chat.channels.Channel;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/**
 * Local Channel Type. Sends messages to people within a specific radius
 * 
 * @author Joe Goett
 */
public class Local implements IChannelType {
	public String name() {
		return "Local";
	}

	@Override
	public List<ICommandSender> getRecipients(ICommandSender sender, Channel channel) {
		List<ICommandSender> recipients = new ArrayList<ICommandSender>();
		if (sender instanceof EntityPlayer) {
			EntityPlayer senderPl = (EntityPlayer) sender;
			double posX = senderPl.posX, posY = senderPl.posY, posZ = senderPl.posZ;
			int dsqr = channel.radius * channel.radius;

			for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				EntityPlayer pl = (EntityPlayer) obj;
				if (pl != senderPl && pl.dimension == senderPl.dimension && pl.getDistanceSq(posX, posY, posZ) <= dsqr) {
					recipients.add(pl);
				}
			}
		}
		return recipients;
	}
}