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
