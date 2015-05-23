package forgeperms.api;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityCommandBlock;

/**
 * Includes this in your mod. ForgePerms will automatically set the 3 managers.
 *
 * @author Joe Goett
 */
public class ForgePermsAPI {
    public static IChatManager chatManager;
    public static IEconomyManager econManager;
    public static IPermissionManager permManager;

    /**
     * Checks if command server has the given permission node. Does NOT allow console to access.
     *
     * @param cs
     * @param node
     * @throws NoAccessException
     * @throws CommandException
     */
    public static void Perm(ICommandSender cs, String node) throws CommandException {
        ForgePermsAPI.Perm(cs, node, false);
    }

    /**
     * Checks if command sender has the given permission node.
     *
     * @param cs
     * @param node
     * @param allowConsole
     * @throws NoAccessException
     * @throws CommandException
     */
    public static void Perm(ICommandSender cs, String node, boolean allowConsole) throws CommandException {
        if (cs instanceof MinecraftServer || cs instanceof RConConsoleSource || cs instanceof TileEntityCommandBlock) {
            if (allowConsole)
                return;
            else
                throw new CommandException("commands.generic.permission");
        }
        if (node == null)
            return;
        EntityPlayer p = (EntityPlayer) cs;
        if (ForgePermsAPI.permManager.canAccess(p.getDisplayName(), p.worldObj.provider.getDimensionName(), node))
            return;
        throw new CommandException("commands.generic.permission");
    }
}