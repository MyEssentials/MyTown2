package mytown.commands.format;

import myessentials.chat.api.ChatComponentContainer;
import myessentials.utils.ColorUtils;
import mytown.MyTown;
import mytown.entities.Rank;
import net.minecraft.util.ChatComponentText;

public class ChatComponentRankPermList extends ChatComponentContainer {
    public ChatComponentRankPermList(Rank rank) {
        // Add header
        this.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.ranks.perm.list", rank.getName(), rank.getTown().getName()));

        // Add permissions
        if (isEmpty()) {
            this.add(new ChatComponentText("NONE").setChatStyle(ColorUtils.styleEmpty));
        } else {
            this.add(new ChatComponentText(String.join(", ", rank.permissionsContainer)));
        }
    }
}
