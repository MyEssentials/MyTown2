package mytown.commands.town.everyone;

import mytown.MyTown;
import mytown.chat.channels.types.Local;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

@Permission("mytown.cmd.resident.spawn")
public class CmdSpawn extends CommandBase {
	
	public CmdSpawn(String name, CommandBase parent) {
		super(name, parent);
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
			throws CommandException {
		super.canCommandSenderUseCommand(sender);
		Resident res = getDatasource().getResident(sender.getCommandSenderName());

		if (res.getSelectedTown() == null) throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(permNode)) throw new CommandException("commands.generic.permission");

		return true;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		EntityPlayer player = (EntityPlayer)sender;
		Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
		
		if(!town.hasSpawn()) throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.spawn.notexist", town.getName()));
		
		player.dimension = town.getSpawnDim();
		player.setPositionAndUpdate(town.getSpawnX(), town.getSpawnY(), town.getSpawnZ());
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
