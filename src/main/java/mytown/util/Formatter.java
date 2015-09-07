package mytown.util;

import myessentials.chat.JsonMessageBuilder;
import mytown.MyTown;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.util.EnumChatFormatting;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;



public class Formatter {
    private static final DateFormat dateFormatter = DateFormat.getDateTimeInstance(0, 0);

    private Formatter() {
    }

    public static String formatDate(Date date) {
        return dateFormatter.format(date);
    }

    public static String formatBlockInfo(TownBlock block) {
        return String.format(" ---------- Block ----------\nTown: %1$s\nDimension: %2$s\nLocation: %3$s", block.getTown().getName(), block.getDim(), block.getCoordString());
    }

    public static void sendMap(Resident res, int dim, int cx, int cz) {
        int heightRad = 4, widthRad = 9;

        res.sendMessage("---------- Town Map ----------");
        for (int z = cz - heightRad; z <= cz + heightRad; z++) {
            JsonMessageBuilder msgBuilder = new JsonMessageBuilder();

            for (int x = cx - widthRad; x <= cx + widthRad; x++) {
                TownBlock b = MyTownUniverse.instance.blocks.get(dim, x, z);
                JsonMessageBuilder extraBuilder = msgBuilder.addExtra();

                boolean mid = z == cz && x == cx;
                boolean isTown = b != null && b.getTown() != null;
                boolean ownTown = isTown && res.townsContainer.contains(b.getTown());

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
        if (res.getPlayer() == null)
            return;
        sendMap(res, res.getPlayer().dimension, res.getPlayer().chunkCoordX, res.getPlayer().chunkCoordZ);
    }

    /**
     * Formats all the 'owners' (plot owners or town mayor) to a string that is sent when a protection is bypassed
     * Uses localization
     */
    public static String formatOwnersToString(Town town, int dim, int x, int y, int z) {
        List<Resident> residents = town.getOwnersAtPosition(dim, x, y, z);
        String formatterList = null;
        if (residents == null || residents.isEmpty()) {
            formatterList = "SERVER ADMINS";
        } else {
            for (Resident r : residents)
                if (formatterList == null) {
                    formatterList = r.getPlayerName();
                } else {
                    formatterList += ", " + r.getPlayerName();
                }
        }
        return MyTown.instance.LOCAL.getLocalization("mytown.notification.town.owners", formatterList);
    }

    /**
     * Formats the town mayor to a string that is sent when a protection is bypassed
     * Uses localization
     */
    public static String formatOwnersToString(Town town) {
        Resident owner = town.residentsMap.getMayor();
        String ownerName;
        if(owner == null)
            ownerName = "SERVER ADMINS";
        else
            ownerName = owner.getPlayerName();
        return MyTown.instance.LOCAL.getLocalization("mytown.notification.town.owners", ownerName);
    }
}


