package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.interfaces.ITownFlag;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.perm")
public class CmdPerm extends CommandHandler {

	public CmdPerm(String name, CommandBase parent) {
		super(name, parent);

		addSubCommand(new CmdPermSet("set", this));
	}

	@Override
	public void sendHelp(ICommandSender sender) {

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
		if (args.length > 0)
			super.processCommand(sender, args);
		else {
			Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
			String formattedFlagList = null;
			for (ITownFlag flag : town.getFlags()) {
				if (formattedFlagList == null)
					formattedFlagList = "";
				else
					formattedFlagList += '\n';
				formattedFlagList += flag;
			}
			ChatUtils.sendChat(sender, formattedFlagList);
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
