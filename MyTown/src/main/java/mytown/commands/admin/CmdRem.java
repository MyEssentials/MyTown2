package mytown.commands.admin;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.adm.cmd.rem")
public class CmdRem extends CommandBase {

	public CmdRem(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 2)
			throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.adm.cmd.usage.rem"));
		if (!getDatasource().hasResident(args[0]))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.resident.notexist", args[0]));
		if (!getDatasource().hasTown(args[1]))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[1]));
		if (!getDatasource().getTown(args[1]).hasResident(getDatasource().getResident(args[0])))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.adm.cmd.err.rem.resident", (Object[]) args));
		try {
			getDatasource().unlinkResidentFromTown(getDatasource().getResident(args[0]), getDatasource().getTown(args[1]));
			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.resident.remove", (Object[]) args);
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
