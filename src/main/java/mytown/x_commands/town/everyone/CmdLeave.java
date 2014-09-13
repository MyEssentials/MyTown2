package mytown.x_commands.town.everyone;

import mytown.MyTown;
import mytown.core.utils.x_command.CommandBase;
import mytown.core.utils.x_command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.resident.leave")
public class CmdLeave extends CommandBase {
    public CmdLeave(CommandBase parent) {
        super("leave", parent);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res == null)
            throw new CommandException("Resident is null"); // TODO Localize!

        Town town = res.getSelectedTown();
        if (town == null)
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.partOfTown"));

        // TODO Unlink the Resident from the Town

        res.sendMessage(MyTown.getLocal().getLocalization("mytown.notification.town.left.self", town.getName()));

        for (Resident r : town.getResidents()) {
            r.sendMessage(MyTown.getLocal().getLocalization("mytown.notification.town.left", res.getPlayerName(), town.getName()));
        }
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
