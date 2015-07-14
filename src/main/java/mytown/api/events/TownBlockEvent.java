package mytown.api.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import mytown.entities.TownBlock;
import net.minecraftforge.common.MinecraftForge;

public class TownBlockEvent extends Event {
    public TownBlock block = null;

    public TownBlockEvent(TownBlock block) {
        this.block = block;
    }

    @Cancelable
    public static class BlockCreateEvent extends TownBlockEvent {
        public BlockCreateEvent(TownBlock block) {
            super(block);
        }
    }

    @Cancelable
    public static class BlockDeleteEvent extends TownBlockEvent {
        public BlockDeleteEvent(TownBlock block) {
            super(block);
        }
    }

    public static boolean fire(TownBlockEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}
