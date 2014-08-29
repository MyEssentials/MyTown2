package mytown.x_commands.admin.config;

import mytown.core.utils.x_command.CommandHandler;
import mytown.core.utils.x_command.Permission;
import net.minecraft.command.ICommand;

/**
 * (Re)loads/saves config
 *
 * @author Joe Goett
 */
@Permission("mytown.cmd.adm.config")
public class CmdConfig extends CommandHandler {
    public CmdConfig(ICommand parent) {
        super("config", parent);
        addSubCommand(new CmdConfigLoad(this));
        addSubCommand(new CmdConfigSave(this));
    }
}