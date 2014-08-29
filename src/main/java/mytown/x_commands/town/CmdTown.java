package mytown.x_commands.town;

import mytown.x_commands.town.assistant.CmdSetSpawn;
import mytown.x_commands.town.claim.CmdClaim;
import mytown.x_commands.town.claim.CmdUnclaim;
import mytown.x_commands.town.everyone.CmdLeave;
import mytown.x_commands.town.everyone.CmdNewTown;
import mytown.x_commands.town.everyone.CmdSelect;
import mytown.x_commands.town.everyone.CmdSpawn;
import mytown.x_commands.town.info.CmdBlocks;
import mytown.x_commands.town.info.CmdInfo;
import mytown.x_commands.town.info.CmdListTown;
import mytown.x_commands.town.info.CmdMap;
import mytown.x_commands.town.invite.CmdInvite;
import mytown.x_commands.town.perm.CmdPromote;
import mytown.x_commands.town.plot.CmdPlot;
import mytown.x_commands.town.plot.CmdPlots;
import mytown.x_commands.town.rank.CmdRanks;
import mytown.core.utils.x_command.CommandHandler;
import mytown.core.utils.x_command.Permission;

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