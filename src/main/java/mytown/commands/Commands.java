package mytown.commands;

import mytown.core.Localization;
import mytown.core.MyTownCore;
import mytown.core.utils.command.CommandManager;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.command.ICommandSender;

import java.util.List;

/**
 * Created by AfterWind on 8/29/2014.
 * Base class for all classes that hold command methods... Mostly for some utils
 */
public abstract class Commands {
    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
    public static MyTownUniverse getUniverse() {
        return MyTownUniverse.getInstance();
    }
    public static Localization getLocal() { return LocalizationProxy.getLocalization(); }
    public static boolean callSubFunctions(ICommandSender sender, List<String> args, List<String> subCommands, String callersPermNode) {
        if(args.size() > 0) {
            for(String s : subCommands) {
                String name = CommandManager.commandNames.get(s);
                // Checking if name corresponds and if parent's corresponds
                MyTownCore.Instance.log.info("Found subcommand with node " + s);
                if(name.equals(args.get(0)) && CommandManager.getParentPermNode(s).equals(callersPermNode)) {
                    CommandManager.commandCall(s, sender, args.subList(1, args.size()));
                    return true;
                }
            }
        } else {
            MyTownCore.Instance.log.info("Nothing found...");
            // TODO: Give help
        }
        return false;
    }
}
