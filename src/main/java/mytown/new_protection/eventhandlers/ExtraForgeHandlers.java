package mytown.new_protection.eventhandlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.MyTown;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.entities.Wild;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.util.ChunkPos;
import mytown.util.MyTownUtils;
import net.minecraftforge.event.world.ExplosionEvent;

import java.util.List;

/**
 * Created by AfterWind on 3/4/2015.
 * Handling any events that are not yet compatible with the most commonly used version of forge.
 */
public class ExtraForgeHandlers {

    private static ExtraForgeHandlers instance;
    public static ExtraForgeHandlers getInstance() {
        if(instance == null)
            instance = new ExtraForgeHandlers();
        return instance;
    }

    /**
     * Forge 1254 is needed for this
     */
    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Start ev) {
        List<ChunkPos> chunks = MyTownUtils.getChunksInBox((int)(ev.explosion.explosionX - ev.explosion.explosionSize - 2), (int)(ev.explosion.explosionZ - ev.explosion.explosionSize - 2), (int)(ev.explosion.explosionX + ev.explosion.explosionSize + 2), (int)(ev.explosion.explosionZ + ev.explosion.explosionSize + 2));
        for(ChunkPos chunk : chunks) {
            TownBlock block = DatasourceProxy.getDatasource().getBlock(ev.world.provider.dimensionId, chunk.getX(), chunk.getZ());
            if(block == null) {
                if(!(Boolean)Wild.getInstance().getValue(FlagType.explosions)) {
                    ev.setCanceled(true);
                    return;
                }
            } else {
                if (!(Boolean) block.getTown().getValue(FlagType.explosions)) {
                    ev.setCanceled(true);
                    block.getTown().notifyEveryone(FlagType.explosions.getLocalizedTownNotification());
                    return;
                }
            }
        }
    }
}
