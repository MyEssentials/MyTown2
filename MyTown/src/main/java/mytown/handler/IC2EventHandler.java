package mytown.handler;

import ic2.api.event.LaserEvent;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.proxies.DatasourceProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ForgeSubscribe;

/**
 * Created by AfterWind on 7/8/2014.
 * Event handler for IC2 events
 */
public class IC2EventHandler {
    @ForgeSubscribe
    public void onLaserBreak(LaserEvent.LaserHitsBlockEvent ev) {
        TownBlock tblock = DatasourceProxy.getDatasource().getTownBlock(ev.owner.dimension, ev.x, ev.z, false);
        if(tblock == null)
            return;
        if(!tblock.getTown().getFlagAtCoords(ev.x, ev.y, ev.z, "breakBlocks").getValue()) {
            if(ev.owner instanceof EntityPlayer) {
                Resident res = DatasourceProxy.getDatasource().getResident(((EntityPlayer) ev.owner).username);
                if(res == null || !res.getTowns().contains(tblock.getTown())) {
                    ev.setCanceled(true);
                    return;
                }
            } else {
                ev.setCanceled(true);
                return;
            }
        }
    }
    @ForgeSubscribe
    public void onLaserExplodes(LaserEvent.LaserExplodesEvent ev) {
        TownBlock tblock = DatasourceProxy.getDatasource().getTownBlock(ev.owner.dimension, ev.lasershot.chunkCoordX, ev.lasershot.chunkCoordZ, true);
        if(tblock == null)
            return;
        if(!tblock.getTown().getFlagAtCoords((int)ev.lasershot.posX, (int)ev.lasershot.posY, (int)Math.floor(ev.lasershot.posZ), "breakBlocks").getValue()) {
            if(ev.owner instanceof EntityPlayer) {
                Resident res = DatasourceProxy.getDatasource().getResident(((EntityPlayer) ev.owner).username);
                if(res == null || !res.getTowns().contains(tblock.getTown())) {
                    ev.setCanceled(true);
                    return;
                }
            } else {
                ev.setCanceled(true);
                return;
            }
        }
    }

}
