package mytown.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import mytown.MyTown;
import mytown.core.utils.Log;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;

/**
 * @author Joe Goett
 */
public class PlayerTracker {
    private static Log log = MyTown.instance.log.createChild("PlayerTracker");

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent ev) {
        MyTownDatasource ds = null; // TODO Get MyTownDatasource
        Resident res = ds.getOrMakeResident(ev.player.getPersistentID().toString());
        if (res != null) {
            res.setPlayer(ev.player);
        } else {
            log.warn("Didn't create resident for player %s (%s)", ev.player.getCommandSenderName(), ev.player.getPersistentID());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent ev) {
        MyTownDatasource ds = null; // TODO Get MyTownDatasource
        Resident res = ds.getResidentsMap().get(ev.player.getPersistentID().toString());
        if (res != null) {
            res.setPlayer(null);
        }
    }
}
