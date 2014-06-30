package mytown.commands.town.perm;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.interfaces.ITownFlag;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.cmd.assistant.perm.set")
public class CmdPermSet extends CommandBase {

	public CmdPermSet(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		super.canCommandSenderUseCommand(sender);

		Resident res = getDatasource().getResident(sender.getCommandSenderName());

		if (res.getTowns().size() == 0)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(permNode))
			throw new CommandException("commands.generic.permission");

		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 2)
			throw new WrongUsageException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.perm.set.usage"));
		Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
		ITownFlag flag = town.getFlag(args[0]);
		if (flag == null)
			throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.flagNotExists", args[0]));
		try {
			if (args[1].equals("true")) {
				flag.setValue(true);
			} else if (args[1].equals("false")) {
				flag.setValue(false);
			} else
				throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.perm.valueNotValid", args[1]));
			ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.perm.set.success", args[0], args[1]);

			getDatasource().updateTownFlag(flag);
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
