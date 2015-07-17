package mytown.util;

import mytown.api.interfaces.IFlagsContainer;
import mytown.api.interfaces.IResidentsContainer;
import myessentials.chat.JsonMessageBuilder;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.util.EnumChatFormatting;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;



// TODO: Refactor the map methods as well.
public class Formatter {
    private static final DateFormat dateFormatter = DateFormat.getDateTimeInstance(0, 0);

    private static final String colorPlayer = EnumChatFormatting.WHITE.toString();
    private static final String colorOwner = EnumChatFormatting.RED.toString();

    private static final String colorRankMayor = EnumChatFormatting.RED.toString();
    private static final String colorRankDefault = EnumChatFormatting.GREEN.toString();
    private static final String colorRankOther = EnumChatFormatting.WHITE.toString();

    private static final String colorTown = EnumChatFormatting.GOLD.toString();
    private static final String colorSelectedTown = EnumChatFormatting.GREEN.toString();

    private static final String colorFlag = EnumChatFormatting.GRAY.toString();
    private static final String colorValueConst = EnumChatFormatting.RED.toString();
    private static final String colorValueVar = EnumChatFormatting.GREEN.toString();
    private static final String colorDescription = EnumChatFormatting.GRAY.toString();

    private static final String colorCoords = EnumChatFormatting.BLUE.toString();

    private static final String colorComma = EnumChatFormatting.WHITE.toString();
    private static final String colorEmpty = EnumChatFormatting.RED.toString();
    private static final String colorAdmin = EnumChatFormatting.RED.toString();
    //private static final String paranthColor = EnumChatFormatting.GOLD.toString();


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
        return LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", formatterList);
    }

    /**
     * Formats the town mayor to a string that is sent when a protection is bypassed
     * Uses localization
     */
    public static String formatOwnersToString(Town town) {
        Resident owner = town.getMayor();
        String ownerName;
        if(owner == null)
            ownerName = "SERVER ADMINS";
        else
            ownerName = owner.getPlayerName();
        return LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", ownerName);
    }

    /**
     * Formats a list of resident to a String that is then sent to the player.
     * For each type of container (Plot, Town etc) has a different formatting.
     */
    public static String formatResidentsToString(IResidentsContainer container) {
        Collection<Resident> residents = container.getResidents();
        String formattedList = null;

        for (Resident r : residents) {

            // Formatting for each type of residents container.
            String toAdd;
            if (container instanceof Town) {
                toAdd = colorPlayer + r.getPlayerName() + colorComma + " (" + formatRankToString(((Town) container).getResidentRank(r)) + colorComma + ")";
            } else if (container instanceof Plot) {
                toAdd = (((Plot)container).hasOwner(r) ? colorOwner : colorPlayer) + r.getPlayerName();
            } else {
                toAdd = colorPlayer + r.getPlayerName();
            }

            if (formattedList == null) {
                formattedList = toAdd;
            } else {
                formattedList += colorComma + ", " + toAdd;
            }
        }
        if (residents.isEmpty()) {
            formattedList = colorEmpty + "NONE";
        }
        return formattedList;
    }

    /**
     * Returns a formatted string from the collection of ranks.
     * Each rank is formatted using formatRankToString method.
     */
    public static String formatRanksToString(Collection<Rank> ranks) {

        String res = null;
        for (Rank rank : ranks) {
            if (res == null) {
                res = formatRankToString(rank);
            } else {
                res += colorComma + ", " + formatRankToString(rank);
            }
        }

        if (ranks.isEmpty()) {
            res = colorEmpty + "NONE";
        }
        return res;
    }

    /**
     * Returns a formatted string from the rank with color.
     */
    public static String formatRankToString(Rank rank) {
        if(rank == null)
            return "";

        String color;
        if (Rank.theMayorDefaultRank.equals(rank.getName())) {
            color = colorRankMayor + "";
        } else if (Rank.theDefaultRank.equals(rank.getName())) {
            color = colorRankDefault + "";
        } else {
            color = colorRankOther + "";
        }
        return color + rank.getName();
    }

    /**
     * Return a formatted string from a list of town blocks.
     */
    public static String formatTownBlocksToString(Collection<TownBlock> blocks) {
        String formattedList = null;
        for(TownBlock block : blocks) {
            String toAdd = colorComma + "{"+ colorCoords + (block.getX() << 4) + colorComma + ","
                                           + colorCoords + (block.getZ() << 4) + colorComma + "}";
            if(formattedList == null)
                formattedList = toAdd;
            else
                formattedList += colorComma + "; " + toAdd;
        }
        return formattedList;
    }

    /**
     * Returns a formatted string from a list of towns.
     */
    public static String formatTownsToString(Collection<Town> towns) {
        String formattedList = null;
        for(Town town : towns) {
            String mayorName = town.getMayor() != null ? colorPlayer + town.getMayor().getPlayerName()
                    : colorAdmin + "SERVER ADMINS";
            String toAdd = colorTown + town.getName() + ":" + colorComma +
                    " { " + colorRankMayor + "Mayor: " + mayorName + colorComma + " }";
            if(formattedList == null)
                formattedList = toAdd;
            else
                formattedList += "\\n" + toAdd;
        }
        return formattedList;
    }

    /**
     * Returns a formatted string from the towns the resident given is in.
     */
    public static String formatTownsToString(Resident res) {
        Collection<Town> towns = res.getTowns();
        String formattedList = null;
        for(Town town : towns) {
            String toAdd = (res.getSelectedTown() == town ? colorSelectedTown : colorTown) + town.getName();
            if(formattedList == null)
                formattedList = toAdd;
            else
                formattedList += colorComma + ", " + toAdd;
        }
        return formattedList;
    }

    /**
     * Returns a formatted string from the container given.
     */
    @SuppressWarnings("unchecked")
    public static String formatFlagsToString(IFlagsContainer container) {
        String formattedFlagList = null;

        for (Flag flag : container.getFlags()) {
            if(flag.getFlagType().canTownsModify() || container instanceof Wild) {
                if (formattedFlagList == null) {
                    formattedFlagList = "";
                } else {
                    formattedFlagList += "\\n";
                }
                formattedFlagList += formatFlagToString(flag, colorValueVar);
            }
        }

        String unconfigurableFlags = "";
        for(FlagType flagType : FlagType.values()) {
            if(!container.hasFlag(flagType) && !((container instanceof Plot) && flagType.isTownOnly()) && !((container instanceof Wild) && !flagType.isWildPerm())) {
                unconfigurableFlags += "\\n" + formatFlagToString(new Flag(flagType, flagType.getDefaultValue()), colorValueConst);
            }
        }

        if(formattedFlagList == null)
            formattedFlagList = "";


        //if(!"".equals(unconfigurableFlags))
        formattedFlagList += unconfigurableFlags;


        return formattedFlagList;
    }

    /**
     * Returns a formatted string from the flag is given.
     */
    public static String formatFlagToString(Flag flag, String valueColor) {
        return String.format(colorFlag + "%s" + colorComma + "[" + valueColor + "%s" + colorComma + "]:" + colorComma + " %s", flag.getFlagType().toString().toLowerCase(), flag.valueToString(), flag.getFlagType().getLocalizedDescription());
    }
}


