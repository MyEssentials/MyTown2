package mytown.commands.town.rank;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownUniverse;
import mytown.proxies.DatasourceProxy;
import mytown.entities.Town;
import mytown.util.Formatter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.outsider.ranks")
public class CmdRanks extends CommandHandler {

    public CmdRanks(CommandBase parent) {
        super("ranks", parent);
        addSubCommand(new CmdRanksAdd(this));
        addSubCommand(new CmdRanksRemove(this));
        addSubCommand(new CmdRanksPerm(this));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length >= 1 && subCommands.containsKey(args[0])) {
            super.processCommand(sender, args);
        } else {
            Town temp = null;
            if (args.length < 1) {
                temp = getDatasource().getOrMakeResident(sender).getSelectedTown();
                if (temp == null)
                    throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.partOfTown"));
            }
            if (args.length >= 1) {
                temp = getUniverse().getTownsMap().get(args[0]);
                if (temp == null)
                    throw new CommandException(MyTown.getLocal().getLocalization("mytown.cmd.err.town.notexist", args[0]));
            }

            ChatUtils.sendLocalizedChat(sender, MyTown.getLocal(), "mytown.notification.town.ranks", Formatter.formatRanksToString(temp.getRanks()));
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
    private MyTownUniverse getUniverse() { return MyTownUniverse.getInstance(); }
}