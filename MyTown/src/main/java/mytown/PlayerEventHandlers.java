package mytown;

import mytown.entities.Resident;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.EventBus;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

public class PlayerEventHandlers extends EventBus{

	@ForgeSubscribe(priority = EventPriority.NORMAL)
	public void onEnterChunk(EntityEvent.EnteringChunk ev) {
		if (!(ev.entity instanceof EntityPlayer)) return;
		if(ev.entity.worldObj.isRemote) return;
		EntityPlayer pl = (EntityPlayer) ev.entity;
		try {
			Resident res = MyTown.instance.datasource.getOrMakeResident(pl);
			res.checkLocation(ev.oldChunkX, ev.oldChunkZ, ev.newChunkX, ev.newChunkZ);
			if (res.isMapOn()) res.sendMap();
		} catch (Exception e) {
			e.printStackTrace(); // TODO Change?
		}
	}
	
}
