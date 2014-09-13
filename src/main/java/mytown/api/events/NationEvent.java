package mytown.api.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import mytown.entities.Nation;
import net.minecraftforge.common.MinecraftForge;

/**
 * @author Joe Goett
 */
public class NationEvent extends Event {
    public Nation nation = null;

    public NationEvent(Nation nation) {
        this.nation = nation;
    }

    @Cancelable
    public static class NationCreateEvent extends NationEvent {
        public NationCreateEvent(Nation nation) {
            super(nation);
        }
    }

    @Cancelable
    public static class NationDeleteEvent extends NationEvent {
        public NationDeleteEvent(Nation nation) {
            super(nation);
        }
    }

    public static boolean fire(NationEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}
