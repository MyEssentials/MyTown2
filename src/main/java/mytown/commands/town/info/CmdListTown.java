package mytown.commands.town.info;

import mytown.core.utils.chat.JsonMessageBuilder;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import mytown.util.Formatter;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

// TODO Move cache to another class?

@Permission("mytown.cmd.outsider.list")
public class CmdListTown extends CommandBase {
    private static List<Town> sortedTownCache = new ArrayList<Town>();
    private static IChatComponent cachedTownList = null;

    public CmdListTown(CommandBase parent) {
        super("list", parent);
    }

    @Override
    public boolean canConsoleUse() {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        sender.addChatMessage(cachedTownList);
    }

    public static void updateTownSortCache() {
        if (DatasourceProxy.getDatasource().getTownsMap().size() <= 0) return;
        sortedTownCache.clear();
        sortedTownCache.addAll(DatasourceProxy.getDatasource().getTownsMap().values());
        // TODO Sort

        JsonMessageBuilder msgBuilder = new JsonMessageBuilder();
        for (int i = 0; i < sortedTownCache.size(); i++) {
            JsonMessageBuilder extra = msgBuilder.addExtra();
            Town t = sortedTownCache.get(i);
            extra.setText(t.getName());
            extra.setHoverEventShowText(Formatter.formatTownInfo(t));
            extra.setClickEventRunCommand("/t info " + t.getName());
            if (i + 1 < sortedTownCache.size()) {
                msgBuilder.addExtra().setText(", ");
            }
        }
        cachedTownList = msgBuilder.build();
    }
}