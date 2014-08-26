package mytown.commands.town.plot;

import mytown.api.x_datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.plot.make")
public class CmdPlotMake extends CommandBase {

    public CmdPlotMake(String name, CommandBase parent) {
        super(name, parent);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return parent.canCommandSenderUseCommand(sender);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Resident res = getDatasource().getResident(sender.getCommandSenderName());
        String plotName = "NoName";

        if (args.length > 0) {
            plotName = args[0];
        }

        boolean result = res.makePlotFromSelection(plotName);
        if (result) {
            ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.created");
        } else
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.failed"));
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
