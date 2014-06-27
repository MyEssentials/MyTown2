package mytown.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.IPlayerTracker;

public class PlayerTracker implements IPlayerTracker {
	@Override
	public void onPlayerLogin(EntityPlayer player) {
		if (player == null)
			return; // Never know ;)
		if (Config.maintenanceMode && player instanceof EntityPlayerMP) {
			((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer(Config.maintenanceModeMessage);
			return;
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
	}
}