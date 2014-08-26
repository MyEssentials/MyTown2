package mytown.commands.admin.config;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.core.utils.config.ConfigProcessor;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

/**
 * (Re)loads the config file
 *
 * @author Joe Goett
 */
@Permission("mytown.cmd.adm.config.load")
public class CmdConfigLoad extends CommandBase {
    public CmdConfigLoad(ICommand parent) {
        super("load", parent);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.config.load.start");
        ConfigProcessor.load(MyTown.instance.config, Config.class);
        ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.config.load.stop");
    }
}