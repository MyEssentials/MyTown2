package mytown.api.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraftforge.common.MinecraftForge;

public class TownEvent extends Event {
    public Town town = null;

    public TownEvent(Town town) {
        this.town = town;
    }

    @Cancelable
    public static class TownCreateEvent extends TownEvent {
        public TownCreateEvent(Town town) {
            super(town);
        }
    }

    @Cancelable
    public static class TownDeleteEvent extends TownEvent {
        public TownDeleteEvent(Town town) {
            super(town);
        }
    }

    // TODO: Make them cancelable?
    public static class TownEnterEvent extends TownEvent {
        public Resident resident = null;

        public TownEnterEvent(Town town, Resident resident) {
            super(town);
            this.resident = resident;
        }
    }

    public static class TownEnterInRangeEvent extends TownEvent {
        public Resident resident = null;

        public TownEnterInRangeEvent(Town town, Resident resident) {
            super(town);
            this.resident = resident;
        }
    }

    public static boolean fire(TownEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}
