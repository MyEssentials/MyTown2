package mytown.commands.town.everyone;

import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.town.Town;
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
	
		if(getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown() == null) throw new CommandException("commands.generic.permission");
		
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		Town town = res.getSelectedTown();
		if(town == null) throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.partOfTown"));
		getDatasource().unlinkResidentFromTown(res, town);
		ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.left", town.getName());
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
