package mytown.commands.town;

import java.util.ArrayList;
import java.util.List;

import mytown.commands.town.assistant.CmdSetSpawn;
import mytown.commands.town.claim.CmdClaim;
import mytown.commands.town.claim.CmdUnclaim;
import mytown.commands.town.everyone.CmdLeave;
import mytown.commands.town.everyone.CmdNewTown;
import mytown.commands.town.everyone.CmdSelect;
import mytown.commands.town.everyone.CmdSpawn;
import mytown.commands.town.info.CmdBlocks;
import mytown.commands.town.info.CmdInfo;
import mytown.commands.town.info.CmdListTown;
import mytown.commands.town.info.CmdMap;
import mytown.commands.town.invite.CmdInvite;
import mytown.commands.town.perm.CmdPerm;
import mytown.commands.town.perm.CmdPromote;
import mytown.commands.town.plot.CmdPlot;
import mytown.commands.town.plot.CmdPlots;
import mytown.commands.town.rank.CmdRanks;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;

/**
 * MyTown command
 * 
 * @author Joe Goett
 */
@Permission("mytown.cmd")
public class CmdTown extends CommandHandler {
	List<String> aliases = new ArrayList<String>();

	public CmdTown() {
		super("town");

		// Add commands
		addSubCommand(new CmdNewTown("new", this));
		addSubCommand(new CmdClaim("claim", this));
		addSubCommand(new CmdMap("map", this));
		addSubCommand(new CmdListTown("list", this));
		addSubCommand(new CmdInvite("invite", this));
		addSubCommand(new CmdInfo("info", this));
		addSubCommand(new CmdSelect("select", this));
		addSubCommand(new CmdRanks("ranks", this));
		addSubCommand(new CmdBlocks("blocks", this));
		addSubCommand(new CmdUnclaim("unclaim", this));
		addSubCommand(new CmdPlot("plot", this));
		addSubCommand(new CmdPlots("plots", this));
		addSubCommand(new CmdPerm("perm", this));
		addSubCommand(new CmdLeave("leave", this));
		addSubCommand(new CmdPromote("promote", this));
		addSubCommand(new CmdSetSpawn("setspawn", this));
		addSubCommand(new CmdSpawn("spawn", this));

		// Add Aliases
		aliases.add("t");
	}

	@Override
	public boolean canConsoleUse() {
		return true;
	}

	@Override
	public List<?> getCommandAliases() {
		return aliases; // TODO Add aliases!
	}
}