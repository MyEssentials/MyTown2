package mytown.commands.format;

import myessentials.chat.api.ChatComponentMultiPage;
import mytown.MyTown;
import mytown.entities.Town;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

public class ChatComponentTownList extends ChatComponentMultiPage {
    private Town.Container towns;

    public ChatComponentTownList(int maxComponentsPerPage, Town.Container towns) {
        super(maxComponentsPerPage);
        this.towns = towns;
        this.construct();
    }

    private void construct() {
        for (Town t : towns) {
            this.add(t.toChatMessage(true));
        }
    }

    @Override
    public List<IChatComponent> getHeader(int page) {
        List<IChatComponent> header = new ArrayList<IChatComponent>();

        header.add(MyTown.instance.LOCAL.getLocalization("mytown.notification.town.list.header", page, getNumberOfPages()));

        return header;
    }
}
