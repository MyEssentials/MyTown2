package mytown.util;

import mytown.config.Config;
import mytown.core.utils.chat.JsonMessageBuilder;
import mytown.entities.*;
import mytown.proxies.DatasourceProxy;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Joe Goett
 */
public class Formatter {
    private Formatter() {
    }

    public static String formatBlockInfo(TownBlock block) {
        return String.format(" ---------- Block ----------\nTown: %1$s\nDimension: %2$s\nLocation: %3$s", block.getTown().getName(), block.getDim(), block.getCoordString());
    }

    public static String formatNationInfo(Nation nation) {
        return String.format(nationInfoFormat, nation.getName());
    }

    public static String formatPlotInfo(Plot plot) {
        String residents = null;
        for (Resident res : plot.getResidents()) {
            String added = (plot.hasOwner(res) ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + res.getPlayerName() + EnumChatFormatting.WHITE;

            if (residents == null)
                residents = added;
            else
                residents += ", " + added;
        }
        residents += EnumChatFormatting.WHITE;
        String position = String.format("(%s, %s, %s) --> (%s, %s, %s)", plot.getStartX(), plot.getStartY(), plot.getStartZ(), plot.getEndX(), plot.getEndY(), plot.getEndZ());
        return "Plot: " + plot.getName() + "\nResidents: " + residents + "\nBorders: " + position;
    }

    public static String formatRankInfo(Rank rank) {
        return String.format(rankInfoFormat, rank.getName(), rank.getPermissionsString());
    }

    public static String formatResidentInfo(Resident resident) {
        String towns = null;
        for (Town town : resident.getTowns()) {
            String added = (resident.getSelectedTown().equals(town) ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + town.getName() + EnumChatFormatting.WHITE;
            if (towns == null)
                towns = added;
            else
                towns += ", " + added;
        }
        String format = EnumChatFormatting.WHITE + "Resident: %s" + EnumChatFormatting.WHITE + "\nTowns: %s" + EnumChatFormatting.WHITE;
        return String.format(format, resident.getPlayerName(), towns);
    }

    public static String formatTownInfo(Town town) { // TODO Show spawn/home-block location?
        String msg;

        String residentsString = formatResidentsToString(town.getResidents(), town);
        String ranksString = formatRanksToString(town.getRanks());

        msg = String.format(townInfoFormat, town.getName(), town.getResidents().size(), town.getBlocks().size(), town.getMaxBlocks(), town.getPlots().size(), residentsString, ranksString);

        return msg;
    }

    public static void sendMap(Resident res, int dim, int cx, int cz) {
        int heightRad = 4, widthRad = 9;

        res.sendMessage("---------- Town Map ----------");
        for (int z = cz - heightRad; z <= cz + heightRad; z++) {
            JsonMessageBuilder msgBuilder = new JsonMessageBuilder();

            for (int x = cx - widthRad; x <= cx + widthRad; x++) {
                TownBlock b = DatasourceProxy.getDatasource().getBlock(dim, x, z);
                JsonMessageBuilder extraBuilder = msgBuilder.addExtra();

                boolean mid = z == cz && x == cx;
                boolean isTown = b != null && b.getTown() != null;
                boolean ownTown = isTown && res.hasTown(b.getTown());

                if (mid) {
                    extraBuilder.setColor(ownTown ? EnumChatFormatting.GREEN : isTown ? EnumChatFormatting.RED : EnumChatFormatting.WHITE);
                } else {
                    extraBuilder.setColor(ownTown ? EnumChatFormatting.DARK_GREEN : isTown ? EnumChatFormatting.DARK_RED : EnumChatFormatting.GRAY);
                }

                extraBuilder.setText(isTown ? "O" : "_");

                if (b != null) {
                    extraBuilder.setHoverEventShowText(formatBlockInfo(b));
                    extraBuilder.setClickEventRunCommand("/t info " + b.getTown().getName());
                }
            }

            res.getPlayer().addChatMessage(msgBuilder.build());
        }
    }

    public static void sendMap(Resident res) {
        if (res.getPlayer() == null) return;
        sendMap(res, res.getPlayer().dimension, res.getPlayer().chunkCoordX, res.getPlayer().chunkCoordZ);
    }


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
                res = EnumChatFormatting.WHITE + r.getPlayerName() + EnumChatFormatting.GOLD + " (" + formatRankToString(t.getResidentRank(r)) + EnumChatFormatting.GOLD + ")";
            } else {
                res += ", " + EnumChatFormatting.WHITE + r.getPlayerName() + EnumChatFormatting.GOLD + " (" + formatRankToString(t.getResidentRank(r)) + EnumChatFormatting.GOLD + ")";
            }
        if (residents.size() == 0) {
            res = EnumChatFormatting.RED + "NONE";
        }
        return res;
    }

