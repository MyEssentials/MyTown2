package mytown.core.utils;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import forgeperms.api.ForgePermsAPI;

// TODO Move to ForgePerms API after the rewrite

public class Assert {
	/**
	 * Checks if command server has the given permission node. Does NOT allow console to access.
	 * 
	 * @param cs
	 * @param node
	 * @throws NoAccessException
	 * @throws CommandException
	 */
	public static void Perm(ICommandSender cs, String node) throws CommandException {
		Assert.Perm(cs, node, false);
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
		if (ForgePermsAPI.permManager.canAccess(p.getCommandSenderName(), p.worldObj.provider.getDimensionName(), node)) // TODO Make ForgePerms use UUIDs (Will need to re-write everything)
			return;
		throw new CommandException("commands.generic.permission");
	}
}