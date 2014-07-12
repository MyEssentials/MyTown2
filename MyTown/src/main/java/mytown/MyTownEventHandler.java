package mytown;

import mytown.api.events.TownEvent.TownCreatedEvent;
import mytown.api.events.TownEvent.TownDestroyednEvent;
import mytown.commands.town.info.CmdListTown;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

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