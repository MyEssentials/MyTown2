package mytown.proxies.mod.MyTownChat;

import mytown.chat.api.IChatFormatter;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class MyTownChatFormatter implements IChatFormatter {
	@Override
	public String format(ICommandSender sender, String format, String message) {
		if (sender == null || format == null || message == null || !(sender instanceof EntityPlayer))
			return format;
		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(sender.getCommandSenderName());
			Town town = res.getSelectedTown(); // TODO List all towns/ranks?
			return format.replace("$town$", town == null ? "" : town.getName()).replace("$town_rank$", town == null ? "" : town.getResidentRank(res).getName());
		} catch (Exception ex) {
			// TODO Log exception
		}
		return format;
	}
}