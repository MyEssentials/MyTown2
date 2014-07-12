package mytown.proxies.mod.MyTownChat;

import java.util.ArrayList;
import java.util.List;

import mytown.MyTown;
import mytown.chat.api.IChannelType;
import mytown.chat.channels.Channel;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.ICommandSender;

/**
 * IChannelType to allow for TownChat. Uses the Residents currently selected town
 * 
 * @author Joe Goett
 */
public class TownChatChannelType implements IChannelType {
	@Override
	public String name() {
		return "TownChat";
	}

	@Override
	public List<ICommandSender> getRecipients(ICommandSender sender, Channel channel) {
		List<ICommandSender> recipients = new ArrayList<ICommandSender>();
		try {
			Resident senderRes = DatasourceProxy.getDatasource().getOrMakeResident(sender.getCommandSenderName());
			for (Resident res : senderRes.getSelectedTown().getResidents()) {
				recipients.add(res.getPlayer());
			}
		} catch (Exception e) {
			MyTown.instance.log.warn("[TownChatChannelType] Failed to get Resident object of %s", e, sender.getCommandSenderName());
		}
		return recipients;
	}
}