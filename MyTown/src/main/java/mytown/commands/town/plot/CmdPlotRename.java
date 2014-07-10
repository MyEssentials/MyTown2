package mytown.commands.town.plot;

import mytown.api.datasource.MyTownDatasource;
import mytown.chat.channels.types.Local;
import mytown.core.ChatUtils;
import mytown.core.Localization;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Resident;
import mytown.entities.town.Town;
import mytown.interfaces.ITownPlot;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;

import java.util.zip.CheckedOutputStream;

@Permission("mytown.cmd.resident.plot.rename")
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
        Resident resident = getDatasource().getResident(sender.getCommandSenderName());
		ITownPlot plot = town.getPlotAtCoords((int) player.posX, (int) player.posY, (int) player.posZ);
		if (plot == null)
			throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.rename.notexist"));
        if(!plot.getOwners().contains(resident)) // Being part of town is checked on adding to the owners list
            throw new CommandException(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.notowner"));

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
		return DatasourceProxy.getDatasource();
	}
}
