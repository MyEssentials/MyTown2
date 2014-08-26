package mytown.commands.admin;

import mytown.commands.admin.config.CmdConfig;
import mytown.core.utils.command.CommandHandler;
import mytown.core.utils.command.Permission;

import java.util.ArrayList;
import java.util.List;

@Permission("mytown.adm.cmd")
public class CmdTownAdmin extends CommandHandler {
    List<String> aliases = new ArrayList<String>();

    public CmdTownAdmin() {
        super("townadmin", null);

        // Subcommands
        addSubCommand(new CmdSafeMode("safemode", this));
        addSubCommand(new CmdDelete("delete", this));
        addSubCommand(new CmdRem("rem", this));
        addSubCommand(new CmdAdd("add", this));
        addSubCommand(new CmdNewTown("new", this));
        addSubCommand(new CmdConfig(this));

        // Add aliases
        aliases.add("ta");
    }

    @Override
    public List<?> getCommandAliases() {
        return aliases;
    }
}