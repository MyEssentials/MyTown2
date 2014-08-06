package mytown.commands.town.rank;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Rank;
import mytown.x_entities.Resident;
import mytown.x_entities.town.Town;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.cmd.assistant.ranks.add")
public class CmdRanksAdd extends CommandBase {

	public CmdRanksAdd(String name, CommandBase parent) {
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
	public void processCommand(ICommandSender sender, String[] args) {

		if (args.length < 1)
			throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.ranks"));

		Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();

		if (town.hasRankName(args[0]))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.add.already", args[0]));
		if (!town.hasRankName(args[1]))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.add.notexist", args[1]));

		try {
			Rank rank = new Rank(args[0], town.getRank(args[1]).getPermissions(), town);
			getDatasource().insertRank(rank);
			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.add", args[0], town.getName());
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
		return X_DatasourceProxy.getDatasource();
	}
}
