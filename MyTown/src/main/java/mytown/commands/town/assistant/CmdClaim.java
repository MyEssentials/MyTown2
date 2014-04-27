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
 * @author Joe Goett
 */
@Permission(node = "mytown.cmd.town.claim")
public class CmdClaim extends SubCommandBase {
	
	public CmdClaim(String name)
	{
		super(name);
	}
	
	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		try {
			EntityPlayer player = (EntityPlayer)sender;
			Resident res = getDatasource().getOrMakeResident(player);
			if (res.getTowns().size() == 0) return; // TODO Tell player they need to be part of a town
			Town town = res.getSelectedTown();
			if (getDatasource().getTownBlock(String.format(TownBlock.keyFormat, player.chunkCoordX, player.chunkCoordZ, player.dimension)) != null) return; // TODO Tell player the block is already claimed
			TownBlock block = new TownBlock(town, player.chunkCoordX, player.chunkCoordZ, player.dimension);
			town.addTownBlock(block);
			getDatasource().insertTownBlock(block);
		} catch (Exception e) {
			throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.failedToClaim")); // TODO Add reason?
		}
	}

	/**
	 * Helper method to return the current MyTownDatasource instance
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return MyTown.instance.datasource;
	}
}