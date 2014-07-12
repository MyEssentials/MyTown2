package mytown.commands.town.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.Permission;
import mytown.entities.comparator.TownComparator;
import mytown.entities.town.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;

import com.google.common.base.Joiner;

// TODO Move cache to another class?

@Permission("mytown.cmd.outsider.list")
public class CmdListTown extends CommandBase {
	private static TownComparator townNameComparator = new TownComparator(TownComparator.Order.Name);
	private static List<Town> sortedTownCache = new ArrayList<Town>();
	private static IChatComponent cachedTownList = null;
	private static String outerJSON = "{'text':'','extra':[%s]}";
	private static String townJSON = "{'text': '%1$s','clickEvent': {'action': 'run_command','value': '/t info %1$s'},'hoverEvent': {'action': 'show_text','value': '%2$s'}}";
	
	public CmdListTown(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public boolean canConsoleUse() {
		return true;
	};

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		sender.addChatMessage(cachedTownList);
	}
	
	public static void updateTownSortCache() {
		if (DatasourceProxy.getDatasource().getTowns(true).size() <= 0) return;
		sortedTownCache.clear();
		sortedTownCache.addAll(DatasourceProxy.getDatasource().getTowns(true));
		Collections.sort(sortedTownCache, townNameComparator);

		List<String> townList = new ArrayList<String>();
		for (int i=0; i<sortedTownCache.size(); i++) {
			Town t = sortedTownCache.get(i);
			townList.add(String.format(townJSON, t.getName(), Joiner.on("").join(t.getInfo())));
			if (i+1 < sortedTownCache.size()) {
				townList.add("{'text': ', '}");
			}
		}
		cachedTownList = IChatComponent.Serializer.func_150699_a(String.format(outerJSON, Joiner.on(", ").join(townList)));
	}
}