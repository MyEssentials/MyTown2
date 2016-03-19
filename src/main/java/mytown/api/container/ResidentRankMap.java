package mytown.api.container;

import myessentials.chat.api.ChatComponentFormatted;
import myessentials.chat.api.IChatFormat;
import myessentials.localization.api.LocalManager;
import mytown.entities.Rank;
import mytown.entities.Resident;
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
    public IChatComponent toChatMessage() {
        IChatComponent root = new ChatComponentText("");

        for (Map.Entry<Resident, Rank> entry : entrySet()) {
            if (root.getSiblings().size() > 0) {
                root.appendSibling(new ChatComponentFormatted("{7|, }"));
            }
            root.appendSibling(LocalManager.get("mytown.format.resident.withRank", entry.getKey(), entry.getValue()));
        }

        return root;
    }
}
