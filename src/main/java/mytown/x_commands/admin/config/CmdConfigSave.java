package mytown.x_commands.admin.config;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.utils.x_command.CommandBase;
import mytown.core.utils.x_command.Permission;
import mytown.core.utils.config.ConfigProcessor;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

/**
 * Saves the config file
 *
 * @author Joe Goett
 */
@Permission("mytown.cmd.adm.config.save")
public class CmdConfigSave extends CommandBase {
    public CmdConfigSave(ICommand parent) {
        super("save", parent);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.config.save.start");
        ConfigProcessor.save(MyTown.instance.config, Config.class);
        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.config.save.stop");
    }
}