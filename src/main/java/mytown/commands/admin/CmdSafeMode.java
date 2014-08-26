package mytown.commands.admin;

import mytown.core.ChatUtils;
import mytown.core.utils.Assert;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import mytown.handlers.SafemodeHandler;
import net.minecraft.command.ICommandSender;

import java.util.List;

/**
 * Command to enable/disable safemode
 *
 * @author Joe Goett
 */
@Permission("mytown.adm.cmd.safemode")
public class CmdSafeMode extends CommandBase {

    public CmdSafeMode(String name, CommandBase parent) {
        super(name, parent);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        boolean safemode = false;
        if (args.length < 1) { // Toggle safemode
            safemode = !SafemodeHandler.isInSafemode();
        } else { // Set safemode
            safemode = ChatUtils.equalsOn(args[0]);
        }
        Assert.Perm(sender, "mytown.adm.cmd.safemode." + (safemode ? "on" : "off"));
        SafemodeHandler.setSafemode(safemode);
        SafemodeHandler.kickPlayers();
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return CommandUtils.getListOfStringsMatchingLastWord(args, "on", "true", "enable", "off", "false", "disable");
    }
}