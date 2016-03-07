package mytown.commands.format;

import myessentials.chat.api.ChatComponentContainer;
import mytown.MyTown;
import mytown.entities.Plot;

public class ChatComponentPlotInfo extends ChatComponentContainer {
    public ChatComponentPlotInfo(Plot plot) {
        // Add header
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.plot.info.1", plot.getName()));

        // Add info
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.plot.info.2", plot.ownersContainer));
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.plot.info.3", plot.toVolume()));
    }
}
