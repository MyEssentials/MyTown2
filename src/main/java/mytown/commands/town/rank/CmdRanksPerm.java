package mytown.commands.town.rank;

import mytown.MyTown;
import mytown.api.x_datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Rank;
import mytown.x_entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.ranks.perm")
public class CmdRanksPerm extends CommandHandler {

    public CmdRanksPerm(CommandBase parent) {
        super("perm", parent);

        addSubCommand(new CmdRanksPermAdd(this));
        addSubCommand(new CmdRanksPermRemove(this));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
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
        if (args.length >= 1) {
            super.processCommand(sender, args);
        } else {
            Rank rank;
            if (args.length == 0) {
                rank = getDatasource().getResident(sender.getCommandSenderName()).getTownRank();
            } else {
                rank = getDatasource().getRank(args[0], getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown());
            }

            if (rank == null)
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.notexist", args[0], getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown().getName()));

            String msg = "";
            for (String s : rank.getPermissions()) {
                msg += '\n' + s;
            }
            ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.perm.list", rank.getName(), rank.getTown().getName(), msg);
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
