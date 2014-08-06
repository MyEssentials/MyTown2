package mytown.commands.town.everyone;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

@Permission("mytown.cmd.outsider.select")
public class CmdSelect extends CommandBase {

	public CmdSelect(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.select"));
        if (!getDatasource().hasTown(args[0]))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
        Resident res = getDatasource().getOrMakeResident(((EntityPlayer)sender).getPersistentID(), true);
        if (res == null)
            throw new CommandException("Failed to get or make Resident"); // TODO Localize
        Town town = getDatasource().getTownsMap().get(args[0]);
        if (!town.hasResident(res))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.select.notpart", args[0]));
        res.selectTown(town);
        res.sendMessage(MyTown.getLocal().getLocalization("mytown.notification.town.select", args[0]));
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		try {
			Resident res = getDatasource().getOrMakeResident(((EntityPlayer)sender).getPersistentID());
			List<String> tabComplete = new ArrayList<String>();
			for (Town t : res.getTowns()) {
				if (args.length == 0 || t.getName().startsWith(args[0])) {
					tabComplete.add(t.getName());
				}
			}
			return tabComplete;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null; // If all else fails...
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
