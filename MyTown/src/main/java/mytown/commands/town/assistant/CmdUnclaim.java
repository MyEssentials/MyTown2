package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
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
	public void process(ICommandSender sender, String[] args) throws Exception {
		EntityPlayer player = (EntityPlayer) sender;
		Resident res = getDatasource().getOrMakeResident(player);
		TownBlock block = getDatasource().getTownBlock(player.dimension, player.chunkCoordX, player.chunkCoordZ, true);

		if (block == null)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.claim.notexist"));

		getDatasource().deleteTownBlock(block);
		ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.townblock.removed", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, res.getSelectedTown().getName());
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