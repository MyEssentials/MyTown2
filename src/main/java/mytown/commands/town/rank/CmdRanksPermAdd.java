package mytown.commands.town.rank;

import mytown.MyTown;
import mytown.api.x_datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Resident;
import mytown.x_entities.town.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

@Permission("mytown.cmd.assistant.ranks.perm.add")
public class CmdRanksPermAdd extends CommandBase {

    public CmdRanksPermAdd(CommandBase parent) {
        super("add", parent);
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
        if (args.length < 2)
            throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.ranks.perm"));

        Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();

        if (getDatasource().getRank(args[0], town) == null)
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.rem.notexist", args[0], town.getName()));
        /*
        FIXME
        if (!CommandUtils.permissionList.containsValue(args[1]))
            throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.perm.notexist", args[1]));
        */

        try {
            // Adding permission if everything is alright
            if (town.getRank(args[0]).addPermission(args[1])) {
                getDatasource().updateRank(town.getRank(args[0]));
                ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks.perm.add", args[1], args[0]);
            } else
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.ranks.perm.add.failed", args[1]));
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