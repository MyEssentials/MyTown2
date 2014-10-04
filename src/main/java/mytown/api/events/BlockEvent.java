package mytown.api.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import mytown.entities.TownBlock;
import net.minecraftforge.common.MinecraftForge;

/**
 * @author Joe Goett
 */
public class BlockEvent extends Event {
    public TownBlock block = null;

    public BlockEvent(TownBlock block) {
        this.block = block;
    }

    @Cancelable
    public static class BlockCreateEvent extends BlockEvent {
        public BlockCreateEvent(TownBlock block) {
            super(block);
        }
    }

    @Cancelable
    public static class BlockDeleteEvent extends BlockEvent {
        public BlockDeleteEvent(TownBlock block) {
            super(block);
        }
    }

    public static boolean fire(BlockEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}
