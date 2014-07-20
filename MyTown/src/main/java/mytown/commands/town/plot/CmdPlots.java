package mytown.commands.town.plot;

import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.util.x_Formatter;
import mytown.x_entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.outsider.plots")
public class CmdPlots extends CommandBase {

	public CmdPlots(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		super.canCommandSenderUseCommand(sender);
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		if (!res.getTownRank().hasPermission(permNode))
			throw new CommandException("commands.generic.permission");
		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		if (args.length == 0) {
			if (res.getSelectedTown() == null)
				throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.partOfTown"));
			else {
				ChatUtils.sendChat(sender, x_Formatter.formatTownPlotsToString(res.getSelectedTown().getPlots()));
			}
		}
	}

	private MyTownDatasource getDatasource() {
		return DatasourceProxy.getDatasource();
	}
}
