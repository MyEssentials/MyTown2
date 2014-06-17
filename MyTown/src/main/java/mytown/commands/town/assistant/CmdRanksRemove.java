package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.cmd.assistant.ranks.remove")
public class CmdRanksRemove extends CommandBase {

	public CmdRanksRemove(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) throws CommandException {
		super.canCommandSenderUseCommand(sender);
		Resident res = getDatasource().getResident(sender.getCommandSenderName());

		if (res.getSelectedTown() == null)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(permNode))
			throw new CommandException("commands.generic.permission");

		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if (args.length < 1)
			throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.ranks"));
		Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
		if (!town.hasRankName(args[0]))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.rem.notexist", args[0], town.getName()));

		if (getDatasource().deleteRank(town.getRank(args[0]))) {
			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.rem", args[0], town.getName());
		} else {
			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.err.ranks.rem.notallowed", args[0]);
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
