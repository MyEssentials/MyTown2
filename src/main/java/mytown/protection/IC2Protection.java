package mytown.protection;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.MyTown;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import net.minecraft.entity.Entity;

import ic2.api.event.LaserEvent;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by AfterWind on 7/8/2014.
 * <p/>
 * IC2 mod implementation here
 */
public class IC2Protection extends Protection {

    @SuppressWarnings("unchecked")
    public IC2Protection() {
        isHandlingEvents = true;
        try {

            this.explosiveBlocks.add((Class<? extends Entity>) Class.forName("ic2.core.block.EntityNuke"));
            this.explosiveBlocks.add((Class<? extends Entity>) Class.forName("ic2.core.block.EntityItnt"));
            this.explosiveBlocks.add((Class<? extends Entity>) Class.forName("ic2.core.block.EntityDynamite"));
            this.explosiveBlocks.add((Class<? extends Entity>) Class.forName("ic2.core.block.EntityStickyDynamite"));

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    // EVENTS
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onLaserBreak(LaserEvent.LaserHitsBlockEvent ev) {
        MyTown.instance.log.info("Detected laser break.");
        TownBlock tblock = DatasourceProxy.getDatasource().getBlock(ev.owner.dimension, ev.x >> 4, ev.z >> 4);
        if(tblock == null)
            return;
        if(ev.owner instanceof EntityPlayer) {
            Resident res = getDatasource().getOrMakeResident(ev.owner);
            //TODO: Check for permission node
            if (!tblock.getTown().checkPermission(res, FlagType.modifyBlocks, ev.world.provider.dimensionId, ev.x, ev.y, ev.z)) {
                ev.setCanceled(true);
                ev.lasershot.setDead();
                res.sendMessage(FlagType.modifyBlocks.getLocalizedProtectionDenial());
            }
        } else {
            // Verifying only the flag itself, not for resident
            boolean breakFlag = (Boolean)tblock.getTown().getValueAtCoords(ev.world.provider.dimensionId, (int)ev.lasershot.posX, (int)ev.lasershot.posY, (int)ev.lasershot.posZ, FlagType.modifyBlocks);
            if(!breakFlag) {
                ev.setCanceled(true);
                ev.lasershot.setDead();
                tblock.getTown().notifyEveryone(FlagType.modifyBlocks.getLocalizedTownNotification());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onLaserExplodes(LaserEvent.LaserExplodesEvent ev) {
        MyTown.instance.log.info("Detected explosion.");
        TownBlock tblock = DatasourceProxy.getDatasource().getBlock(ev.owner.dimension, ev.lasershot.chunkCoordX, ev.lasershot.chunkCoordZ);
        if(tblock == null)
            return;
        if(ev.owner instanceof EntityPlayer) {
            Resident res = getDatasource().getOrMakeResident(ev.owner);
            //TODO: Check for permission node
            if (!tblock.getTown().checkPermission(res, FlagType.modifyBlocks, ev.world.provider.dimensionId, (int)ev.lasershot.posX, (int)ev.lasershot.posY, (int)ev.lasershot.posZ)) {
                ev.setCanceled(true);
                ev.lasershot.setDead();
                res.sendMessage(FlagType.modifyBlocks.getLocalizedProtectionDenial());
            }
        } else {
            // Verifying only the flag itself, not for resident
            boolean breakFlag = (Boolean)tblock.getTown().getValueAtCoords(ev.world.provider.dimensionId, (int)ev.lasershot.posX, (int)ev.lasershot.posY, (int)ev.lasershot.posZ, FlagType.modifyBlocks);
            if(!breakFlag) {
                ev.setCanceled(true);
                ev.lasershot.setDead();
                tblock.getTown().notifyEveryone(FlagType.modifyBlocks.getLocalizedTownNotification());
            }
        }
    }


}


