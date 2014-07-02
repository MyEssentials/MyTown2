package mytown.api.events;

import mytown.entities.town.Town;
import net.minecraftforge.event.Event;

/**
 * Base for all events that deal with {@link Town}'s
 * @author Joe Goett
 */
public class TownEvent extends Event {
	public Town town;
	
	public TownEvent(Town town) {
		this.town = town;
	}
	
	/**
	 * Fired off when a {@link Town} has been created
	 * @author Joe Goett
	 */
	public static class TownCreatedEvent extends TownEvent {
		public TownCreatedEvent(Town town) {
			super(town);
		}
	}
	
	/**
	 * Fired off when a {@link Town} has been destroyed
	 * @author Joe Goett
	 */
	public static class TownDestroyednEvent extends TownEvent {
		public TownDestroyednEvent(Town town) {
			super(town);
		}
	}
}