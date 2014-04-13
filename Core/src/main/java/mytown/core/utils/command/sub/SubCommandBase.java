package mytown.core.utils.command.sub;

import java.util.List;

import mytown.core.utils.Assert;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public abstract class SubCommandBase implements SubCommand {
	@Override
	public void canUse(ICommandSender sender) throws CommandException {
		Assert.Perm(sender, getPermNode(), canUseByConsole());
	}

	@Override
	public boolean canUseByConsole() {
		return false;
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return null;
	}

	public String getDesc(ICommandSender sender) {
		return "";
	}

	public String getArgs(ICommandSender sender) {
		return "";
	}
}