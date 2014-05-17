package mytown;

import java.util.List;

import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.util.EnumChatFormatting;

public class Formatter {

	public static String formatRanksToString(List<Rank> ranks) {
		EnumChatFormatting color;
		String res = null;
		for (Rank r : ranks) {
			if (r.getName().equals("Resident"))
				color = EnumChatFormatting.RED;
			else if (Constants.DEFAULT_RANK_VALUES.containsKey(r.getName()))
				color = EnumChatFormatting.GREEN;
			else
				color = EnumChatFormatting.YELLOW;
			if (res == null)
				res = color + r.getName();
			else
				res += EnumChatFormatting.WHITE + ", " + color + r.getName();
		}

		return res;
	}

	public static String formatResidentsToString(List<Resident> residents, Town t) {
		String res = null;
		for (Resident r : residents)
			if (res == null)
				res = EnumChatFormatting.WHITE + r.getUUID() + EnumChatFormatting.GOLD + " (" + EnumChatFormatting.GREEN + r.getTownRank(t).getName() + EnumChatFormatting.GOLD + ")";
			else
				res += ", " + r.getUUID();
		return res;
	}
	public static String formatTownBlocksToString(List<TownBlock> blocks, boolean chunkCoords) {
		String res = null;
		for (TownBlock block : blocks) {
			String toBeAdded;
			if (chunkCoords)
				toBeAdded = "(" + block.getX() + ", " + block.getZ() + ")";
			else
				toBeAdded = "(" + (block.getX() * 16) + ", " + (block.getZ() * 16) + ")";
			if (res == null)

				res = toBeAdded;
			else
				res += " | " + toBeAdded;
		}
		return res;
	}
}
