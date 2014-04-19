package mytown.commands.admin;

import java.util.List;

import forgeperms.api.ForgePermsAPI;
import mytown.Config;
import mytown.MyTown;
import mytown.core.utils.Assert;
import mytown.core.utils.command.CommandUtils;
import mytown.core.utils.command.sub.SubCommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class SafeMode extends SubCommandBase {
	@Override
	public String getName() {
		return "safemode";
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
		// TODO Kick anyone that shouldn't be on
		MyTown.instance.safemode = safemode;
	}

	@Override
	public List<String> tabComplete(ICommandSender sender, String[] args) {
		return CommandUtils.getListOfStringsMatchingLastWord(args, "on", "true", "enable", "off", "false", "disable");
	}
	
	protected void kickPlayers() {
		for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			if (!(obj instanceof EntityPlayerMP)) {
				EntityPlayerMP pl = (EntityPlayerMP) obj;
				if (!ForgePermsAPI.permManager.canAccess(pl.getCommandSenderName(), pl.worldObj.provider.getDimensionName(), "mytown.adm.safemode")) {
					pl.playerNetServerHandler.kickPlayerFromServer(Config.safeModeMsg);
				}
			}
		}
	}
}