package mytown.commands.admin;

import java.util.List;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.ChatUtils;
import mytown.core.utils.Assert;
import mytown.core.utils.command.CommandBase;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.Permission;
import mytown.handlers.SafemodeHandler;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import forgeperms.api.ForgePermsAPI;

/**
 * Command to enable/disable safemode
 * 
 * @author Joe Goett
 */
@Permission("mytown.adm.cmd.safemode")
public class CmdSafeMode extends CommandBase {

	public CmdSafeMode(String name, CommandBase parent) {
		super(name, parent);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		boolean safemode = false;
		if (args.length < 1) { // Toggle safemode
			safemode = !SafemodeHandler.isInSafemode();
		} else { // Set safemode
			safemode = ChatUtils.equalsOn(args[0]);
		}
		Assert.Perm(sender, "mytown.adm.cmd.safemode." + (safemode ? "on" : "off"));
        SafemodeHandler.setSafemode(safemode);
		CmdSafeMode.kickPlayers();
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
		return CommandUtils.getListOfStringsMatchingLastWord(args, "on", "true", "enable", "off", "false", "disable");
	}

	/**
	 * Kicks all players that can't bypass safemode (mytown.adm.safemode)
	 */
	public static void kickPlayers() {
		if (!MyTown.instance.safemode)
			return;
		for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			if (!(obj instanceof EntityPlayerMP)) {
				CmdSafeMode.kickPlayer((EntityPlayerMP) obj);
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