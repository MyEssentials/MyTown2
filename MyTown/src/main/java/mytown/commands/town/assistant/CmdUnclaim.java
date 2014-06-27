package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Command to claim TownBlocks
 * 
 * @author Joe Goett
 */
@Permission("mytown.cmd.assistant.unclaim")
public class CmdUnclaim extends CommandBase {

	public CmdUnclaim(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) throws CommandException {
		super.canCommandSenderUseCommand(sender);

		Resident res = null;
		try {
			res = getDatasource().getOrMakeResident(sender.getCommandSenderName());
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change later
		}
		if (res.getTowns().size() == 0)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(permNode))
			throw new CommandException("commands.generic.permission");

		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		EntityPlayer player = (EntityPlayer) sender;
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		TownBlock block = getDatasource().getTownBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, true);

		if (block == null)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.claim.notexist"));
		try {
			if (block.isBlockInChunk((int) block.getTown().getSpawnX(), (int) block.getTown().getSpawnZ(), block.getTown().getSpawnDim())) {
				block.getTown().setSpawnState(false); // No longer has a spawn point
			}

			getDatasource().deleteTownBlock(block);
			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.townblock.removed", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, res.getSelectedTown().getName());
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
		return MyTown.getDatasource();
	}
}