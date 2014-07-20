package mytown.util;

import com.google.common.base.Joiner;
import mytown.config.Config;
import mytown.entities.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Joe Goett
 */
public class Formatter {
    private Formatter() {}

    public static String formatBlockInfo(Block block) {
        return String.format(Config.blockInfoFormat, block.getTown().getName(), block.getDim(), block.getCoordString());
    }

    public static String formatNationInfo(Nation nation) {
        return String.format(Config.nationInfoFormat, nation.getName());
    }

    public static String formatPlotInfo(Plot plot) {
        return String.format(Config.plotInfoFormat, plot.getName(), plot.getTown().getName(), plot.getDim(), plot.getStartCoordString(), plot.getEndCoordString());
    }

    public static String formatRankInfo(Rank rank) {
        return String.format(Config.rankInfoFormat, rank.getName(), rank.getPermissionsString());
    }

    public static String formatResidentInfo(Resident resident) {
        return String.format(Config.residentInfoFormat, resident.getPlayerName());
    }

    public static String formatTownInfo(Town town) { // TODO Show spawn/home-block location?
        List<String> residentNames = getResidentNameList(town.getResidents());
        List<String> rankNames = getRankNameList(town.getRanks());
        return String.format(Config.townInfoFormat, town.getName(), town.getResidents().size(), town.getBlocks().size(), town.getPlots().size(), Joiner.on(", ").join(residentNames), Joiner.on(", ").join(rankNames));
    }

    public static String formatMap(Resident res, int cx, int cz) {
        int heightRad = 4;
        int widthRad = 9;
        StringBuilder sb = new StringBuilder();
        String c;

        sb.append("---------- Town Map ----------");
        sb.setLength(0);
        for (int z = cz - heightRad; z <= cz + heightRad; z++) {
            sb.setLength(0);
            for (int x = cx - widthRad; x <= cx + widthRad; x++) {
                Block b = null; // TODO Get the block from the DB

                boolean mid = z == cz && x == cx;
                boolean isTown = b != null && b.getTown() != null;
                boolean ownTown = isTown && res.hasTown(b.getTown());

                if (mid) {
                    c = ownTown ? "§a" : isTown ? "§c" : "§f";
                } else {
                    c = ownTown ? "§2" : isTown ? "§4" : "§7";
                }

                c += isTown ? "O" : "_";
                sb.append(c);
            }
        }

        return sb.toString();
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
