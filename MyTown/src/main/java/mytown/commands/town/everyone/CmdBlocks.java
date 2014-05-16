package mytown.commands.town.everyone;

import mytown.Formatter;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.blocks")
public class CmdBlocks extends CommandHandler {

	public CmdBlocks(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if (args.length == 1 && subCommands.containsKey(args[0]))
			super.process(sender, args);
		else {
			Resident res = MyTown.getDatasource().getResident(sender.getCommandSenderName());
			Town town;
			if (args.length == 0)
				if (res.getSelectedTown() == null)
					throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
				else
					town = res.getSelectedTown();
			else if (!MyTown.getDatasource().hasTown(args[0]))
				throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
			else
				town = res.getSelectedTown();

			ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.townblock.list", town.getName(), Formatter.formatTownBlocksToString(town.getTownBlocks(), false));
		}

	}

	@Override
	public void sendHelp(ICommandSender sender) {}
}