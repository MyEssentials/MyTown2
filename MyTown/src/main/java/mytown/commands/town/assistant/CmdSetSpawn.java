package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
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

		Resident res = getDatasource().getResident(sender.getCommandSenderName());

		if (res.getTowns().size() == 0)
			throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(permNode))
			throw new CommandException("commands.generic.permission");

		return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		EntityPlayer player = (EntityPlayer) sender;
		Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();

		if (!town.isBlockInTown((int) player.posX, (int) player.posZ, player.dimension))
			throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.setspawn.notintown", town.getName()));

		town.setSpawn(player.posX, player.posY, player.posZ, player.dimension);
		try {
			getDatasource().updateTown(town);
			ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.setspawn");
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