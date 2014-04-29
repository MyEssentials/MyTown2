package mytown.commands.admin;

import java.util.List;

import mytown.Config;
import mytown.MyTown;
import mytown.core.utils.Assert;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import mytown.core.utils.command.sub.SubCommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import forgeperms.api.ForgePermsAPI;

/**
 * Command to enable/disable safemode
 * 
 * @author Joe Goett
 */
@Permission(node = "mytown.adm.cmd.safemode")
public class CmdSafeMode extends SubCommandBase {

	public CmdSafeMode(String name) {
		super(name);
	}

	@Override
	public String getPermNode() {
		return "mytown.adm.cmd.safemode";
	}

	@Override
	public void process(ICommandSender sender, String[] args) throws Exception {
		boolean safemode = false;
		if (args.length < 1) { // Toggle safemode
			safemode = !MyTown.instance.safemode;
		} else { // Set safemode
			safemode = (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("enable"));
		}
		Assert.Perm(sender, "mytown.adm.cmd.safemode." + (safemode ? "on" : "off"));
		kickPlayers();
		MyTown.instance.safemode = safemode;
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return CommandUtils.getListOfStringsMatchingLastWord(args, "on", "true", "enable", "off", "false", "disable");
	}

	/**
	 * Kicks all players that can't bypass safemode (mytown.adm.safemode)
	 */
	public static void kickPlayers() {
		for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			if (!(obj instanceof EntityPlayerMP)) {
				kickPlayer((EntityPlayerMP) obj);
			}
		}
	}

	/**
	 * Kicks the given EntityPlayerMP if they dont have mytown.adm.safemode
	 * 
	 * @param pl
	 */
	public static void kickPlayer(EntityPlayerMP pl) {
		if (!ForgePermsAPI.permManager.canAccess(pl.getCommandSenderName(), pl.worldObj.provider.getDimensionName(), "mytown.adm.safemode")) {
			pl.playerNetServerHandler.kickPlayerFromServer(Config.safeModeMsg);
		}
	}
}