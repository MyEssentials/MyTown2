package mytown.api.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import mytown.entities.Block;
import net.minecraftforge.common.MinecraftForge;

/**
 * @author Joe Goett
 */
public class BlockEvent extends Event {
    public Block block = null;

    public BlockEvent(Block block) {
        this.block = block;
    }

    @Cancelable
    public static class BlockCreateEvent extends BlockEvent {
        public BlockCreateEvent(Block block) {
            super(block);
        }
    }

    @Cancelable
    public static class BlockDeleteEvent extends BlockEvent {
        public BlockDeleteEvent(Block block) {
            super(block);
        }
    }

    public static boolean fire(BlockEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}
