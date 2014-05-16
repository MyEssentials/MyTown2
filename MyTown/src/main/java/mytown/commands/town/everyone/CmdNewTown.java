package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.core.utils.Assert;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

/**
 * Sub command to create a new town
 * 
 * @author Joe Goett
 */
@Permission(node = "mytown.cmd.outsider.new")
public class CmdNewTown extends CommandBase {

	public CmdNewTown(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if (args.length < 1) {
			throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.newtown"));
		}
		if (MyTown.instance.datasource.hasTown(args[0])) {
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.newtown.nameinuse", (Object[]) args));
		}

		Town town = new Town(args[0]);
		Resident res = getDatasource().getOrMakeResident(sender.getCommandSenderName());
		getDatasource().insertTown(town);
		getDatasource().linkResidentToTown(res, town);
		res.sendLocalizedMessage(MyTown.instance.local, "mytown.notification.town.created", town.getName());
	}


	/**
	 * Helper method to return the current MyTownDatasource instance
	 * 
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return MyTown.instance.datasource;
	}
	
}