package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.x_entities.Resident;
import mytown.x_entities.town.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.resident.leave")
public class CmdLeave extends CommandBase {
	public CmdLeave(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		super.canCommandSenderUseCommand(sender);

		if (getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown() == null)
			throw new CommandException("commands.generic.permission");

		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
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
