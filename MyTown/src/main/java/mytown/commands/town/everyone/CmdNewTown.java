package mytown.commands.town.everyone;

import mytown.Constants;
import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.api.events.TownEvent.TownCreatedEvent;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.flag.TownFlag;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

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
	public void processCommand(ICommandSender sender, String[] args) {
		EntityPlayer player = (EntityPlayer) sender;

		if (args.length < 1)
			throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.newtown"));
		if (getDatasource().hasTown(args[0]))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.newtown.nameinuse", (Object[]) args));
		if (getDatasource().hasTownBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, true))
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.newtown.positionError"));

		try {
			Town town = new Town(args[0]);
			town.setSpawn(player.posX, player.posY, player.posZ, player.dimension);
			Resident res = getDatasource().getOrMakeResident(sender.getCommandSenderName());

			getDatasource().insertTown(town);
			getDatasource().linkResidentToTown(res, town, town.getRank(Constants.DEFAULT_SUPER_RANK));
			getDatasource().insertTownBlock(new TownBlock(town, player.chunkCoordX, player.chunkCoordZ, player.dimension));
			getDatasource().insertTownFlag(town, new TownFlag("mobs", "Controls mobs spawning", true));
			getDatasource().insertTownFlag(town, new TownFlag("breakBlocks", "Controls whether or not non-residents can break blocks", false));
			getDatasource().insertTownFlag(town, new TownFlag("explosions", "Controls if explosions can occur", true));
			getDatasource().insertTownFlag(town, new TownFlag("accessBlocks", "Controls whether or not non-residents can access(right click) blocks", false));
			getDatasource().insertTownFlag(town, new TownFlag("enter", "Controls whether or not a non-resident can enter the town", true));
			getDatasource().insertTownFlag(town, new TownFlag("pickup", "Controls whether or not a non-resident can pick up items", true));
			
			MinecraftForge.EVENT_BUS.post(new TownCreatedEvent(town));

			res.sendLocalizedMessage(MyTown.getLocal(), "mytown.notification.town.created", town.getName());
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