package mytown.x_commands.town.invite;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.core.utils.x_command.CommandBase;
import mytown.core.utils.x_command.Permission;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import mytown.entities.Resident;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.outsider.invite.accept")
public class CmdInviteAccept extends CommandBase {

    public CmdInviteAccept(CommandBase parent) {
        super("accept", parent);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res.getInvites().isEmpty())
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.noinvitations"));
        if (getUniverse().getTownsMap().get(args[0]) == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
        String townName;
        if (args.length == 0) {
            townName = res.getInvites().get(0).getName();
        } else {
            townName = args[0];
        }
        if (!res.getInvites().contains(getUniverse().getTownsMap().get(townName)))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.usage.invite"));
        Town t = res.getInvite(townName);
        res.removeInvite(t);

        // Notify everyone
        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.invited.accept", townName));
        t.notifyResidentJoin(res);

        // Link Resident to Town
        t.addResident(res);
        res.addTown(t);
        getDatasource().saveTown(t);
    }

    /**
     * Helper method to return the current MyTownDatasource instance
     *
     * @return
     */
    private MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
    private MyTownUniverse getUniverse() { return MyTownUniverse.getInstance(); }

}
