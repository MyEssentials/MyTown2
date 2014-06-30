package mytown.entities.town;

import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.TownBlock;

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
