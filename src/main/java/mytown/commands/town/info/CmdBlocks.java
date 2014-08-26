package mytown.commands.town.info;

import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.blocks")
public class CmdBlocks extends CommandHandler {

    public CmdBlocks(CommandBase parent) {
        super("blocks", parent);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            super.processCommand(sender, args);
        } else {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(sender);
            Town town;
            if (args.length == 0)
                if (res.getSelectedTown() == null)
                    throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
                else {
                    town = res.getSelectedTown();
                }
            else if (!DatasourceProxy.getDatasource().hasTown(args[0]))
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
            else {
                town = res.getSelectedTown();
            }

            ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.townblock.list", town.getName(), ""); // Formatter.formatTownBlocksToString(town.getBlocks(), true)  TODO Get block list
        }

    }
}
