package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.plot")
public class CmdPlot extends CommandHandler {

	public CmdPlot(String name, CommandBase parent) {
		super(name, parent);

		addSubCommand(new CmdPlotMake("make", this));
		addSubCommand(new CmdPlotSelect("select", this));
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
	public void process(ICommandSender sender, String[] args) throws Exception {
		if (args.length > 0 && subCommands.containsKey(args[0])) {
			super.process(sender, args);
		} else {
			// Do something here
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

	@Override
	public void sendHelp(ICommandSender sender) {
		// TODO Auto-generated method stub

	}
}
