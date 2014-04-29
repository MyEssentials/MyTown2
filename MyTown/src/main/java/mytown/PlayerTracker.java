package mytown;

import mytown.entities.Resident;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import cpw.mods.fml.common.IPlayerTracker;
import forgeperms.api.ForgePermsAPI;

public class PlayerTracker implements IPlayerTracker {
	@Override
	public void onPlayerLogin(EntityPlayer player) {
		if (player == null) return; // Never know ;)
		if (MyTown.instance.safemode && player instanceof EntityPlayerMP && !ForgePermsAPI.permManager.canAccess(player.username, player.worldObj.provider.getDimensionName(), "mytown.adm.safemode")) {
			((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer(Config.safeModeMsg);
			return;
		}

		try {
			Resident res = MyTown.instance.datasource.getOrMakeResident(player);
			res.setOnline(true);
			res.setPlayer(player);
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change later?
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		if (player == null) return; // Never know ;)
		try {
			Resident res = MyTown.instance.datasource.getOrMakeResident(player);
			res.setOnline(false);
			res.setPlayer(null);
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change later?
		}
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		// TODO Auto-generated method stub
	}

	@ForgeSubscribe
	public void onEnterChunk(EntityEvent.EnteringChunk ev) {
		if (!(ev.entity instanceof EntityPlayer)) return;
		EntityPlayer pl = (EntityPlayer) ev.entity;
		try {
			Resident res = MyTown.instance.datasource.getOrMakeResident(pl);
			// TODO Tell player if entered town/plot/wild/etc.
			if (res.isMapOn()) res.sendMap();
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change?
		}
	}
}