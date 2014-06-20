package mytown.commands.town.assistant;

import mytown.MyTown;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.cmd.assistant.promote")
public class CmdPromote extends CommandBase {
	public CmdPromote(String name, CommandBase parent) {
		super(name, parent);
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		super.canCommandSenderUseCommand(sender);

		Resident res = getDatasource().getResident(sender.getCommandSenderName());

		if (res.getTowns().size() == 0) throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(this.permNode)) throw new CommandException("commands.generic.permission");

		return true;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		// /t promote <user> <rank>
		if(args.length < 2) throw new WrongUsageException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.usage.promote"));
		Resident resSender = getDatasource().getResident(sender.getCommandSenderName());
		Resident resTarget = getDatasource().getResident(args[0]);
		Town town = resSender.getSelectedTown();
		
		if(resTarget == null) 
			throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.resident.notexist", args[0]));
		if(!resTarget.getTowns().contains(town)) 
			throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.resident.notsametown", args[0], town.getName()));
		if(!resSender.getSelectedTown().hasRankName(args[1]))
			throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.rank.notexist", args[1], town.getName()));
		if(args[1].equalsIgnoreCase("mayor"))
			throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.promote.notMayor"));
		try {
			Rank rank = town.getRank(args[1]);
			town.promoteResident(resTarget, rank);
			getDatasource().updateLinkResidentToTown(resTarget, town);
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
