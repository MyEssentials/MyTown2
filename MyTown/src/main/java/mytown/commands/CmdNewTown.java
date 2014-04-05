package mytown.commands;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission(node="mytown.cmd.town")
public class CmdNewTown extends CommandBase {
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender) {
		return true;
	}

	@Override
	public String getCommandName() {
		return "town";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/" + getCommandName() + " new";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length < 2 || !args[0].equals("new")) {
			throw new CommandException("commands.generic.usage");
		}
		
		if (MyTown.INSTANCE.datasource.hasTown(args[1])) {
			ChatUtils.sendChat(sender, "Town name %s already used!", args[1]);
			return;
		}
		
		try {
			Town town = new Town(args[1]);
			getDatasource().insertTown(town);
			Resident res = getDatasource().getOrMakeResident(sender.getCommandSenderName());
			getDatasource().linkResidentToTown(res, town);
			ChatUtils.sendChat(sender, "Town %s created", town.getName());
		} catch (Exception e) {
			e.printStackTrace();  // TODO Change later
		}
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	/**
	 * Helper method to return the current MyTownDatasource instance
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return MyTown.INSTANCE.datasource;
	}
}