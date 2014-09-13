package mytown.util;

import com.google.common.base.Joiner;
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

    public static String formatRanksToString(List<Rank> ranks) {
        String rs = null;
        for (Rank r : ranks) {
            String added = (r.getTown().getDefaultRank().equals(r) ? EnumChatFormatting.RED : EnumChatFormatting.GREEN) + r.getName() + EnumChatFormatting.WHITE;
            if(rs == null)
                rs = added;
            else
                rs += ", " + added;
        }
        if (rs == null) {
            rs = EnumChatFormatting.RED + "None";
        }
        return rs;
    }

    public static String formatBlockInfo(Block block) {
        return String.format(" ---------- Block ----------\nTown: %1$s\nDimension: %2$s\nLocation: %3$s", block.getTown().getName(), block.getDim(), block.getCoordString());
    }

    public static String formatNationInfo(Nation nation) {
        return String.format(Config.nationInfoFormat, nation.getName());
    }

    public static String formatPlotInfo(Plot plot) {
        String residents = null;
        for(Resident res : plot.getResidents()) {
            String added = (plot.hasOwner(res) ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + res.getPlayerName() + EnumChatFormatting.WHITE;

            if(residents == null)
                residents = added;
            else
                residents += ", " + added;
        }
        residents += EnumChatFormatting.WHITE;
        String position = String.format("(%s, %s, %s) --> (%s, %s, %s)", plot.getStartX(), plot.getStartY(), plot.getStartZ(), plot.getEndX(), plot.getEndY(), plot.getEndZ());
        return "Plot: " + plot.getName() + "\nResidents: " + residents + "\nBorders: " + position;
    }

    public static String formatRankInfo(Rank rank) {
        return String.format(Config.rankInfoFormat, rank.getName(), rank.getPermissionsString());
    }

    public static String formatResidentInfo(Resident resident) {
        String towns = null;
        for(Town town : resident.getTowns()) {
            String added = (resident.getSelectedTown().equals(town) ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + town.getName() + EnumChatFormatting.WHITE;
            if(towns == null)
                towns = added;
            else
                towns += ", " + added;
        }
        String format = EnumChatFormatting.WHITE + "Resident: %s" + EnumChatFormatting.WHITE + "\nTowns: %s" + EnumChatFormatting.WHITE;
        return String.format(format, resident.getPlayerName(), towns);
    }

    public static String formatTownInfo(Town town) { // TODO Show spawn/home-block location?
        String msg;

        String residentsString = null;
        for(Resident res : town.getResidents()) {
            if(residentsString == null)
                residentsString = res.getPlayerName();
            else
                residentsString += ", " + res.getPlayerName();
        }
        if(residentsString == null)
            residentsString = "";

        String ranksString = null;
        for(Rank rank : town.getRanks()) {
            if(ranksString == null)
                ranksString = rank.getName();
            else
                ranksString += ", " + rank.getName();
        }
        if(ranksString == null)
            ranksString = "";


        msg = String.format(Config.townInfoFormat, town.getName(), town.getResidents().size(), town.getBlocks().size(), town.getPlots().size(), residentsString, ranksString);

        return msg;
    }

    public static void sendMap(Resident res, int dim, int cx, int cz) {
        int heightRad = 4, widthRad = 9;

        res.sendMessage("---------- Town Map ----------");
        for (int z = cz - heightRad; z <= cz + heightRad; z++) {
            JsonMessageBuilder msgBuilder = new JsonMessageBuilder();

            for (int x = cx - widthRad; x <= cx + widthRad; x++) {
                Block b = DatasourceProxy.getDatasource().getBlock(dim, x, z);
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

    private static List<String> getResidentNameList(Collection<Resident> residents) {
        List<String> residentNames = new ArrayList<String>(residents.size());
        for (Resident res : residents) {
            residentNames.add(res.getPlayerName());
        }
        return residentNames;
    }

    private static List<String> getRankNameList(Collection<Rank> ranks) {
        List<String> rankNames = new ArrayList<String>(ranks.size());
        for (Rank rank : ranks) {
            rankNames.add(rank.getName());
        }
        return rankNames;
    }
}
