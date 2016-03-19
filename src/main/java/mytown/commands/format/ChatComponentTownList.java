package mytown.commands.format;

import myessentials.chat.api.ChatComponentContainer;
import myessentials.chat.api.ChatComponentFormatted;
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
            this.add(new ChatComponentFormatted("{7| - }{%s}", t.toChatMessage()));
        }
    }

    @Override
    public ChatComponentContainer getHeader(int page) {
        ChatComponentContainer header = super.getHeader(page);

        header.add(new ChatComponentFormatted("{9| - Towns}"));

        return header;
    }
}
