package mytown.handlers;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mytown.MyTown;
import mytown.config.Config;
import mytown.new_datasource.MyTownDatasource;
import mytown.new_datasource.MyTownUniverse;
import mytown.entities.AdminTown;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.entity.player.EntityPlayerMP;
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

        if((Config.instance.costTownUpkeep.get() > 0 || Config.instance.costAdditionalUpkeep.get() > 0) && ev.phase == TickEvent.Phase.START) {
            if (ticked) {
                if(lastCalendarDay != -1 && Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != lastCalendarDay) {
                    for (int i = 0; i < MyTownUniverse.instance.towns.size(); i++) {
                        Town town = MyTownUniverse.instance.towns.get(i);
                        if (!(town instanceof AdminTown)) {
                            town.bank.payUpkeep();
                            if(town.bank.getDaysNotPaid() == Config.instance.upkeepTownDeletionDays.get() && Config.instance.upkeepTownDeletionDays.get() > 0) {
                                MyTown.instance.LOG.info("Town {} has been deleted because it didn't pay upkeep for {} days.", town.getName(), Config.instance.upkeepTownDeletionDays.get());
                                getDatasource().deleteTown(town);
                            } else {
                                getDatasource().saveTownBank(town.bank);
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
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        } else {
            MyTown.instance.LOG.error("Didn't create resident for player {} ({})", ev.player.getCommandSenderName(), ev.player.getPersistentID());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.player);
        if (res != null) {
            res.setPlayer(ev.player);
        }
    }

    @SubscribeEvent
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        if (VisualsHandler.instance.isBlockMarked(ev.x, ev.y, ev.z, ev.world.provider.dimensionId, (EntityPlayerMP) ev.getPlayer())) {
            // Cancel event if it's a border that has been broken
            ev.setCanceled(true);
        }
    }

    private MyTownDatasource getDatasource() {
        return MyTown.instance.datasource;
    }
}
