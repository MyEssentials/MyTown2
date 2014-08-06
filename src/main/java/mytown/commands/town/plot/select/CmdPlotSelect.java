package mytown.commands.town.plot.select;

import mytown.MyTown;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.ChatUtils;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;
import mytown.proxies.X_DatasourceProxy;
import mytown.x_entities.Resident;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;

@Permission("mytown.cmd.assistant.plot.select")
public class CmdPlotSelect extends CommandHandler {
	public static ItemStack selectionTool;

	public CmdPlotSelect(String name, CommandBase parent) {
		super(name, parent);

		addSubCommand(new CmdPlotSelectExpand("expandVert", this));
		addSubCommand(new CmdPlotSelectReset("reset", this));
		
		// Create selection tool TODO Localize the tool
		selectionTool = new ItemStack(Items.wooden_hoe);
		selectionTool.setStackDisplayName(Constants.EDIT_TOOL_NAME);
		NBTTagList lore = new NBTTagList();
		lore.appendTag(new NBTTagString(EnumChatFormatting.DARK_AQUA + "Select 2 blocks to make a plot."));
		lore.appendTag(new NBTTagString(EnumChatFormatting.DARK_AQUA + "Uses: 1"));
		selectionTool.getTagCompound().getCompoundTag("display").setTag("Lore", lore);
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
		if (args.length > 0) {
			super.processCommand(sender, args);
		} else {
			EntityPlayer player = (EntityPlayer) sender;

			boolean ok = !player.inventory.hasItemStack(selectionTool);
			boolean result = false;
			if (ok) {
				result = player.inventory.addItemStackToInventory(selectionTool);
			}
			if (result) {
				ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.start");
			} else if (ok) {
				ChatUtils.sendLocalizedChat(sender, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.start.failed");
			}
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
