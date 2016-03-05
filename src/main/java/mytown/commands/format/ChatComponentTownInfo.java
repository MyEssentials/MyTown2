package mytown.commands.format;

import myessentials.chat.api.ChatComponentContainer;
import mytown.MyTown;
import mytown.entities.Town;

public class ChatComponentTownInfo extends ChatComponentContainer {
    public ChatComponentTownInfo(Town town) {
        // Add the header
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.info.1", town.getName()));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.info.2", town.residentsMap.size(), town.townBlocksContainer.size(), town.getMaxBlocks(), town.plotsContainer.size()));

        // Add resident info
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.info.3", town.residentsMap));

        // Add rank info
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.info.4", town.ranksContainer));
    }
}
