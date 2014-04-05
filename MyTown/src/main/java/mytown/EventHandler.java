package mytown;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.IPlayerTracker;

public class EventHandler implements IPlayerTracker {
	@Override
	public void onPlayerLogin(EntityPlayer player) {
		if (player == null) return; // Never know ;)
		if (MyTown.INSTANCE.safemode && player instanceof EntityPlayerMP) {
			((EntityPlayerMP)player).playerNetServerHandler.kickPlayerFromServer("MyTown is in safe mode. Please tell a server admin!");  // TODO Make configurable
			return;
		}
		
		try {
			MyTown.INSTANCE.datasource.getOrMakeResident(player);
		} catch (Exception e) {
			e.printStackTrace();  // TODO Change later?
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		// TODO Auto-generated method stub
	}
}