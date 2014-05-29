package mytown.commands.town.assistant;

import mytown.Formatter;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.perm")
public class CmdPerm extends CommandHandler {
	
	public CmdPerm(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void sendHelp(ICommandSender sender) {
		
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		super.canCommandSenderUseCommand(sender);

		Resident res = getDatasource().getResident(sender.getCommandSenderName());

		if (res.getTowns().size() == 0) throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(this.permNode)) throw new CommandException("commands.generic.permission");

		return true;
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if(args.length > 0 && subCommands.containsKey(args[0]))
			super.process(sender, args);
		else {
			Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
			ChatUtils.sendChat(sender, Formatter.formatFlagsToString(town.getFlags()));
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
