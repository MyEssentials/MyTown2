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

@Permission("mytown.adm.cmd.delete")
public class CmdDelete extends CommandBase {

	public CmdDelete(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 1)
			throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.adm.cmd.delete.usage"));
		if (!getDatasource().hasTown(args[0]))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist"), args[0]);

		try {
			if (args.length == 1) {
				if (getDatasource().deleteTown(getDatasource().getTown(args[0]))) {
					ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.deleted", args[0]);
				}
			} else {
				for (String s : args)
					if (getDatasource().getTown(s) == null)
						throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist"), s);
				for (String s : args) {
					if (getDatasource().deleteTown(getDatasource().getTown(s))) {
						ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.deleted", s);
					}
				}
			}
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
		return DatasourceProxy.getDatasource();
	}
}
