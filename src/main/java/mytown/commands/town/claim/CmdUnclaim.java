package mytown.commands.town.claim;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.Block;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
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
		Resident res = getDatasource().getOrMakeResident(((EntityPlayer)sender).getPersistentID());
        if (res == null)
            throw new CommandException("Unknown error"); // TODO Localize
		if (res.getTowns().size() == 0)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));

		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayer pl = (EntityPlayer) sender;
        Resident res = getDatasource().getOrMakeResident(pl.getPersistentID());
        if (res == null)
            throw new CommandException("Failed to get/make Resident"); // TODO Localize
        Block block = getDatasource().getBlock(pl.dimension, pl.chunkCoordX, pl.chunkCoordZ);
        if (block == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.claim.notexist"));
        Town town = block.getTown();
        if (!town.hasResident(res))
            throw new CommandException("Your not part of that town!"); // TODO Localize
        if (res.getTownRank(town).hasPermission(permNode))
            throw new CommandException("commands.generic.permission");
        if (block.isPointIn(town.getSpawn().getDim(), town.getSpawn().getX(), town.getSpawn().getZ())) {
            town.setSpawn(null); // Removes the Town's spawn point if in this Block
        }
        getDatasource().deleteBlock(block);

        /*
        ----- Old Implementation -----
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
			MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
			e.printStackTrace();
		}
         */
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