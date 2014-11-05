package mytown.proxies.plugin;

import mytown.MyTown;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
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
import ru.tehkode.permissions.PermissionUser;
//import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.List;
import java.util.Set;

/**
 * Created by AfterWind on 10/29/2014.
 * PermissionsEX implementation goes here.
 */
public class PEXCompat {

    private static PEXCompat instance;
    public static PEXCompat getInstance() {
        if(instance == null) {
            instance = new PEXCompat();
        }
        return instance;
    }

    public static boolean firstPermissionBreachPEX(String permission, ICommandSender sender) {
        if(!(sender instanceof EntityPlayer))
            return true;



        PermissionUser user =  null; //ru.tehkode.permissions.bukkit.PermissionsEx.getUser(Bukkit.getPlayer(((EntityPlayer) sender).getUniqueID()));

        try {
            Class c = Class.forName("ru.tehkode.permissions.bukkit.PermissionsEx");
            MyTown.instance.log.info("Found class!");
        } catch (Exception e) {
            e.printStackTrace();
        }


        if(user == null)
            return false;

        MyTown.instance.log.info("Got user: " + user.getName());

        if (!user.has(permission)) {
            return false;
        }

        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(sender);
        // Get its rank with the permissions
        Rank rank = res.getTownRank(res.getSelectedTown());

        if (rank == null) {
            return Rank.outsiderPermCheck(permission);
        }
        return rank.hasPermissionOrSuperPermission(permission);
    }

    public PEXCompat() {
        MyTown.instance.log.info("Did NOT fail registering PEX compatibility. W00t");
    }

    public boolean checkPermission(Resident res, String permission) {
        PermissionUser user = null;//PermissionsEx.getUser(Bukkit.getPlayer(res.getUUID()));
        if(user == null)
            return false;

        MyTown.instance.log.info("Got user: " + user.getName());

        return user.has(permission);
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
