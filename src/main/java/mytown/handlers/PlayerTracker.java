package mytown.handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.core.utils.Log;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Block;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.protection.Protection;
import mytown.protection.Protections;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import mytown.util.Formatter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;

/**
 * @author Joe Goett
 */
public class PlayerTracker {
    private static Log log = MyTown.instance.log.createChild("PlayerTracker");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent ev) {
        MyTownDatasource ds = DatasourceProxy.getDatasource();
        Resident res = ds.getOrMakeResident(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        } else {
            log.warn("Didn't create resident for player %s (%s)", ev.player.getCommandSenderName(), ev.player.getPersistentID());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
        MyTownDatasource ds = DatasourceProxy.getDatasource();
        Resident res = ds.getOrMakeResident(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        }

    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent ev) {
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.player);
        res.checkLocationOnDimensionChanged(ev.player.chunkCoordX, ev.player.chunkCoordZ, ev.toDim);
    }

    @SubscribeEvent
    public void onEnterChunk(EntityEvent.EnteringChunk ev) {
        if (!(ev.entity instanceof EntityPlayer))
            return;
        checkLocationAndSendMap(ev);
    }

    private void checkLocationAndSendMap(EntityEvent.EnteringChunk ev) {
        if (ev.entity instanceof FakePlayer || ev.entity.worldObj.isRemote)
            return;
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entity);
        if (res == null) return; // TODO Log?
        // TODO Check Resident location

        res.checkLocation(ev.oldChunkX, ev.oldChunkZ, ev.newChunkX, ev.newChunkZ, ev.entity.dimension);

        if (res.isMapOn()) {
            Formatter.sendMap(res);
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
        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && currentStack.getItem().equals(Items.wooden_hoe) && currentStack.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
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



    @SubscribeEvent
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if(VisualsTickHandler.instance.isBlockMarked(ev.x, ev.y, ev.z, ev.world.provider.dimensionId)) {
            // Cancel event if it's a border that has been broken
            ev.setCanceled(true);
        }
    }




}
