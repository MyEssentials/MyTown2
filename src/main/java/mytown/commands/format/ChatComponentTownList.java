package mytown.commands.format;

import myessentials.chat.api.ChatComponentContainer;
import myessentials.chat.api.ChatComponentMultiPage;
import mytown.MyTown;
import mytown.entities.Town;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

public class ChatComponentTownList extends ChatComponentMultiPage {
    private Town.Container towns;

    public ChatComponentTownList(Town.Container towns) {
        super(9);
        this.towns = towns;
        this.construct();
    }

    private void construct() {
        for (Town t : towns) {
            this.add(t.toChatMessage());
        }
    }

    @Override
    public ChatComponentContainer getHeader(int page) {
        ChatComponentContainer header = new ChatComponentContainer();

        header.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.list.header", page, getNumberOfPages()));

        return header;
    }
}
