package mytown.util;

import mytown.x_interfaces.ITownFlag;
import mytown.x_interfaces.ITownPlot;
import mytown.x_entities.Rank;
import mytown.x_entities.Resident;
import mytown.x_entities.TownBlock;
import mytown.x_entities.town.Town;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class x_Formatter {


    /**
     * Formats a list of resident in a town to a String that is then sent to the player.
     *
     * @param residents
     * @param t
     * @return
     */
    public static String formatResidentsToString(List<Resident> residents, Town t) {
        String res = null;
        for (Resident r : residents)
            if (res == null) {
                res = EnumChatFormatting.WHITE + r.getUUID() + EnumChatFormatting.GOLD + " (" + EnumChatFormatting.GREEN + r.getTownRank(t).getName() + EnumChatFormatting.GOLD + ")";
            } else {
                res += ", " + EnumChatFormatting.WHITE + r.getUUID() + EnumChatFormatting.GOLD + " (" + EnumChatFormatting.GREEN + r.getTownRank(t).getName() + EnumChatFormatting.GOLD + ")";
            }
        if (residents.size() == 0) {
            res = EnumChatFormatting.RED + "NONE";
        }
        return res;
    }

    /**
     * Formats a list of town blocks to a String that is then sent to the player.
     *
     * @param blocks
     * @param chunkCoords
     * @return
     */
    public static String formatTownBlocksToString(List<TownBlock> blocks, boolean chunkCoords) {
        String res = null;
        for (TownBlock block : blocks) {
            String toBeAdded;
            if (chunkCoords) {
                toBeAdded = "(" + block.getX() + ", " + block.getZ() + ")";
            } else {
                toBeAdded = "(" + (block.getX() * 16) + ", " + (block.getZ() * 16) + ")";
            }

            if (res == null) {
                res = toBeAdded;
            } else {
                res += " | " + toBeAdded;
            }
        }
        return res;
    }

    /**
     * Formats a list of plots to a String that is then sent to the player.
     *
     * @param plots
     * @return
     */
    public static String formatTownPlotsToString(List<ITownPlot> plots) {
        String res = " "; // Adding a space since minecraft likes to mess with formatting -_-
        for (ITownPlot plot : plots) {
            String toBeAdded = "";
            if (!res.equals(" ")) {
                toBeAdded = "\n";
            }

            toBeAdded += "Dim:" + plot.getDim();
            toBeAdded += ", From [" + plot.getStartX();
            toBeAdded += ", " + plot.getStartY();
            toBeAdded += ", " + plot.getStartZ();
            toBeAdded += "] to [" + plot.getEndX();
            toBeAdded += ", " + plot.getEndY();
            toBeAdded += ", " + plot.getEndZ();
            toBeAdded += "], Owner: " + plot.getOwner().getUUID();
            res += toBeAdded;
        }
        return res;
    }

    /**
     * Formats a list of flags to a String that is then sent to the player.
     *
     * @param flags
     * @return
     */
    public static String formatFlagsToString(List<ITownFlag> flags) {
        String res = " "; // Adding a space since minecraft likes to mess with formatting -_-
        for (ITownFlag flag : flags) {
            String toBeAdded = "";
            if (!res.equals(" ")) {
                toBeAdded = "\n";
            }

            toBeAdded += EnumChatFormatting.YELLOW + flag.getName();
            toBeAdded += EnumChatFormatting.GREEN + " (" + EnumChatFormatting.RED + flag.getValue() + EnumChatFormatting.GREEN + ")" + EnumChatFormatting.WHITE;
            toBeAdded += ": " + flag.getLocalizedDescription();
            res += toBeAdded;
        }

        return res;
    }
}
