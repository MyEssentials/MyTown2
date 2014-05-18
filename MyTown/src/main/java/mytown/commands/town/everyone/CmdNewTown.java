package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Sub command to create a new town
 * 
 * @author Joe Goett
 */
@Permission("mytown.cmd.outsider.new")
public class CmdNewTown extends CommandBase {

	public CmdNewTown(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if (args.length < 1) {
			throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.newtown"));
		}
		if (getDatasource().hasTown(args[0])) {
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.newtown.nameinuse", (Object[]) args));
		}

		Town town = new Town(args[0]);
		Resident res = getDatasource().getOrMakeResident(sender.getCommandSenderName());
		EntityPlayer player = (EntityPlayer)sender;
		getDatasource().insertTown(town);
		getDatasource().linkResidentToTown(res, town, town.getRank("Mayor"));
		getDatasource().insertTownBlock(new TownBlock(town, player.chunkCoordX, player.chunkCoordZ, player.dimension));
		res.sendLocalizedMessage(MyTown.getLocal(), "mytown.notification.town.created", town.getName());
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