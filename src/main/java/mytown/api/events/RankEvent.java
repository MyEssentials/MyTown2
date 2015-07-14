package mytown.api.events;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import mytown.entities.Rank;
import net.minecraftforge.common.MinecraftForge;

public class RankEvent extends Event {
    public Rank rank = null;

    public RankEvent(Rank rank) {
        this.rank = rank;
    }

    @Cancelable
    public static class RankCreateEvent extends RankEvent {
        public RankCreateEvent(Rank rank) {
            super(rank);
        }
    }

    @Cancelable
    public static class RankDeleteEvent extends RankEvent {
        public RankDeleteEvent(Rank rank) {
            super(rank);
        }
    }

    public static boolean fire(RankEvent ev) {
        return MinecraftForge.EVENT_BUS.post(ev);
    }
}
