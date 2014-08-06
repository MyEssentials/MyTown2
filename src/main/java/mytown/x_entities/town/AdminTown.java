package mytown.x_entities.town;

import mytown.x_entities.Rank;
import mytown.x_entities.Resident;
import mytown.x_entities.TownBlock;

public class AdminTown extends Town {
	public AdminTown(String name) {
		super(name, 0);
	}

	@Override
	public boolean addTownBlock(TownBlock block) {
		return townBlocks.add(block);
		// TODO: To be edited when checking distance between towns.
	}

	@Override
	public void addResident(Resident res, Rank rank) {
		// Nothing, since admin towns shouldn't have residents
	}

}
