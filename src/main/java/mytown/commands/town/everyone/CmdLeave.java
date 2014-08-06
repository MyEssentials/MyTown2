package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.proxies.DatasourceProxy;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

@Permission("mytown.cmd.resident.leave")
public class CmdLeave extends CommandBase {
	public CmdLeave(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
        Resident res = getDatasource().getOrMakeResident(((EntityPlayer)sender).getPersistentID(), true);
        if (res == null)
            throw new CommandException("Resident is null"); // TODO Localize!

        Town town = res.getSelectedTown();
        if (town == null)
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.partOfTown"));

        // TODO Unlink the Resident from the Town

        res.sendMessage(MyTown.getLocal().getLocalization("mytown.notification.town.left.self", town.getName()));

        for (Resident r : town.getResidents()) {
            r.sendMessage(MyTown.getLocal().getLocalization("mytown.notification.town.left", res.getPlayerName(), town.getName()));
        }

        /*
        ----- Old Implementation
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		Town town = res.getSelectedTown();
		if (town == null)
			throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.partOfTown"));
		try {
			getDatasource().unlinkResidentFromTown(res, town);
			// Send message to player that has left the town
			ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.left.self", town.getName());

			// Send message to all that player has left the town
			for (Resident r : town.getResidents()) {
				r.sendLocalizedMessage(LocalizationProxy.getLocalization(), "mytown.notification.town.left", res.getUUID(), town.getName());
			}
		} catch (Exception e) {
			MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
			e.printStackTrace();
		}
		*/
	}

	/**
	 * Helper method to return the current MyTownDatasource instance
	 * 
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return DatasourceProxy.getDatasource();
	}
}
