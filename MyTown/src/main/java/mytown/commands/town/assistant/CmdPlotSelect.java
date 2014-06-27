package mytown.commands.town.assistant;

import mytown.Constants;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Permission("mytown.cmd.assistant.plot.select")
public class CmdPlotSelect extends CommandHandler {

	public CmdPlotSelect(String name, CommandBase parent) {
		super(name, parent);

		addSubCommand(new CmdPlotSelectExpand("expandVert", this));
		addSubCommand(new CmdPlotSelectReset("reset", this));
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
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
		if (args.length > 0)
			super.processCommand(sender, args);
		else {
			EntityPlayer player = (EntityPlayer) sender;

			ItemStack stack = new ItemStack(Item.hoeWood);
			stack.setItemName(Constants.EDIT_TOOL_NAME);
			boolean ok = true;
			for (ItemStack st : player.inventory.mainInventory)
				if (st != null && st.getDisplayName().equals(Constants.EDIT_TOOL_NAME) && st.itemID == Item.hoeWood.itemID) {
					ok = false;
				}
			boolean result = false;
			if (ok) {
				result = player.inventory.addItemStackToInventory(stack);
			}
			if (result)
				ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.start");
			else if (ok)
				ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.start.failed");
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

	@Override
	public void sendHelp(ICommandSender sender) {
		// TODO Auto-generated method stub

	}
}
