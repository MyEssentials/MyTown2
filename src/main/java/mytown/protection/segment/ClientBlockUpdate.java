package mytown.protection.segment;

import cpw.mods.fml.common.FMLCommonHandler;
import myessentials.entities.api.BlockPos;
import myessentials.entities.api.Volume;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Sends packets to the client when denying actions.
 */
public class ClientBlockUpdate {

    public final Volume relativeCoords;

    public ClientBlockUpdate(Volume relativeCoords) {
        this.relativeCoords = relativeCoords;
    }

    public ClientBlockUpdate(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        this.relativeCoords = new Volume(xMin, yMin, zMin, xMax, yMax, zMax);
    }

    public void send(BlockPos center, EntityPlayerMP player, ForgeDirection face) {
        World world = MinecraftServer.getServer().worldServerForDimension(center.getDim());
        int x, y, z;
        Volume updateVolume = relativeCoords.translate(face);

        for (int i = updateVolume.getMinX(); i <= updateVolume.getMaxX(); i++) {
            for (int j = updateVolume.getMinY(); j <= updateVolume.getMaxY(); j++) {
                for (int k = updateVolume.getMinZ(); k <= updateVolume.getMaxZ(); k++) {
                    x = center.getX() + i;
                    y = center.getY() + j;
                    z = center.getZ() + k;

                    S23PacketBlockChange packet = new S23PacketBlockChange(x, y, z, world);
                    FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(packet);
                }
            }
        }
    }

    public void send(BlockPos center, EntityPlayerMP player) {
        send(center, player, ForgeDirection.SOUTH);
    }
}
