package mytown.x_commands.town.rank;

import mytown.MyTown;
import mytown.api.x_datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.x_command.CommandBase;
import mytown.core.utils.x_command.Permission;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Resident;
import mytown.x_entities.town.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.cmd.assistant.ranks.remove")
public class CmdRanksRemove extends CommandBase {

    public CmdRanksRemove(CommandBase parent) {
        super("remove", parent);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) throws CommandException {
        super.canCommandSenderUseCommand(sender);
        Resident res = getDatasource().getResident(sender.getCommandSenderName());

        if (res.getSelectedTown() == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
        if (!res.getTownRank().hasPermission(permNode))
            throw new CommandException("commands.generic.permission");

        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.ranks"));
        Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();
        if (!town.hasRankName(args[0]))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.rem.notexist", args[0], town.getName()));
        try {
            if (getDatasource().deleteRank(town.getRank(args[0]))) {
                ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.rem", args[0], town.getName());
            } else {
                ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.cmd.err.ranks.rem.notallowed", args[0]);
            }
        } catch (Exception e) {
            MyTown.instance.log.fatal(LocalizationProxy.getLocalization().getLocalization("mytown.databaseError"));
            e.printStackTrace();
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
