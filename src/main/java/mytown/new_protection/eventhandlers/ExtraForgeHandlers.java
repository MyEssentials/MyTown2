package mytown.new_protection.eventhandlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
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
        List<ChunkPos> chunks = MyTownUtils.getChunksInBox((int)(ev.explosion.explosionX - ev.explosion.explosionSize), (int)(ev.explosion.explosionZ - ev.explosion.explosionSize), (int)(ev.explosion.explosionX + ev.explosion.explosionSize), (int)(ev.explosion.explosionZ + ev.explosion.explosionSize));
        for(ChunkPos chunk : chunks) {
            Town town = MyTownUtils.getTownAtPosition(ev.world.provider.dimensionId, chunk.getX(), chunk.getZ());
            if(town != null) {
                boolean explosionValue = (Boolean) town.getValue(FlagType.explosions);
                if (!explosionValue) {
                    ev.setCanceled(true);
                    town.notifyEveryone(FlagType.explosions.getLocalizedTownNotification());
                    return;
                }
            }
        }
    }
}
