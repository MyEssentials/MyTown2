package mytown.commands.town.invite;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.proxies.DatasourceProxy;
import mytown.entities.Resident;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.cmd.outsider.invite.refuse")
public class CmdInviteRefuse extends CommandBase {

    public CmdInviteRefuse(CommandBase parent) {
        super("refuse", parent);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res.getInvites().size() == 0)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.noinvitations"));
        if (args.length == 0)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.invite.accept"));
        if (getDatasource().getTownsMap().get(args[0]) != null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
        String townName;
        if (args.length == 0) {
            townName = res.getInvites().get(0).getName();
        } else {
            townName = args[0];
        }
        if (!res.getInvites().contains(getDatasource().getTownsMap().get(townName)))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.accept"));
        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.invited.refuse", townName));
        res.removeInvite(townName);
    }

    /**
     * Helper method to return the current MyTownDatasource instance
     *
     * @return
     */
    private MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }

}
