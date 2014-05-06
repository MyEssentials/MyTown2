package mytown.core.utils.teleport;

import mytown.core.ChatUtils;
import mytown.core.MyTownCore;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.network.ForgePacket;
import net.minecraftforge.common.network.packet.DimensionRegisterPacket;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Defines a location in the world that can be teleported to
 * @author Joe Goett
 */
public class TeleportLocation {
	protected EntityPlayerMP player = null;
	protected double playerX, playerY, playerZ;
	protected long teleportStamp = 0;
	protected int dim;
	protected double x, y, z;
	protected float pitch, yaw;
	
	public void set(EntityPlayerMP player, long timeToTeleport, int dim, double x, double y, double z, float pitch, float yaw) {
		this.player = player;
		playerX = player.posX;
		playerY = player.posY;
		playerZ = player.posZ;
		teleportStamp = System.currentTimeMillis() + timeToTeleport;
		this.dim = dim;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
	}
	
	public void set(EntityPlayerMP player, long timeToTeleport, int dim, double x, double y, double z) {
		set(player, timeToTeleport, dim, x, y, z, 0, 0);
	}
	
	public void reset() {
		player = null;
		teleportStamp = 0;
		dim = 0;
		x = y = z = 0;
		pitch = yaw = 0;
	}
	
	/**
	 * Teleport the player to this location if the time has run out
	 * @param player
	 */
	public boolean teleportPlayer() {
		if (player == null) return false;
		if (player.posX != playerX || player.posY != playerY || player.posZ != playerZ) {
			ChatUtils.sendChat(player, "Teleport canceled because you moved!"); // TODO Localize?
			return true;
		}
		if (teleportStamp > System.currentTimeMillis()) return false;
		
		WorldServer world = MinecraftServer.getServer().worldServerForDimension(dim);
		
		if (player.dimension != dim) {
			if (MyTownCore.IS_MCPC) {
				Packet250CustomPayload[] dimensionRegisterPackets = ForgePacket.makePacketSet(new DimensionRegisterPacket(dim, DimensionManager.getProviderType(dim)));
	        	for (int i = 0; i < dimensionRegisterPackets.length; i++) {
	        		PacketDispatcher.sendPacketToPlayer(dimensionRegisterPackets[i], (Player)player);
	        	}
			}
			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, dim);
		}
		
		player.setLocationAndAngles(x + 0.5f, y + 0.1f, z + 0.5f, yaw, pitch);

		world.theChunkProviderServer.loadChunk((int) player.posX >> 4, (int) player.posZ >> 4);

		while (!world.getCollidingBoundingBoxes(player, player.boundingBox).isEmpty()) {
			player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
		}

		player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
		
		return true;
	}
}