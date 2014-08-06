package mytown.commands.town.rank;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.proxies.X_DatasourceProxy;
import mytown.util.x_Formatter;
import mytown.x_entities.town.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.outsider.ranks")
public class CmdRanks extends CommandHandler {

	public CmdRanks(String name, CommandBase parent) {
		super(name, parent);
		addSubCommand(new CmdRanksAdd("add", this));
		addSubCommand(new CmdRanksRemove("remove", this));
		addSubCommand(new CmdRanksPerm("perm", this));
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length >= 1) {
			super.processCommand(sender, args);
		} else {
			Town temp = null;
			if (args.length < 1) {
				temp = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
				if (temp == null)
					throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
			}
			if (args.length >= 1) {
				temp = getDatasource().getTown(args[0]);
				if (temp == null)
					throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
			}

			String jupiter = x_Formatter.formatRanksToString(temp.getRanks());
			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks", jupiter);

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