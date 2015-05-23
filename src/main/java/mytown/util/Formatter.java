package mytown.util;

import mytown.api.interfaces.IHasFlags;
import mytown.core.utils.chat.JsonMessageBuilder;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import net.minecraft.util.EnumChatFormatting;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Joe Goett
 */
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


    public static String formatResidentsToString(Collection<Resident> residents) {
        String formatterList = null;
        if (residents == null || residents.size() == 0) {
            formatterList = "SERVER ADMINS";
        } else {
            for (Resident r : residents)
                if (formatterList == null) {
                    formatterList = r.getPlayerName();
                } else {
                    formatterList += ", " + r.getPlayerName();
                }
        }
        return formatterList;
    }

    /**
     * Formats a list of resident in a town to a String that is then sent to the player.
     *
     * @param t
     * @return
     */
    public static String formatResidentsToString(Town t) {
        Collection<Resident> residents = t.getResidents();
        String formattedList = null;
        for (Resident r : residents)
            if (formattedList == null) {
                formattedList = EnumChatFormatting.WHITE + r.getPlayerName() + EnumChatFormatting.GOLD + " (" + formatRankToString(t.getResidentRank(r)) + EnumChatFormatting.GOLD + ")";
            } else {
                formattedList += ", " + EnumChatFormatting.WHITE + r.getPlayerName() + EnumChatFormatting.GOLD + " (" + formatRankToString(t.getResidentRank(r)) + EnumChatFormatting.GOLD + ")";
            }
        if (residents.size() == 0) {
            formattedList = EnumChatFormatting.RED + "NONE";
        }
        return formattedList;
    }

    public static String formatResidentsToString(Plot p) {
        Collection<Resident> residents = p.getResidents();
        String formattedList = null;
        for(Resident r : residents) {
            String toAdd = (p.hasOwner(r) ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + r.getPlayerName();
            if(formattedList == null)
                formattedList = toAdd;
            else
                formattedList += ", " + toAdd;
        }
        if (residents.size() == 0) {
            formattedList = EnumChatFormatting.RED + "NONE";
        }
        return formattedList;
    }

    /**
     * Formats a list of ranks to a String that is then sent to the player.
     */
    public static String formatRanksToString(Collection<Rank> ranks) {

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
     * Formats a rank to String with color
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
     * Return a formatted string from a list of town blocks
     *
     * @param blocks
     * @return
     */
    public static String formatTownBlocksToString(Collection<TownBlock> blocks) {
        String formattedList = null;
        for(TownBlock block : blocks) {
            String toAdd = "{"+ EnumChatFormatting.BLUE + (block.getX() << 4) + EnumChatFormatting.GRAY + "," + EnumChatFormatting.BLUE + (block.getZ() << 4) + EnumChatFormatting.GRAY + "}";
            if(formattedList == null)
                formattedList = toAdd;
            else
                formattedList += EnumChatFormatting.RED + "; " + EnumChatFormatting.GRAY + toAdd;
        }
        return formattedList;
    }

    /**
     * Returns a formatted string from a list of towns
     *
     * @param towns
     * @return
     */
    public static String formatTownsToString(Collection<Town> towns) {
        String formattedList = null;
        for(Town town : towns) {
            String toAdd = EnumChatFormatting.GREEN + town.getName() + ":" + EnumChatFormatting.GRAY + " { " + EnumChatFormatting.RED + "Mayor: " + EnumChatFormatting.WHITE + (town.getMayor() != null ? town.getMayor().getPlayerName() : EnumChatFormatting.RED + "SERVER ADMINS") + EnumChatFormatting.GRAY + " }";
            if(formattedList == null)
                formattedList = toAdd;
            else
                formattedList += "\\n" + toAdd;
        }
        return formattedList;
    }

    public static String formatTownsToString(Resident res) {
        Collection<Town> towns = res.getTowns();
        String formattedList = null;
        for(Town town : towns) {
            String toAdd = (res.getSelectedTown() == town ? EnumChatFormatting.GREEN : EnumChatFormatting.WHITE) + town.getName();
            if(formattedList == null)
                formattedList = toAdd;
            else
                formattedList += ", " + toAdd;
        }
        return formattedList;
    }

    public static String formatOwnerToString(Resident res) {
        List<Resident> list = new ArrayList<Resident>();
        list.add(res);
        return formatResidentsToString(list);
    }

    @SuppressWarnings("unchecked")
    public static String formatFlagsToString(IHasFlags container) {
        String formattedFlagList = null;

        for (Flag flag : container.getFlags()) {
            if(flag.flagType.canTownsModify() || container instanceof Wild) {
                if (formattedFlagList == null) {
                    formattedFlagList = "";
                } else {
                    formattedFlagList += "\\n";
                }
                formattedFlagList += formatFlagToString(flag, EnumChatFormatting.GREEN);
            }
        }

        String unconfigurableFlags = "";
        for(FlagType flagType : FlagType.values()) {
            if(!container.hasFlag(flagType) && (!(container instanceof Plot) || !flagType.isTownOnly()) && (!(container instanceof Wild) || flagType.isWildPerm())) {
                unconfigurableFlags += "\\n" + formatFlagToString(new Flag(flagType, flagType.getDefaultValue()), EnumChatFormatting.RED);
            }
        }

        if(formattedFlagList == null)
            formattedFlagList = "";

        if(!unconfigurableFlags.equals(""))
            formattedFlagList += "\\n" + EnumChatFormatting.RED + "UNCONFIGURABLE FLAGS: " + unconfigurableFlags;

        return formattedFlagList;
    }

    public static String formatFlagToString(Flag flag, EnumChatFormatting valueColor) {
        return String.format(EnumChatFormatting.GRAY + "%s" + EnumChatFormatting.WHITE + "[" + valueColor + "%s" + EnumChatFormatting.WHITE + "]:" + EnumChatFormatting.GRAY + " %s", flag.flagType.toString(), flag.valueToString(), flag.flagType.getLocalizedDescription());
    }
}


