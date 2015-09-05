package mytown.handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mytown.MyTown;
import mytown.config.Config;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.blocks.Sign;
import mytown.entities.tools.Tool;
import mytown.proxies.DatasourceProxy;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
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


        for(Resident res : MyTownUniverse.instance.residents) {
            res.tick();
        }

        if((Config.costTownUpkeep > 0 || Config.costAdditionalUpkeep > 0) && ev.phase == TickEvent.Phase.START) {
            if (ticked) {
                if(lastCalendarDay != -1 && Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != lastCalendarDay) {
                    for (int i = 0; i < MyTownUniverse.instance.towns.size(); i++) {
                        Town town = MyTownUniverse.instance.towns.get(i);
                        if (!(town instanceof AdminTown)) {
                            town.bank.payUpkeep();
                            if(town.bank.getDaysNotPaid() == Config.upkeepTownDeletionDays && Config.upkeepTownDeletionDays > 0) {
                                MyTown.instance.LOG.info("Town {} has been deleted because it didn't pay upkeep for {} days.", town.getName(), Config.upkeepTownDeletionDays);
                                DatasourceProxy.getDatasource().deleteTown(town);
                            } else {
                                DatasourceProxy.getDatasource().saveTownBank(town.bank);
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
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        } else {
            MyTown.instance.LOG.error("Didn't create resident for player {} ({})", ev.player.getCommandSenderName(), ev.player.getPersistentID());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
        MyTownDatasource ds = DatasourceProxy.getDatasource();
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.player);
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

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        Tool currentTool = res.toolContainer.get();
        if (currentTool == null)
            return;

        if(currentStack == currentTool.getItemStack()) {
            ev.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote || ev.isCanceled())
            return;

        if(Tool.interact(ev))
            return;

        if(Sign.interact(ev))
            return;
    }


    @SubscribeEvent
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if (VisualsHandler.instance.isBlockMarked(ev.x, ev.y, ev.z, ev.world.provider.dimensionId, (EntityPlayerMP) ev.getPlayer())) {
            // Cancel event if it's a border that has been broken
            ev.setCanceled(true);
        }
        Sign sign = Sign.getSign(ev.world, ev.x, ev.y, ev.z);
        if(sign != null) {
            Resident resisdent = MyTownUniverse.instance.getOrMakeResident(ev.getPlayer());
            sign.onShiftRightClick(resisdent);
            ev.setCanceled(true);
        }
    }
}
