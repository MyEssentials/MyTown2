package mytown.commands.town.perm;

import mytown.MyTown;
import mytown.api.x_datasource.MyTownDatasource;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Rank;
import mytown.x_entities.Resident;
import mytown.x_entities.town.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

// TODO Move to new Datasource

@Permission("mytown.cmd.assistant.promote")
public class CmdPromote extends CommandBase {
    public CmdPromote(CommandBase parent) {
        super("promote", parent);
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
        // /t promote <user> <rank>
        if (args.length < 2)
            throw new WrongUsageException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.usage.promote"));
        Resident resSender = getDatasource().getResident(sender.getCommandSenderName());
        Resident resTarget = getDatasource().getResident(args[0]);
        Town town = resSender.getSelectedTown();

        if (resTarget == null)
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.resident.notexist", args[0]));
        if (!resTarget.getTowns().contains(town))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.resident.notsametown", args[0], town.getName()));
        if (!resSender.getSelectedTown().hasRankName(args[1]))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.rank.notexist", args[1], town.getName()));
        if (args[1].equalsIgnoreCase("mayor"))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.promote.notMayor"));
        try {
            Rank rank = town.getRank(args[1]);
            town.promoteResident(resTarget, rank);
            getDatasource().updateLinkResidentToTown(resTarget, town);
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
