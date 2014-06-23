package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
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

/**
 * Command to claim TownBlocks
 * 
 * @author Joe Goett
 */
@Permission("mytown.cmd.assistant.claim")
public class CmdClaim extends CommandHandler {

	public CmdClaim(String name, CommandBase parent) {
		super(name, parent);

		addSubCommand(new CmdClaimFar("far", this));
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
		if (args.length >= 1 && subCommands.containsKey(args[0])) {
			super.process(sender, args);
		} else {
			EntityPlayer player = (EntityPlayer) sender;
			Resident res = getDatasource().getOrMakeResident(player);
			Town town = res.getSelectedTown();
			if (getDatasource().hasTownBlock(String.format(TownBlock.keyFormat, player.dimension, player.chunkCoordX, player.chunkCoordZ)))
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.claim.already"));
			if (!checkNearby(player.dimension, player.chunkCoordX, player.chunkCoordZ, town))
				throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.claim.farClaim"));

			TownBlock block = new TownBlock(town, player.chunkCoordX, player.chunkCoordZ, player.dimension);
			getDatasource().insertTownBlock(block);

			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.townblock.added", block.getX() * 16, block.getZ() * 16, block.getX() * 16 + 15, block.getZ() * 16 + 15, town.getName());
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

	@Override
	public void sendHelp(ICommandSender sender) {
		// TODO Auto-generated method stub

	}

}