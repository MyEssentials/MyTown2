package mytown.x_commands.town.plot.select;

import mytown.api.x_datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.x_command.CommandBase;
import mytown.core.utils.x_command.Permission;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Resident;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

@Permission("mytown.cmd.assistant.plot.select.expand")
public class CmdPlotSelectExpand extends CommandBase {

    public CmdPlotSelectExpand(String name, CommandBase parent) {
        super(name, parent);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return parent.canCommandSenderUseCommand(sender);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Resident res = getDatasource().getResident(sender.getCommandSenderName());

        if (!(res.isFirstPlotSelectionActive() && res.isSecondPlotSelectionActive()))
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.notSelected"));

        res.expandSelectionVert();

        ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.expanded");
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
