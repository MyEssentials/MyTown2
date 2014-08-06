package mytown.commands.town.info;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.proxies.DatasourceProxy;
import mytown.util.Formatter;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

@Permission("mytown.cmd.assistant.blocks")
public class CmdBlocks extends CommandHandler {

	public CmdBlocks(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args.length == 1) {
			super.processCommand(sender, args);
		} else {
            EntityPlayer pl = (EntityPlayer) sender;
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(pl.getPersistentID());
			Town town;
			if (args.length == 0)
				if (res.getSelectedTown() == null)
					throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
				else {
					town = res.getSelectedTown();
				}
			else if (!DatasourceProxy.getDatasource().hasTown(args[0]))
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
			else {
				town = res.getSelectedTown();
			}

			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.townblock.list", town.getName(), ""); // Formatter.formatTownBlocksToString(town.getBlocks(), true)  TODO Get block list
		}

	}
}
