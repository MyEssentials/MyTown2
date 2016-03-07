package mytown.util;

import myessentials.chat.api.JsonMessageBuilder;
import mytown.MyTown;
import mytown.new_datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.util.ChatComponentText;
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

        res.sendMessage(new ChatComponentText("---------- Town Map ----------"));
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
}


