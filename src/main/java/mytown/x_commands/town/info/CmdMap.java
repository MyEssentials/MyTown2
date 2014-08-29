package mytown.x_commands.town.info;

import mytown.core.ChatUtils;
import mytown.core.utils.x_command.CommandBase;
import mytown.core.utils.x_command.Permission;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.util.Formatter;
import net.minecraft.command.ICommandSender;

import java.util.ArrayList;
import java.util.List;

@Permission("mytown.cmd.outsider.map")
public class CmdMap extends CommandBase {
    private List<String> tabCompletionOptions;

    public CmdMap(CommandBase parent) {
        super("map", parent);

        // Setup tab completion
        tabCompletionOptions = new ArrayList<String>();
        tabCompletionOptions.add("on");
        tabCompletionOptions.add("true");
        tabCompletionOptions.add("enable");
        tabCompletionOptions.add("off");
        tabCompletionOptions.add("false");
        tabCompletionOptions.add("disable");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        Resident res = getDatasource().getOrMakeResident(sender);
        if (args.length == 0) {
            Formatter.sendMap(res);
        } else {
            res.setMapOn(ChatUtils.equalsOn(args[0]));
        }
    }

    @Override
    public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
        return tabCompletionOptions;
    }

    /**
     * Helper method to return the current MyTownDatasource instance
     *
     * @return
     */
    private MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}