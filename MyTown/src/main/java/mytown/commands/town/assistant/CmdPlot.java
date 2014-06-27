package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.interfaces.ITownPlot;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.plot")
public class CmdPlot extends CommandHandler {

	public CmdPlot(String name, CommandBase parent) {
		super(name, parent);

		addSubCommand(new CmdPlotMake("make", this));
		addSubCommand(new CmdPlotSelect("select", this));
		addSubCommand(new CmdPlotRename("rename", this));

		addSubCommand(new CmdPlotShow("show", this));
		addSubCommand(new CmdPlotVanish("vanish", this));
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		super.canCommandSenderUseCommand(sender);

		Resident res = getDatasource().getResident(sender.getCommandSenderName());

		if (res.getTowns().size() == 0)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(permNode))
			throw new CommandException("commands.generic.permission");

		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length > 0) {
			super.processCommand(sender, args);
		} else {
			Resident resident = getDatasource().getResident(sender.getCommandSenderName());
			Town town = resident.getSelectedTown();
			if (town == null)
				throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.partOfTown"));

			String formattedPlotsList = "";
			for (ITownPlot plot : town.getTownPlots()) {
				formattedPlotsList += "\n";
				formattedPlotsList += plot;
			}
			ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.plots", town, formattedPlotsList);
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

	@Override
	public void sendHelp(ICommandSender sender) {
		// TODO Auto-generated method stub

	}
}