    /**
     * Formats a list of ranks to a String that is then sent to the player.
     *
     * @param ranks
     * @return
     */
    public static String formatRanksToString(List<Rank> ranks) {

        String res = null;
        for (Rank r : ranks) {
            if (res == null) {
                res = formatRankToString(r);
            } else {
                res += EnumChatFormatting.WHITE + ", " + formatRankToString(r);
            }
        }
        if (ranks.size() == 0) {
            res = EnumChatFormatting.RED + "NONE";
        }
        return res;
    }

    /**
     * Formats a rank to String with colo(u)r
     *
     * @param rank
     * @return
     */
    public static String formatRankToString(Rank rank) {
        String color;
        if (Rank.theMayorDefaultRank.equals(rank.getName())) {
            color = EnumChatFormatting.RED + "";
        } else if (Rank.theDefaultRank.equals(rank.getName())) {
            color = EnumChatFormatting.GREEN + "";
        } else {
            color = EnumChatFormatting.WHITE + "";
        }
        return color + rank.getName();
    }

    /**
     * Formats a list of owners of a plot or town
     * TODO: Generalize this
     *
     * @param residentList
     * @return
     */
    public static String formatOwnersToString(List<Resident> residentList) {
        String formattedList = null;
        for(Resident res : residentList) {
            if(res != null) {
                if (formattedList == null)
                    formattedList = "§6Owners: " + res.getPlayerName();
                else
                    formattedList = ", " + res.getPlayerName();
            } else {
                formattedList = "§6Owners: SERVER ADMINS";
                break;
            }
        }
        return formattedList;
    }

    public static String formatOwnerToString(Resident res) {
        List<Resident> list = new ArrayList<Resident>();
        list.add(res);
        return formatOwnersToString(list);
    }



    public static String blockInfoFormat = " ---------- Block ----------\nTown: %1$s\nDimension: %2$s\nLocation: %3$s";

    public static String nationInfoFormat = " ---------- %1$s  ----------\nCapital: TODO";

    public static String plotInfoFormat = " ---------- %1$s  ----------\nTown: %2$s\nDimension: %3$s\nStart: %4$s\nEnd: %5$s";

    public static String rankInfoFormat = " ---------- %1$s  ----------\nPermissions: %2$s";

    public static String residentInfoFormat = " ---------- %1$s  ----------";

    public static String townInfoFormat =
            EnumChatFormatting.GRAY + " -------- " + EnumChatFormatting.GREEN+" %1$s "+EnumChatFormatting.GREEN+" ("+EnumChatFormatting.WHITE+"R:%2$s"+EnumChatFormatting.GREEN+" |"+EnumChatFormatting.WHITE+" C:%3$s/%4$s "+EnumChatFormatting.GREEN+"| "+EnumChatFormatting.WHITE+"P:%5$s"+EnumChatFormatting.GREEN+")"+EnumChatFormatting.GRAY+" --------" +
            "\n"+EnumChatFormatting.GRAY+"Residents: %6$s" +
            "\n"+EnumChatFormatting.GRAY+"Ranks: %7$s";



}
