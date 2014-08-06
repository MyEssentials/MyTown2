package mytown.commands.town.invite;

import java.util.List;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Resident;
import mytown.x_entities.town.Town;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

@Permission("mytown.cmd.assistant.invite")
public class CmdInvite extends CommandHandler {

	public CmdInvite(String name, CommandBase parent) {
		super(name, parent);

		addSubCommand(new CmdInviteAccept("accept", this));
		addSubCommand(new CmdInviteRefuse("refuse", this));
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length != 0) {
			super.processCommand(sender, args);
		} else {
			Resident res = getDatasource().getResident(sender.getCommandSenderName());
			if (res.getTowns().size() == 0)
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
			if (!res.getTownRank().hasPermission(permNode))
				throw new CommandException("commands.generic.permission");
			if (args.length < 1)
				throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.invite"));
			if (!getDatasource().hasResident(args[0]))
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.resident.notexist", args[0]));

			Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();

			if (town == null)
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
			if (town.hasResident(args[0]))
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.already", args[0], town.getName()));

			Resident target = getDatasource().getResident(args[0]);
			target.addInvitation(town);
			target.sendLocalizedMessage(LocalizationProxy.getLocalization(), "mytown.notification.town.invited", town.getName());
			res.sendLocalizedMessage(LocalizationProxy.getLocalization(), "mytown.notification.town.invite.sent", args[0]);
		}
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		return CommandUtils.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
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