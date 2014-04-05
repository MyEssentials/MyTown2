package mytown.commands.town;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;

public class NewTown extends SubCommandBase {
	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.town.new";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
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

	/**
	 * Helper method to return the current MyTownDatasource instance
	 * @return
	 */
	private MyTownDatasource getDatasource() {
		return MyTown.INSTANCE.datasource;
	}
}