package mytown.commands.town;

import mytown.MyTown;
import mytown.core.utils.command.sub.SubCommandBase;
import mytown.entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class Map extends SubCommandBase {
	@Override
	public String getName() {
		return "map";
	}

	@Override
	public String getPermNode() {
		return "mytown.cmd.map";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws CommandException {
		try {
			Resident res = MyTown.instance.datasource.getOrMakeResident(sender.getCommandSenderName());
			res.sendMap(
					res.getPlayer().dimension,
					res.getPlayer().chunkCoordX,
					res.getPlayer().chunkCoordZ);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}