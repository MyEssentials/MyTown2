package mytown.handler;

import net.minecraftforge.event.ForgeSubscribe;
import mytown.api.events.TownEvent.TownCreatedEvent;
import mytown.api.events.TownEvent.TownDestroyednEvent;
import mytown.commands.town.info.CmdListTown;

public class MyTownEventHandler {
	@ForgeSubscribe
	public void townCreated(TownCreatedEvent townEvent) {
		CmdListTown.updateTownSortCache();
	}

	@ForgeSubscribe
	public void townDestroyed(TownDestroyednEvent townEvent) {
		CmdListTown.updateTownSortCache();
	}
}