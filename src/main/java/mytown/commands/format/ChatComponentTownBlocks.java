package mytown.commands.format;

import myessentials.chat.api.ChatComponentContainer;
import mytown.MyTown;
import mytown.entities.Resident;
import mytown.entities.Town;
import net.minecraft.util.ChatComponentText;

import java.util.Iterator;

public class ChatComponentTownBlocks extends ChatComponentContainer {
    public ChatComponentTownBlocks(Town town) {
        // Add header
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.blocks.info.1"));

        // Calculate data
        String blocks = town.townBlocksContainer.size() + "/" + town.getMaxBlocks();
        String extraBlocks = town.getExtraBlocks() + "";
        String dash = " - ";
        String farBlocks = town.townBlocksContainer.getFarClaims() + "/" + town.getMaxFarClaims();

        // Add data
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.blocks.info.2", blocks));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.blocks.info.3"));
        this.add(new ChatComponentText(dash + "TOWN (" + town.townBlocksContainer.getExtraBlocks() + ")"));
        for(Iterator<Resident> it = town.residentsMap.keySet().iterator(); it.hasNext();) {
            Resident resInTown = it.next();
            this.add(new ChatComponentText(dash + resInTown.getPlayerName() + " (" + resInTown.getExtraBlocks() + ")"));
        }

        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.blocks.info.4", farBlocks));
    }
}
