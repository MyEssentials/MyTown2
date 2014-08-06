package mytown.commands.town.invite;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

// TODO Move to new Datasource

@Permission("mytown.cmd.outsider.invite.refuse")
public class CmdInviteRefuse extends CommandBase {

	public CmdInviteRefuse(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		if (res.getInvitations().size() == 0)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.noinvitations"));
		if (res.getInvitations().size() != 1 && args.length == 0)
			throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.invite.accept"));
		if (res.getInvitations().size() != 1 && getDatasource().getTown(args[0]) != null)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
		String townName;
		if (args.length == 0) {
			townName = res.getInvitations().get(0).getName();
		} else {
			townName = args[0];
		}
		if (!res.getInvitations().contains(getDatasource().getTown(townName)))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.accept"));
		res.confirmForm(false, townName);
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
