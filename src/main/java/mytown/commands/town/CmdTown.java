package mytown.commands.town;

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

import java.util.ArrayList;
import java.util.List;

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
        addSubCommand(new CmdNewTown(this));
        addSubCommand(new CmdClaim(this));
        addSubCommand(new CmdMap(this));
        addSubCommand(new CmdListTown(this));
        addSubCommand(new CmdInvite(this));
        addSubCommand(new CmdInfo(this));
        addSubCommand(new CmdSelect(this));
        addSubCommand(new CmdRanks(this));
        addSubCommand(new CmdBlocks(this));
        addSubCommand(new CmdUnclaim(this));
        addSubCommand(new CmdPlot(this));
        addSubCommand(new CmdPlots(this));
        // addSubCommand(new CmdPerm(this)); TODO Redo the perm commands
        addSubCommand(new CmdLeave(this));
        addSubCommand(new CmdPromote(this));
        addSubCommand(new CmdSetSpawn(this));
        addSubCommand(new CmdSpawn(this));

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