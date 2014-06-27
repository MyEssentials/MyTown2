package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.outsider.invite.accept")
public class CmdInviteAccept extends CommandBase {

	public CmdInviteAccept(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		if (res.getInvitations().size() == 0)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.noinvitations"));
		if (res.getInvitations().size() > 1 && args.length == 0)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.accept"));
		if (res.getInvitations().size() > 1 && getDatasource().getTown(args[0]) == null)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
		String townName;
		if (args.length == 0) {
			townName = res.getInvitations().get(0).getName();
		} else {
			townName = args[0];
		}
		if (!res.getInvitations().contains(getDatasource().getTown(townName)))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.usage.invite"));
		res.confirmForm(true, townName);
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
