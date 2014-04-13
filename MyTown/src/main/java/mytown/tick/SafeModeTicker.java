package mytown.tick;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import mytown.Config;
import mytown.MyTown;
import mytown.core.utils.tick.TickBase;

/**
 * Makes sure when safemode is enabled everyone not allowed in is kicked
 * @author Joe Goett
 */
public class SafeModeTicker extends TickBase {
	@Override
	public void run() throws Exception {
		for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			if (!(player instanceof EntityPlayerMP)) return; // Just in-case
			EntityPlayerMP mpPlayer = (EntityPlayerMP) player;
			mpPlayer.playerNetServerHandler.kickPlayerFromServer(Config.safeModeMsg);
		}
	}

	@Override
	public String name() {
		return "SafeModeTicker";
	}
	
	@Override
	public boolean enabled() {
		return MyTown.instance.safemode;
	}
}