package mytown.commands.town.claim;

import mytown.MyTown;
import mytown.core.utils.Assert;
import mytown.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.proxies.DatasourceProxy;
import mytown.entities.Resident;
import mytown.entities.Block;
import mytown.entities.Town;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Command to claim TownBlocks
 * 
 * @author Joe Goett
 */
@Permission("mytown.cmd.assistant.claim")
public class CmdClaim extends CommandBase {

	public CmdClaim(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		super.canCommandSenderUseCommand(sender);

		Resident res = getDatasource().getOrMakeResident(((EntityPlayer)sender).getPersistentID());
        if (res == null)
            throw new CommandException("Unknown error"); // TODO Localize
		if (res.getTowns().size() == 0)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(permNode))
			throw new CommandException("commands.generic.permission");

		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayer player = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(player.getPersistentID());
        if (res == null)
            throw new CommandException("Failed to get/make resident"); // TODO Localize
        Town town = res.getSelectedTown();
        if (town == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        if (getDatasource().hasBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.claim.already"));
        if (checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town)) // Checks if the player can claim far
            Assert.Perm(player, "mytown.cmd.assistant.claim.far");
        Block block = getDatasource().newBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, town);
        if (block == null)
            throw new CommandException("Failed to create Block"); // TODO Localize
        getDatasource().saveBlock(block);
        res.sendMessage(MyTown.getLocal().getLocalization("mytown.notification.townblock.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName()));

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