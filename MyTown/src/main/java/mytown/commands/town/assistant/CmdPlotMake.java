package mytown.commands.town.assistant;

import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.plot.make")
public class CmdPlotMake extends CommandBase {

	public CmdPlotMake(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return parent.canCommandSenderUseCommand(sender);
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		boolean result = res.makePlotFromSelection();
		if (result) {
			ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.created");
		} else
			throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.failed"));
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
