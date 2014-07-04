package mytown.commands.admin.config;

import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

/**
 * (Re)loads/saves config
 * 
 * @author Joe Goett
 */
@Permission("mytown.adm.cmd.config")
public class CmdConfig extends CommandHandler {
	public CmdConfig(ICommand parent) {
		super("config", parent);
		addSubCommand(new CmdConfigLoad(this));
		addSubCommand(new CmdConfigSave(this));
	}

	@Override
	public void sendHelp(ICommandSender sender) {
	}
}