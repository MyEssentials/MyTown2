package mytown.commands.format;

import myessentials.chat.api.ChatComponentContainer;
import mytown.MyTown;
import mytown.entities.Resident;

public class ChatComponentResidentInfo extends ChatComponentContainer {
    public ChatComponentResidentInfo(Resident res) {
        // Add header
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.resident.info.1", res.getPlayerName()));

        // Add data
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.resident.info.2", res.townsContainer));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.resident.info.3", res.getJoinDate().toString()));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.resident.info.4", res.getLastOnline().toString()));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.resident.info.5", res.getExtraBlocks()));
    }
}
