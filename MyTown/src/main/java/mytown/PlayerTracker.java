package mytown;

import mytown.config.Config;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.flag.EnumFlagValue;
import mytown.proxies.DatasourceProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.BlockEvent;
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
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(player);
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
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(player);
			res.setOnline(false);
			res.setPlayer(null);
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change later?
		}
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer pl) {
		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(pl);
			res.checkLocation(pl.chunkCoordX, pl.chunkCoordZ, pl.dimension);
			if (res.isMapOn()) res.sendMap();
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change?
		}
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		// TODO Auto-generated method stub
	}
	@ForgeSubscribe
	public void onEnterChunk(EntityEvent.EnteringChunk ev) {
		if (!(ev.entity instanceof EntityPlayer)) return;
		if(ev.entity.worldObj.isRemote) return; // So that it's not called twice :P
		EntityPlayer pl = (EntityPlayer) ev.entity;
		try {
			Resident res = DatasourceProxy.getDatasource().getOrMakeResident(pl);
			res.checkLocation(ev.oldChunkX, ev.oldChunkZ, ev.newChunkX, ev.newChunkZ, pl.dimension);
			if (res.isMapOn()) res.sendMap();
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change?
		}
	}
	@ForgeSubscribe
	public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
		// TODO: Implement wilderness perms too
		if(!DatasourceProxy.getDatasource().hasTownBlock(String.format(TownBlock.keyFormat, ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4))) return;
		else {
			TownBlock block = DatasourceProxy.getDatasource().getTownBlock(String.format(TownBlock.keyFormat, ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4));
			EnumFlagValue value = block.getValueForFlagOnBlock(ev.x, ev.y, ev.z, "access");
			if(value != null && value == EnumFlagValue.Build)
				return;
			if(DatasourceProxy.getDatasource().getResident(ev.getPlayer().username).isPartOfTown(block.getTown()))
				return;
			ev.setCanceled(true);
		}
	}
}