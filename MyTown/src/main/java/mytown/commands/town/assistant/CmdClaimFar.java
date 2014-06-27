package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

@Permission("mytown.cmd.assistant.claim.far")
public class CmdClaimFar extends CommandBase {

	public CmdClaimFar(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return parent.canCommandSenderUseCommand(sender);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		try {
			EntityPlayer player = (EntityPlayer) sender;
			Resident res = getDatasource().getOrMakeResident(player);
			Town town = res.getSelectedTown();
			if (getDatasource().hasTownBlock(String.format(TownBlock.keyFormat, player.dimension, player.chunkCoordX, player.chunkCoordZ)))
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.claim.already"));
			if (checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town))
				throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.claim.notFarClaim"));

			TownBlock block = new TownBlock(town, player.chunkCoordX, player.chunkCoordZ, player.dimension);
			getDatasource().insertTownBlock(block);

			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.townblock.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName());
		} catch (Exception e) {
			MyTown.instance.log.severe(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
			e.printStackTrace();
		}
	}

	private boolean checkNearby(int dim, int x, int z, Town town) {
		int[] dx = { 1, 0, -1, 0 };
		int[] dz = { 0, 1, 0, -1 };

		for (int i = 0; i < 4; i++)
			if (getDatasource().hasTownBlock(dim, x + dx[i], z + dz[i], true, town))
				return true;
		return false;
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
