package mytown.api.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import mytown.entities.Resident;
import net.minecraftforge.common.MinecraftForge;

public class ResidentEvent extends Event {
    public Resident resident = null;

    public ResidentEvent(Resident resident) {
        this.resident = resident;
    }

    @Cancelable
    public static class ResidentCreateEvent extends ResidentEvent {
        public ResidentCreateEvent(Resident resident) {
            super(resident);
        }
    }

    @Cancelable
    public static class ResidentDeleteEvent extends ResidentEvent {
        public ResidentDeleteEvent(Resident resident) {
            super(resident);
        }
    }

    public static boolean fire(ResidentEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}
