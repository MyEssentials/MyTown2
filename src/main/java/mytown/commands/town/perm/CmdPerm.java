package mytown.commands.town.perm;

import mytown.MyTown;
import mytown.api.x_datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.x_interfaces.ITownFlag;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Resident;
import mytown.x_entities.town.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

// TODO Move to new Datasource

@Permission("mytown.cmd.assistant.perm")
public class CmdPerm extends CommandHandler {

    public CmdPerm(CommandBase parent) {
        super("perm", parent);

        addSubCommand(new CmdPermSet(this));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        super.canCommandSenderUseCommand(sender);

        Resident res = getDatasource().getResident(sender.getCommandSenderName());

        if (res.getTowns().size() == 0)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        if (!res.getTownRank().hasPermission(permNode))
            throw new CommandException("commands.generic.permission");

        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            super.processCommand(sender, args);
        } else {
            Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
            String formattedFlagList = null;
            for (ITownFlag flag : town.getFlags()) {
                if (formattedFlagList == null) {
                    formattedFlagList = "";
                } else {
                    formattedFlagList += '\n';
                }
                formattedFlagList += flag;
            }
            ChatUtils.sendChat(sender, formattedFlagList);
        }
    }

    /**
     * Helper method to return the current MyTownDatasource instance
     *
     * @return
     */
    private MyTownDatasource getDatasource() {
        return X_DatasourceProxy.getDatasource();
    }

}
