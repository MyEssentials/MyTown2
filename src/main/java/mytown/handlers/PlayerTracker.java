package mytown.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import mytown.MyTown;
import mytown.core.utils.Log;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.util.Formatter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * @author Joe Goett
 */
public class PlayerTracker {
    private static Log log = MyTown.instance.log.createChild("PlayerTracker");

    @SubscribeEvent
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
        checkLocationAndSendMap(ev.player);
    }

    @SubscribeEvent
    public void onEnterChunk(EntityEvent.EnteringChunk ev) {
        if (!(ev.entity instanceof EntityPlayer))
            return;
        checkLocationAndSendMap((EntityPlayer) ev.entity);
    }

    private void checkLocationAndSendMap(EntityPlayer pl) {
        if (pl instanceof FakePlayer || pl.worldObj.isRemote)
            return;
        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(pl);
        if (res == null) return; // TODO Log?
        // TODO Check Resident location
        if (res.isMapOn()) {
            Formatter.sendMap(res);
        }
    }
}
