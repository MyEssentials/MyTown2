package mytown.commands.town.plot;

import mytown.api.x_datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.x_interfaces.ITownPlot;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.town.Town;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

@Permission("mytown.cmd.assistant.plot.rename")
public class CmdPlotRename extends CommandBase {

    public CmdPlotRename(String name, CommandBase parent) {
        super(name, parent);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return parent.canCommandSenderUseCommand(sender);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1)
            throw new WrongUsageException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.usage.plot.rename"));

        EntityPlayer player = (EntityPlayer) sender;
        Town town = getDatasource().getResident(sender.getCommandSenderName()).getSelectedTown();

        ITownPlot plot = town.getPlotAtCoords((int) player.posX, (int) player.posY, (int) player.posZ);
        if (plot == null)
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.rename.notexist"));

        plot.setName(args[0]);
        try {
            getDatasource().updatePlot(plot);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.plot.renamed"); // Maybe give more info about the plot?
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
