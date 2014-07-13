package mytown.commands.town.plot;

import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.town.Town;
import mytown.handlers.VisualsTickHandler;
import mytown.interfaces.ITownPlot;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.plot.show")
public class CmdPlotShow extends CommandBase {
	public CmdPlotShow(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return parent.canCommandSenderUseCommand(sender);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
		for (ITownPlot plot : town.getPlots()) {
			VisualsTickHandler.instance.markPlotBorders(plot);
		}
		ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.plot.showing");
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
