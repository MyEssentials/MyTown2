package mytown.commands.town.everyone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mytown.Formatter;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.comparator.TownComparator;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

@Permission("mytown.cmd.outsider.info")
public class CmdInfo extends CommandBase {

	public CmdInfo(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		Resident res = getDatasource().getResident(sender.getCommandSenderName());
		String[] msg = new String[3];

		if (args.length < 1) {
			if (res.getSelectedTown() != null)
				msg = prepare(msg, res.getSelectedTown());
			else
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.info.notpart"));
		}

		if (args.length >= 1) {
			// Printing out info for all towns.
			if (args[0].equals("@a")) {
				msg = new String[getDatasource().getTowns().size() * 3];
				List<Town> temp = new ArrayList<Town>(getDatasource().getTowns());

				// Using Comparator object to compare names and such
				TownComparator comp = new TownComparator(TownComparator.Order.Name);
				Collections.sort(temp, comp);
				msg = prepare(msg, temp.toArray(new Town[temp.size()]));
			} else if (getDatasource().hasTown(args[0])) {
				msg = prepare(msg, getDatasource().getTown(args[0]));
			} else
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
		}
		for (int i = 0; i < msg.length / 3; i++)
			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.info", msg[i * 3], msg[i * 3 + 1], msg[i * 3 + 2]);
	}

	public String[] prepare(String[] msg, Town... towns) {
		int i = 0;
		for(Town t : towns) {
			msg[i*3] = EnumChatFormatting.RED + t.getName() + "\n Blocks: " + EnumChatFormatting.GREEN + t.getTownBlocks().size() + '\n';
			msg[i*3+1] = Formatter.formatResidentsToString(t.getResidents(), t) + "\n";
			msg[i*3+2] = Formatter.formatRanksToString(t.getRanks());
		}
		return msg;
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
