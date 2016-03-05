package mytown.api.container;

import myessentials.chat.api.IChatFormat;
import mytown.MyTown;
import mytown.entities.Rank;
import mytown.entities.Resident;
import myessentials.utils.ColorUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.Map;

public class ResidentRankMap extends HashMap<Resident, Rank> implements IChatFormat {

    public void remove(Resident res) {
        /*
        for (Iterator<Plot> it = res.getCurrentTown().plotsContainer.asList().iterator(); it.hasNext(); ) {
            Plot plot = it.next();
            if (plot.ownersContainer.contains(res) && plot.ownersContainer.size() <= 1) {
                it.remove();
            }
        }
        */
        super.remove(res);
    }

    public boolean contains(String username) {
        for (Resident res : keySet()) {
            if (res.getPlayerName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public Resident getMayor() {
        for(Map.Entry<Resident, Rank> entry : entrySet()) {
            if(entry.getValue().getType() == Rank.Type.MAYOR) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toChatMessage().getUnformattedText();
    }

    @Override
    public IChatComponent toChatMessage(boolean shortened) {
        IChatComponent result = new ChatComponentText("");

        for (Map.Entry<Resident, Rank> entry : entrySet()) {
            result.appendSibling(MyTown.instance.LOCAL.getLocalization("mytown.format.resident.withrank", entry.getKey().toChatMessage(true), entry.getValue()));
            result.appendSibling(new ChatComponentText(", ").setChatStyle(ColorUtils.styleComma));
        }

        result.getSiblings().remove(result.getSiblings().size() - 1);

        if (isEmpty()) {
            result.appendSibling(new ChatComponentText("NONE").setChatStyle(ColorUtils.styleEmpty));
        }
        return result;
    }

    @Override
    public IChatComponent toChatMessage() {
        return toChatMessage(false);
    }
}
