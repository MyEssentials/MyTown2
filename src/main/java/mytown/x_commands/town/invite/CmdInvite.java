package mytown.x_commands.town.invite;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.core.utils.x_command.CommandBase;
import mytown.core.utils.x_command.CommandHandler;
import mytown.core.utils.x_command.CommandUtils;
import mytown.core.utils.x_command.Permission;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import mytown.entities.Resident;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

import java.util.List;

// TODO Move to new Datasource

@Permission("mytown.cmd.assistant.invite")
public class CmdInvite extends CommandHandler {

    public CmdInvite(CommandBase parent) {
        super("invite", parent);

        addSubCommand(new CmdInviteAccept(this));
        addSubCommand(new CmdInviteRefuse(this));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 0 && subCommands.containsKey(args[0])) {
            super.processCommand(sender, args);
        } else {
            Resident res = getDatasource().getOrMakeResident(sender);
            if (res == null)
                throw new CommandException("Failed to get/make Resident"); // TODO Localize
            if (res.getTowns().size() == 0)
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
            if (!res.getTownRank().hasPermission(permNode))
                throw new CommandException("commands.generic.permission");
            if (args.length < 1)
                throw new WrongUsageException(MyTown.getLocal().getLocalization("mytown.cmd.usage.invite"));
            if (!getDatasource().hasResident(args[0]))
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.resident.notexist", args[0]));

            Town town = res.getSelectedTown();
            if (town == null)
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
            if (town.hasResident(args[0]))
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.invite.already", args[0], town.getName()));

            Resident target = getDatasource().getOrMakeResident(args[0]);
            target.addInvite(town);
            target.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.invited", town.getName()));
            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.sent", args[0]));
        }
    }

    @Override
    public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return CommandUtils.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
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