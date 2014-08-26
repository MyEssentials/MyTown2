package mytown.commands.town.everyone;

import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.resident.spawn")
public class CmdSpawn extends CommandBase {

    public CmdSpawn(CommandBase parent) {
        super("spawn", parent);
    }

    // TODO Re-implement canCommandSenderUseCommand

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (res == null)
            throw new CommandException("Failed to get or make Resident"); // TODO Localize!
        Town town = null;

        if (args.length == 0) {
            town = res.getSelectedTown();
        } else {
            town = getDatasource().getTownsMap().get(args[0]);
        }

        if (town == null) {
            throw new CommandException("Town doesn't exist!"); // TODO localize!
        } else { // TODO Check if the Resident is allowed to go to spawn
            if (!town.hasSpawn())
                throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.spawn.notexist", town.getName()));
            town.sendToSpawn(res);
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
