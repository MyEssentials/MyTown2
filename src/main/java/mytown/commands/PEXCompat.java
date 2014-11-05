package mytown.commands;

import mytown.MyTown;
import mytown.core.utils.command.CommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;
import ru.tehkode.permissions.commands.CommandListener;
import ru.tehkode.permissions.commands.CommandsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 10/29/2014.
 * PermissionsEX implementation goes here.
 */
public class PEXCompat implements CommandListener {

    private CommandsManager manager;
    private static PEXCompat instance;
    public static PEXCompat getInstance() {
        if(instance == null) {
            instance = new PEXCompat();
        }
        return instance;
    }

    public PEXCompat() {
        MyTown.instance.log.info("Did NOT fail registering PEX compatibility. W00t");
    }

    @Command(
            name = "t",
            description = "Town command for all your towny needs.",
            permission = "mytown.cmd",
            syntax = " <command>")
    public void townCommand(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        ICommandSender iCommandSender = convertToForge(sender);
        if(iCommandSender != null) {
            List<String> arguments = new ArrayList<String>(args.values());
            CommandManager.commandCall("mytown.cmd", iCommandSender, arguments);
        }
    }

    @Command(
            name = "ta",
            description = "Town command for all your towny needs.",
            permission = "mytown.adm.cmd",
            syntax = " <command>")
    public void townAdminCommand(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        ICommandSender iCommandSender = convertToForge(sender);
        if(iCommandSender != null) {
            List<String> arguments = new ArrayList<String>(args.values());
            CommandManager.commandCall("mytown.adm.cmd", iCommandSender, arguments);
        }
    }


    @Override
    public void onRegistered(CommandsManager commandsManager) {
        manager = commandsManager;
    }

    @SuppressWarnings("unchecked")
    private ICommandSender convertToForge(CommandSender sender) {
        if(sender instanceof Player) {
            Player player = (Player)sender;
            for(EntityPlayer entityPlayer : (List<EntityPlayer>)MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                if(player.getUniqueId().equals(entityPlayer.getUniqueID())) {
                    return entityPlayer;
                }
            }
        } else if(sender instanceof ConsoleCommandSender) {
            return MinecraftServer.getServer();
        } else if(sender instanceof BlockCommandSender) {
            BlockCommandSender block = (BlockCommandSender) sender;
            World world = getWorldFromName(block.getBlock().getWorld().getName());
            TileEntityCommandBlock commandBlock = (TileEntityCommandBlock)world.getTileEntity(block.getBlock().getX(), block.getBlock().getY(), block.getBlock().getZ());
            return commandBlock.func_145993_a();
        } else if(sender instanceof RemoteConsoleCommandSender) {
            return RConConsoleSource.instance;
        }
        return null;
    }

    private World getWorldFromName(String name) {
        MyTown.instance.log.info("Trying to find world with name: " + name);
        for(World world : MinecraftServer.getServer().worldServers)
            if(world.provider.getDimensionName().equals(name))
                return world;
        return null;
    }


}
