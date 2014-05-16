package mytown.core.utils.teleport;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class TeleportHandler implements IScheduledTickHandler {
	public static TeleportHandler teleportHandler = new TeleportHandler();

	public static void init() {
		TickRegistry.registerScheduledTickHandler(TeleportHandler.teleportHandler, Side.SERVER);
	}

	private TeleportHandler() {}

	private List<TeleportLocation> teleportations = new ArrayList<TeleportLocation>();

	public void teleport(EntityPlayerMP pl, int dim, long timeToTeleport, double x, double y, double z, float pitch, float yaw) {
		TeleportLocation loc = obtain();
		loc.set(pl, timeToTeleport, dim, x, y, z, pitch, yaw);
		teleportations.add(loc);
	}

	public void teleport(EntityPlayerMP pl, int dim, long timeToTeleport, double x, double y, double z) {
		TeleportLocation loc = obtain();
		loc.set(pl, timeToTeleport, dim, x, y, z);
		teleportations.add(loc);
	}

	// /////////////////////
	// IScheduledTickHandler
	// /////////////////////

	@Override
	public String getLabel() {
		return "MyTownCore - TeleportHandler";
	}

	@Override
	public void tickStart(EnumSet<TickType> arg0, Object... tickData) {
		for (TeleportLocation loc : teleportations) {
			loc.teleportPlayer();
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> arg0, Object... tickData) {}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public int nextTickSpacing() {
		return 5;
	}

	// /////////////////////
	// Pool
	// /////////////////////

	private List<TeleportLocation> cache = new ArrayList<TeleportLocation>();

	public TeleportLocation obtain() {
		return (cache.size() > 0) ? cache.remove(cache.size()) : new TeleportLocation();
	}

	public void free(TeleportLocation obj) {
		obj.reset();
		cache.add(obj);
		teleportations.remove(obj);
	}
}