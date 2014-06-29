package mytown.commands.admin.config;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;

/**
 * (Re)loads/saves config
 * 
 * @author Joe Goett
 */
@Permission("mytown.cmd.adm.config")
public class CmdConfig extends CommandHandler {
	public CmdConfig(ICommand parent) {
		super("config", parent);
		this.addSubCommand(new CmdConfigLoad(this));
		this.addSubCommand(new CmdConfigSave(this));
	}

	@Override
	public void sendHelp(ICommandSender sender) {
	}
}