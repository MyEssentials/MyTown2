package mytown.commands.town.assistant;

import java.util.List;

import mytown.MyTown;
import mytown.commands.town.everyone.CmdInviteAccept;
import mytown.commands.town.everyone.CmdInviteRefuse;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

@Permission(node = "mytown.cmd.assistant.invite")
public class CmdInvite extends CommandHandler {

	public CmdInvite(String name, CommandBase parent) {
		super(name, parent);
		
		addSubCommand(new CmdInviteAccept("accept", this));
		addSubCommand(new CmdInviteRefuse("refuse", this));
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) throws CommandException {
		super.canCommandSenderUseCommand(sender);
		
		Resident res = MyTown.instance.datasource.getResident(sender.getCommandSenderName());
		
		if (res.getTowns().size() == 0) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.partOfTown"));
		if (!res.getTownRank().hasPermission(this.permNode)) throw new CommandException("commands.generic.permission");
		
		return true;
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		if(args.length != 0 && subCommands.containsKey(args[0]))
			super.process(sender, args);
		else
		{
			if (args.length < 1) {
				throw new WrongUsageException(MyTown.instance.local.getLocalization("mytown.cmd.usage.invite"));
			}
			if(!MyTown.instance.datasource.hasResident(args[0])) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.resident.notexist", args[0]));
			Town town = MyTown.instance.datasource.getResident(sender.getCommandSenderName()).getSelectedTown();
			if(town == null) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.partOfTown"));
			if(town.hasResident(args[0])) throw new CommandException(MyTown.instance.local.getLocalization("mytown.cmd.err.invtite.already", args[0], town.getName()));
		}
		// TODO: send request to player	
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
		return CommandUtils.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
	}
}