package mytown.handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mytown.MyTown;
import mytown.config.Config;
import myessentials.utils.PlayerUtils;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.tools.Tool;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.EconomyProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import mytown.util.MyTownUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Calendar;

public class Ticker {

    private boolean ticked = true;
    private int lastCalendarDay = -1;
    @SubscribeEvent
    public void onTickEvent(TickEvent.WorldTickEvent ev) {
        if(ev.side == Side.CLIENT)
            return;


        for(Resident res : MyTownUniverse.instance.getResidentsMap().values()) {
            res.tick();
        }

        if((Config.costTownUpkeep > 0 || Config.costAdditionalUpkeep > 0) && ev.phase == TickEvent.Phase.START) {
            if (ticked) {
                if(lastCalendarDay != -1 && Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != lastCalendarDay) {
                    for (int i = 0; i < MyTownUniverse.instance.getTownsMap().size(); i++) {
                        Town town = MyTownUniverse.instance.getTownsMap().values().asList().get(i);
                        if (!(town instanceof AdminTown)) {
                            town.payUpkeep();
                            if(town.getDaysNotPaid() == Config.upkeepTownDeletionDays && Config.upkeepTownDeletionDays > 0) {
                                MyTown.instance.LOG.info("Town {} has been deleted because it didn't pay upkeep for {} days.", town.getName(), Config.upkeepTownDeletionDays);
                                DatasourceProxy.getDatasource().deleteTown(town);
                            } else {
                                DatasourceProxy.getDatasource().updateTownBank(town, town.getBankAmount());
                            }
                        }
                    }
                    ticked = false;
                }
                lastCalendarDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            } else {
                ticked = true;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent ev) {
        MyTownDatasource ds = DatasourceProxy.getDatasource();
        Resident res = ds.getOrMakeResident(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        } else {
            MyTown.instance.LOG.error("Didn't create resident for player {} ({})", ev.player.getCommandSenderName(), ev.player.getPersistentID());
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

    // Because I can
    @SubscribeEvent
    public void onUseHoe(UseHoeEvent ev) {
        ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();
        if (currentStack == null)
            return;

        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
        Tool currentTool = res.getCurrentTool();
        if (currentTool == null)
            return;

        if(currentStack == currentTool.getItemStack()) {
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

        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
            Tool currentTool = res.getCurrentTool();
            if(currentTool == null)
                return;
            if(currentTool.getItemStack() == currentStack) {
                if (ev.entityPlayer.isSneaking()) {
                    currentTool.onShiftRightClick();
                } else if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                    currentTool.onItemUse(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, ev.face);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if (VisualsHandler.instance.isBlockMarked(ev.x, ev.y, ev.z, ev.world.provider.dimensionId, (EntityPlayerMP) ev.getPlayer())) {
            // Cancel event if it's a border that has been broken
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent ev) {
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
        Block block = ev.world.getBlock(ev.x, ev.y, ev.z);

        // Shop and plot sale click verify
        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {

            if (block == Blocks.wall_sign || block == Blocks.standing_sign) {
                TileEntitySign te = (TileEntitySign) ev.world.getTileEntity(ev.x, ev.y, ev.z);

                if(te.signText[1].equals(Constants.PLOT_SELL_IDENTIFIER)) {
                    if (ev.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && ev.entityPlayer.isSneaking() && PlayerUtils.isOp(ev.entityPlayer)) {
                        ev.world.setBlock(ev.x, ev.y, ev.z, Blocks.air);
                    } else if(ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                        Town town = MyTownUtils.getTownAtPosition(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
                        if(town != null) {
                            if(town.hasResident(res)) {
                                Plot plot = town.getPlotAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z);
                                if(plot != null) {
                                    if(!plot.hasOwner(res)) {
                                        if (town.canResidentMakePlot(res)) {
                                            int price = Integer.parseInt(te.signText[2].substring(2, te.signText[2].length()));
                                            if (EconomyProxy.getEconomy().takeMoneyFromPlayer(ev.entityPlayer, price)) {
                                                for(Resident resInPlot : plot.getOwners()) {
                                                    resInPlot.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.buy.oldOwner", plot.getName()));
                                                }
                                                for(Resident resInPlot : plot.getResidents()) {
                                                    DatasourceProxy.getDatasource().unlinkResidentFromPlot(resInPlot, plot);
                                                }
                                                DatasourceProxy.getDatasource().linkResidentToPlot(res, plot, true);
                                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.buy.newOwner", plot.getName()));
                                                ev.world.setBlock(ev.x, ev.y, ev.z, Blocks.air);
                                            } else {
                                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.buy.failed", EconomyProxy.getCurrency(price)));
                                            }
                                        } else {
                                            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.limit", town.getMaxPlots()));
                                        }
                                    } else {
                                        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.sell.alreadyOwner"));
                                    }
                                }
                            } else {
                                res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.notInTown", town.getName()));
                            }
                        }
                    }
                    ev.setCanceled(true);
                }
            }
        }
    }

}
