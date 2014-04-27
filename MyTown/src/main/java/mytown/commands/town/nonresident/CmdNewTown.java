package mytown.commands.town.nonresident;

import mytown.MyTown;
import mytown.core.utils.Assert;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

/**
 * Sub command to create a new town
 * @author Joe Goett
 */
@Permission(node = "mytown.cmd.town.new")
public class CmdNewTown extends SubCommandBase {

	public CmdNewTown(String name)
	{
		super(name);
	}
	
	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		try {
			Resident res = getDatasource().getOrMakeResident(sender.getCommandSenderName());
			Assert.Perm(sender, "mytown.cmd.town.new." + (res.getTowns().size()+1));
		} catch(CommandException ce){
			throw (ce);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if (args.length < 1) {
			throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.newtown"));
		}
		if (MyTown.instance.datasource.hasTown(args[0])) {
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.newtown.nameinuse", (Object[])args));
		}
		
		Town town = new Town(args[0]);
		getDatasource().insertTown(town);
		Resident res = getDatasource().getOrMakeResident(sender.getCommandSenderName());
		getDatasource().linkResidentToTown(res, town);
		res.sendLocalizedMessage("mytown.notification.town.created", MyTown.instance.local, town.getName());
	}

	/**
	 * Helper method to return the current MyTownDatasource instance
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return MyTown.instance.datasource;
	}
}