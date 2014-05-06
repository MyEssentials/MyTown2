package mytown.core.utils.command;

import java.util.List;

import mytown.core.utils.Assert;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

public abstract class CommandBase implements Command {
	protected String permNode;

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {

		Assert.Perm(sender, getPermNode(), canConsoleUse());
		return true;

	}

	public String getPermNode() {
		return permNode;
	}

	@Override
	public List<?> getCommandAliases() {
		return null;
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
		return null;
	}

	@Override
	public boolean canConsoleUse() {
		return false;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		return false;
	}

	public int compareTo(ICommand sender) {
		return getCommandName().compareTo(sender.getCommandName());
	}

	@Override
	public int compareTo(Object obj) {
		return this.compareTo((ICommand) obj);
	}
}
