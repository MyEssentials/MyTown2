package mytown;

import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.town.Town;
import mytown.interfaces.ITownFlag;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.common.IPlayerTracker;
import forgeperms.api.ForgePermsAPI;

public class PlayerTracker implements IPlayerTracker {
	@Override
	public void onPlayerLogin(EntityPlayer player) {
		if (player == null) return; // Never know ;)
		if (MyTown.instance.safemode && player instanceof EntityPlayerMP && !ForgePermsAPI.permManager.canAccess(player.username, player.worldObj.provider.getDimensionName(), "mytown.adm.safemode")) {
			((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer(Config.safeModeMsg);
			return;
		}

		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(player);
			res.setOnline(true);
			res.setPlayer(player);
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change later?
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		if (player == null) return; // Never know ;)
		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(player);
			res.setOnline(false);
			res.setPlayer(null);
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change later?
		}
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer pl) {
		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(pl);
			res.checkLocation(pl.chunkCoordX, pl.chunkCoordZ, pl.dimension);
			if (res.isMapOn()) res.sendMap();
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change?
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		// TODO Auto-generated method stub
	}
	@ForgeSubscribe
	public void onEnterChunk(EntityEvent.EnteringChunk ev) {
		if (!(ev.entity instanceof EntityPlayer)) return;
		if(ev.entity.worldObj.isRemote) return; // So that it's not called twice :P
		EntityPlayer pl = (EntityPlayer) ev.entity;
		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(pl);
			res.checkLocation(ev.oldChunkX, ev.oldChunkZ, ev.newChunkX, ev.newChunkZ, pl.dimension);
			if (res.isMapOn()) res.sendMap();
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change?
		}
	}
	@ForgeSubscribe
	public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
		// TODO: Implement wilderness perms too
		if(!DatasourceProxy.getDatasource().hasTownBlock(String.format(TownBlock.keyFormat, ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4))) return;
		else {
			Town town = DatasourceProxy.getDatasource().getTownBlock(String.format(TownBlock.keyFormat, ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4)).getTown();
			ITownFlag flag = town.getFlagAtCoords(ev.x, ev.y, ev.z, "breakBlocks");
			if(flag == null)
				return;
			if(flag.getValue() == true)
				return;
			if(DatasourceProxy.getDatasource().getResident(ev.getPlayer().username).isPartOfTown(town))
				return;
			ev.setCanceled(true);
		}
	}
	
	@ForgeSubscribe
	public void onItemToolTip(ItemTooltipEvent ev) {
		if(ev.itemStack.getDisplayName().equals(EnumChatFormatting.BLUE + "Selector") && ev.itemStack.getItem() == Item.hoeWood) {
			ev.toolTip.add(EnumChatFormatting.DARK_AQUA + "Select 2 blocks to make a plot.");
			ev.toolTip.add(EnumChatFormatting.DARK_AQUA + "Uses: 1");
		}
	}
	
	// Because I can
	@ForgeSubscribe
	public void onUseHoe(UseHoeEvent ev) {
		if(ev.current.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
			ev.setCanceled(true);
		}
	}
	
	@ForgeSubscribe
	public void onItemUse(PlayerInteractEvent ev) {
		if(ev.entityPlayer.worldObj.isRemote) return;
		ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();
		if(ev.action == Action.RIGHT_CLICK_BLOCK && currentStack.getItem().equals(Item.hoeWood) && currentStack.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
			Resident res = DatasourceProxy.getDatasource().getResident(ev.entityPlayer.username);
			if(res == null) return;
			if(!res.isFirstPlotSelectionActive()) {
				boolean result = res.setFirstPlotSelectionAndCheck(ev.entityPlayer.dimension, ev.x, ev.y, ev.z);
				if(result)
					ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.selected");
				else
					ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.notintown");
			} else {
				boolean result = res.setSecondPlotSelectionAndCheck(ev.entityPlayer.dimension, ev.x, ev.y, ev.z);
				if(result)
					ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.created");
				else
					ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.notintown");
			}
			System.out.println(String.format("Player has selected: %s;%s;%s", ev.x, ev.y, ev.z));
		}
	}
	

}