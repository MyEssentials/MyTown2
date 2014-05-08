package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Command to claim TownBlocks
 * 
 * @author Joe Goett
 */
@Permission(node = "mytown.cmd.assistant.claim")
public class CmdClaim extends SubCommandBase {

	public CmdClaim(String name) {
		super(name);
	}

	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		super.canUse(sender);
		
		Resident res = null;
		try {
			res = getDatasource().getOrMakeResident(sender.getCommandSenderName());
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change later
		}
		if (res.getTowns().size() == 0) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission("assistant.claim")) throw new CommandException("commands.generic.permission");
		
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		EntityPlayer player = (EntityPlayer) sender;
		Resident res = getDatasource().getOrMakeResident(player);
		Town town = res.getSelectedTown();
		if (getDatasource().getTownBlock(String.format(TownBlock.keyFormat, player.chunkCoordX, player.chunkCoordZ, player.dimension)) != null) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.claim.already"));
		TownBlock block = new TownBlock(town, player.chunkCoordX, player.chunkCoordZ, player.dimension);
		town.addTownBlock(block);
		getDatasource().insertTownBlock(block);
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