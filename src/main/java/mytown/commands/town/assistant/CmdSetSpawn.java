package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.proxies.DatasourceProxy;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

@Permission("mytown.cmd.assistant.setspawn")
public class CmdSetSpawn extends CommandBase {
	public CmdSetSpawn(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		super.canCommandSenderUseCommand(sender);

		Resident res = getDatasource().getOrMakeResident(((EntityPlayer)sender).getPersistentID());

        if (res == null)
            throw new CommandException("Failed to get/make Resident"); // TODO Localize!
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
            throw new CommandException("Failed to get/make Resident"); // TODO Localize
        Town town = res.getSelectedTown();
        if (!town.isChunkInTown(player.dimension, player.chunkCoordX, player.chunkCoordZ))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.setspawn.notintown", town.getName()));
        town.getSpawn().setDim(player.dimension).setPosition((float)player.posX, (float)player.posY, (float)player.posZ).setRotation(player.cameraYaw, player.cameraPitch);
        getDatasource().saveTown(town);
        ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.setspawn");
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