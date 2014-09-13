package mytown.x_commands.town.info;

import mytown.MyTown;
import mytown.core.utils.x_command.CommandBase;
import mytown.core.utils.x_command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import mytown.util.Formatter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.ArrayList;
import java.util.List;

@Permission("mytown.cmd.outsider.info")
public class CmdInfo extends CommandBase {
    public CmdInfo(CommandBase parent) {
        super("info", parent);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        List<Town> towns = new ArrayList<Town>();

        Resident res = getDatasource().getOrMakeResident(sender);
        if (res == null)
            throw new CommandException("Failed to get/make Resident"); // TODO Localize
        if (args.length < 1) {
            if (res.getSelectedTown() != null) {
                towns.add(res.getSelectedTown());
            } else {
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.info.notpart"));
            }
        } else {
            if (args[0].equals("@a")) {
                towns = new ArrayList<Town>(getUniverse().getTownsMap().values());
                // TODO Sort
            } else if (getDatasource().hasTown(args[0])) {
                towns.add(getUniverse().getTownsMap().get(args[0]));
            } else {
                throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
            }
        }

        for (Town town : towns) {
            res.sendMessage(Formatter.formatTownInfo(town));
        }
    }

    @Override
    public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
        List<String> tabComplete = new ArrayList<String>();
        if (args.length == 0 || args[0].isEmpty()) {
            tabComplete.add("@a"); // Add the "all" selector
            tabComplete.addAll(getUniverse().getTownsMap().keySet());
        } else {
            for (Town t : getUniverse().getTownsMap().values()) {
                if (t.getName().startsWith(args[0])) {
                    tabComplete.add(t.getName());
                }
            }
        }
        return tabComplete;
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