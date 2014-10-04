package mytown.handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import mytown.MyTown;
import mytown.api.events.TownEvent;
import mytown.core.ChatUtils;
import mytown.core.utils.Log;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.protection.Protection;
import mytown.protection.Protections;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import mytown.util.Formatter;
import mytown.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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

        Town lastTown = Utils.getTownAtPosition(ev.entity.dimension, ev.oldChunkX, ev.oldChunkZ);
        Town currTown = Utils.getTownAtPosition(ev.entity.dimension, ev.newChunkX, ev.newChunkZ);

        if(currTown != null && (lastTown == null || currTown != lastTown))
            TownEvent.fire(new TownEvent.TownEnterEvent(currTown, res));
        
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
        if((ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) && ev.entityPlayer.isSneaking()) {
            if(currentStack.getItem().equals(Items.wooden_hoe) && currentStack.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
                // For shift right clicking the selector, we may need it
            }
        }
        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && currentStack.getItem().equals(Items.wooden_hoe) && currentStack.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
            Town town;
            //TODO: Verify permission

            NBTTagList lore = currentStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
            String description = lore.getStringTagAt(0);

            if(description.equals(Constants.EDIT_TOOL_DESCRIPTION_PLOT)) {
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
            } else if (description.equals(Constants.EDIT_TOOL_DESCRIPTION_BLOCK_WHITELIST)) {
                town = MyTownUniverse.getInstance().getTownsMap().get(Utils.getTownNameFromLore(ev.entityPlayer));
                Town townAt = Utils.getTownAtPosition(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
                if(town == null || town != townAt) {
                    res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.blockNotInTown"));
                    return;
                } else {
                    // If town is found then create of delete the block whitelist

                    FlagType flagType = FlagType.valueOf(Utils.getFlagNameFromLore(ev.entityPlayer));
                    ev.entityPlayer.setCurrentItemOrArmor(0, null);
                    BlockWhitelist bw = town.getBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, flagType);
                    if(bw == null) {
                        bw = new BlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, flagType);
                        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.perm.town.whitelist.added"));
                        DatasourceProxy.getDatasource().saveBlockWhitelist(bw, town);
                    } else {
                        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.perm.town.whitelist.removed"));
                        DatasourceProxy.getDatasource().deleteBlockWhitelist(bw, town);
                    }
                    ev.setCanceled(true);
                }
            }
        }
    }


    @SubscribeEvent
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if (VisualsTickHandler.instance.isBlockMarked(ev.x, ev.y, ev.z, ev.world.provider.dimensionId)) {
            // Cancel event if it's a border that has been broken
            ev.setCanceled(true);
        }
    }
}
