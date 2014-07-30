package mytown.x_handlers;

import com.google.common.base.Joiner;
import com.mojang.authlib.properties.Property;
import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.x_entities.Resident;
import mytown.x_entities.TownBlock;
import mytown.x_entities.town.Town;
import mytown.interfaces.ITownFlag;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import forgeperms.api.ForgePermsAPI;

import java.util.Collection;
import java.util.Map;

public class PlayerTracker {
    private static ItemStack myTownGuideBook = new ItemStack(Items.written_book);
    
    static { // TODO localize the book!
    	myTownGuideBook.setTagInfo("author", new NBTTagString("legobear154"));
    	myTownGuideBook.setTagInfo("title", new NBTTagString("MyTown Guide"));
    	NBTTagList pages = new NBTTagList();
    	pages.appendTag(new NBTTagString("Hello!\nThis server is using MyTown 2 for protecting YOUR stuff.\nThis book is a fairly lengthy book that is ment to help you use MyTown!"));
    	pages.appendTag(new NBTTagString(" Table of Contents\n"));
    	pages.appendTag(new NBTTagString(""));
    	// TODO Finish book :p
    	myTownGuideBook.setTagInfo("pages", pages);
    }
    
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent ev) {
		if (MyTown.instance.safemode && ev.player instanceof EntityPlayerMP && !ForgePermsAPI.permManager.canAccess(ev.player.getDisplayName(), ev.player.worldObj.provider.getDimensionName(), "mytown.adm.safemode")) {
			((EntityPlayerMP) ev.player).playerNetServerHandler.kickPlayerFromServer(Config.safeModeMsg);
			return;
		}

		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.player);
			res.setOnline(true);
			res.setPlayer(ev.player);
			if (!ev.player.getEntityData().getCompoundTag("MyTown").getBoolean("givenMyTownGuide")) {
				ev.player.inventory.addItemStackToInventory(myTownGuideBook);
				ev.player.getEntityData().getCompoundTag("MyTown").setBoolean("givenMyTownGuide", true);
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change later?
		}
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent ev) {
		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.player);
			res.setOnline(false);
			res.setPlayer(null);
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change later?
		}
	}

	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent ev) {
		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.player);
			res.checkLocation(ev.player.chunkCoordX, ev.player.chunkCoordZ, ev.player.dimension);
			if (res.isMapOn()) {
				res.sendMap();
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change?
		}
	}

	@SubscribeEvent
	public void onEnterChunk(EntityEvent.EnteringChunk ev) {
		if (!(ev.entity instanceof EntityPlayer))
			return;
		if (ev.entity.worldObj.isRemote)
			return; // So that it's not called twice :P
		EntityPlayer pl = (EntityPlayer) ev.entity;
		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(pl);
			res.checkLocation(ev.oldChunkX, ev.oldChunkZ, ev.newChunkX, ev.newChunkZ, pl.dimension);
			if (res.isMapOn()) {
				res.sendMap();
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change?
		}
	}

	@SubscribeEvent
	public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
		// TODO: Implement wilderness perms too
		if (!DatasourceProxy.getDatasource().hasTownBlock(String.format(TownBlock.keyFormat, ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4)))
			return;
		else {
			Town town = DatasourceProxy.getDatasource().getTownBlock(String.format(TownBlock.keyFormat, ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4)).getTown();
			ITownFlag flag = town.getFlagAtCoords(ev.x, ev.y, ev.z, "breakBlocks");
			if (flag == null)
				return;
			if (flag.getValue() == true)
				return;
			if (DatasourceProxy.getDatasource().getResident(ev.getPlayer().getDisplayName()).isPartOfTown(town))
				return;
			ev.setCanceled(true);
		}
	}

	// Because I can
	@SubscribeEvent
	public void onUseHoe(UseHoeEvent ev) {
		if (ev.current.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
			ev.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onItemUse(PlayerInteractEvent ev) {
		if (ev.entityPlayer.worldObj.isRemote)
			return;

		ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();
		if (currentStack == null)
			return;
		if (ev.action == Action.RIGHT_CLICK_BLOCK && currentStack.getItem().equals(Items.wooden_hoe) && currentStack.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
			Resident res = DatasourceProxy.getDatasource().getResident(ev.entityPlayer.getDisplayName());
			if (res == null)
				return;

			if (res.isFirstPlotSelectionActive() && res.isSecondPlotSelectionActive()) {
				ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.alreadySelected");
			} else {
				boolean result = res.selectBlockForPlot(ev.entityPlayer.dimension, ev.x, ev.y, ev.z);
				if (result) {
					if (!res.isSecondPlotSelectionActive()) {
						ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.selectionStart");
					} else {
						ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.selectionEnd");
					}
				} else
					ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.selectionFailed");

			}
			System.out.println(String.format("Player has selected: %s;%s;%s", ev.x, ev.y, ev.z));
		}
	}

}