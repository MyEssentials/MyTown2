package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.cmd.outsider.select")
public class CmdSelect extends CommandBase {

	public CmdSelect(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1)
			throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.select"));
		if (!getDatasource().hasTown(args[0]))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
		if (!getDatasource().getTown(args[0]).hasResident(getDatasource().getResident(sender.getCommandSenderName())))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.select.notpart", args[0]));

		try {
			Town town = getDatasource().getTown(args[0]);
			Resident res = getDatasource().getResident(sender.getCommandSenderName());

			res.setSelectedTown(getDatasource().getTown(args[0]));
			getDatasource().updateLinkResidentToTown(res, town);
			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.select", args[0]);
		} catch (Exception e) {
			MyTown.instance.log.severe(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
			e.printStackTrace();
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
