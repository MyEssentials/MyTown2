package mytown.x_handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.api.x_events.TownEvent.TownCreatedEvent;
import mytown.api.x_events.TownEvent.TownDestroyednEvent;
import mytown.x_commands.town.info.CmdListTown;

public class MyTownEventHandler {
    @SubscribeEvent
    public void townCreated(TownCreatedEvent townEvent) {
        CmdListTown.updateTownSortCache();
    }

    @SubscribeEvent
    public void townDestroyed(TownDestroyednEvent townEvent) {
        CmdListTown.updateTownSortCache();
    }
}