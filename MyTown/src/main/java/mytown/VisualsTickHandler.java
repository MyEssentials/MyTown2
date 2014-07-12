package mytown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mytown.interfaces.ITownPlot;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

public class VisualsTickHandler {
	private class BlockCoords {
		public int x, y, z, dim;
		public boolean deleted = false;
		public boolean packetSent = false;

		public BlockCoords(int x, int y, int z, int dim) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.dim = dim;
		}
	}

	public static VisualsTickHandler instance = new VisualsTickHandler();
	private List<BlockCoords> markedBlocks;

	public VisualsTickHandler() {
		markedBlocks = new ArrayList<BlockCoords>();
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent ev) {
		if (ev.side != Side.SERVER || ev.phase != TickEvent.Phase.START) return;
		if (markedBlocks.size() != 0) {
			Iterator<BlockCoords> iterator = markedBlocks.iterator();

			while (iterator.hasNext()) {
				BlockCoords coord = iterator.next();
				if (!coord.packetSent) {
                    S23PacketBlockChange packet = new S23PacketBlockChange(coord.x, coord.y, coord.z, MinecraftServer.getServer().worldServerForDimension(coord.dim));
					packet.field_148883_d = Blocks.redstone_block;
					FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(packet);

					coord.packetSent = true;
				}
				if (coord.deleted) {
                    S23PacketBlockChange packet = new S23PacketBlockChange(coord.x, coord.y, coord.z, MinecraftServer.getServer().worldServerForDimension(coord.dim));
					packet.field_148883_d = MinecraftServer.getServer().worldServerForDimension(coord.dim).getBlock(coord.x, coord.y, coord.z);
					packet.field_148884_e = MinecraftServer.getServer().worldServerForDimension(coord.dim).getBlockMetadata(coord.x, coord.y, coord.z);
					FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(packet);

					iterator.remove();
				}
			}
		}
	}

	public void markBlock(int x, int y, int z, int dim) {
		markedBlocks.add(new BlockCoords(x, y, z, dim));
	}

	public boolean unmarkBlock(int x, int y, int z, int dim) {
		if (markedBlocks.size() == 0)
			return false;
		for (BlockCoords coord : markedBlocks) {
			if (coord.x == x && coord.y == y && coord.z == z && coord.dim == dim) {
				coord.deleted = true;
				return true;
			}
		}
		return false;
	}

	public void markPlotCorners(int selectionX1, int selectionY1, int selectionZ1, int selectionX2, int selectionY2, int selectionZ2, int dim) {

		markBlock(selectionX1, selectionY1, selectionZ1, dim);
		markBlock(selectionX2, selectionY2, selectionZ2, dim);

		// On the X
		markBlock(selectionX1 + (selectionX1 > selectionX2 ? -1 : 1), selectionY1, selectionZ1, dim);
		markBlock(selectionX2 + (selectionX1 > selectionX2 ? 1 : -1), selectionY2, selectionZ2, dim);
		markBlock(selectionX1 + (selectionX1 > selectionX2 ? -2 : 2), selectionY1, selectionZ1, dim);
		markBlock(selectionX2 + (selectionX1 > selectionX2 ? 2 : -2), selectionY2, selectionZ2, dim);

		// On the Z
		markBlock(selectionX2, selectionY2, selectionZ2 + (selectionZ1 > selectionZ2 ? 1 : -1), dim);
		markBlock(selectionX1, selectionY1, selectionZ1 + (selectionZ1 > selectionZ2 ? -1 : 1), dim);
		markBlock(selectionX2, selectionY2, selectionZ2 + (selectionZ1 > selectionZ2 ? 2 : -2), dim);
		markBlock(selectionX1, selectionY1, selectionZ1 + (selectionZ1 > selectionZ2 ? -2 : 2), dim);

		if (selectionY1 != selectionY2) {
			// On the Y
			markBlock(selectionX1, selectionY1 + (selectionY1 > selectionY2 ? -1 : 1), selectionZ1, dim);
			markBlock(selectionX2, selectionY2 + (selectionY1 > selectionY2 ? 1 : -1), selectionZ2, dim);
			markBlock(selectionX1, selectionY1 + (selectionY1 > selectionY2 ? -2 : 2), selectionZ1, dim);
			markBlock(selectionX2, selectionY2 + (selectionY1 > selectionY2 ? 2 : -2), selectionZ2, dim);
		}
	}

	public void unmarkPlotCorners(int selectionX1, int selectionY1, int selectionZ1, int selectionX2, int selectionY2, int selectionZ2, int dim) {

		unmarkBlock(selectionX1, selectionY1, selectionZ1, dim);
		unmarkBlock(selectionX2, selectionY2, selectionZ2, dim);

		// On the X
		unmarkBlock(selectionX1 + (selectionX1 > selectionX2 ? -1 : 1), selectionY1, selectionZ1, dim);
		unmarkBlock(selectionX2 + (selectionX1 > selectionX2 ? 1 : -1), selectionY2, selectionZ2, dim);
		unmarkBlock(selectionX1 + (selectionX1 > selectionX2 ? -2 : 2), selectionY1, selectionZ1, dim);
		unmarkBlock(selectionX2 + (selectionX1 > selectionX2 ? 2 : -2), selectionY2, selectionZ2, dim);

		// On the Z
		unmarkBlock(selectionX2, selectionY2, selectionZ2 + (selectionZ1 > selectionZ2 ? 1 : -1), dim);
		unmarkBlock(selectionX1, selectionY1, selectionZ1 + (selectionZ1 > selectionZ2 ? -1 : 1), dim);
		unmarkBlock(selectionX2, selectionY2, selectionZ2 + (selectionZ1 > selectionZ2 ? 2 : -2), dim);
		unmarkBlock(selectionX1, selectionY1, selectionZ1 + (selectionZ1 > selectionZ2 ? -2 : 2), dim);

		if (selectionY1 != selectionY2) {
			// On the Y
			unmarkBlock(selectionX1, selectionY1 + (selectionY1 > selectionY2 ? -1 : 1), selectionZ1, dim);
			unmarkBlock(selectionX2, selectionY2 + (selectionY1 > selectionY2 ? 1 : -1), selectionZ2, dim);
			unmarkBlock(selectionX1, selectionY1 + (selectionY1 > selectionY2 ? -2 : 2), selectionZ1, dim);
			unmarkBlock(selectionX2, selectionY2 + (selectionY1 > selectionY2 ? 2 : -2), selectionZ2, dim);
		}
	}

	public void markPlotBorders(ITownPlot plot) {
		markPlotBorders(plot.getStartX(), plot.getStartY(), plot.getStartZ(), plot.getEndX(), plot.getEndY(), plot.getEndZ(), plot.getDim());
	}

	public void markPlotBorders(int x1, int y1, int z1, int x2, int y2, int z2, int dim) {
		// assuming x1 < x2, y1 < y2, z1 < z2

		for (int i = x1; i <= x2; i++) {
			markBlock(i, y1, z1, dim);
			markBlock(i, y2, z1, dim);
			markBlock(i, y1, z2, dim);
			markBlock(i, y2, z2, dim);
		}
		for (int i = y1; i <= y2; i++) {
			markBlock(x1, i, z1, dim);
			markBlock(x2, i, z1, dim);
			markBlock(x1, i, z2, dim);
			markBlock(x2, i, z2, dim);
		}
		for (int i = z1; i <= z2; i++) {
			markBlock(x1, y1, i, dim);
			markBlock(x2, y1, i, dim);
			markBlock(x1, y2, i, dim);
			markBlock(x2, y2, i, dim);
		}
	}

	public void unmarkPlotBorders(ITownPlot plot) {
		unmarkPlotBorders(plot.getStartX(), plot.getStartY(), plot.getStartZ(), plot.getEndX(), plot.getEndY(), plot.getEndZ(), plot.getDim());
	}

	public void unmarkPlotBorders(int x1, int y1, int z1, int x2, int y2, int z2, int dim) {
		// assuming x1 < x2, y1 < y2, z1 < z2

		for (int i = x1; i <= x2; i++) {
			unmarkBlock(i, y1, z1, dim);
			unmarkBlock(i, y2, z1, dim);
			unmarkBlock(i, y1, z2, dim);
			unmarkBlock(i, y2, z2, dim);
		}
		for (int i = y1; i <= y2; i++) {
			unmarkBlock(x1, i, z1, dim);
			unmarkBlock(x2, i, z1, dim);
			unmarkBlock(x1, i, z2, dim);
			unmarkBlock(x2, i, z2, dim);
		}
		for (int i = z1; i <= z2; i++) {
			unmarkBlock(x1, y1, i, dim);
			unmarkBlock(x2, y1, i, dim);
			unmarkBlock(x1, y2, i, dim);
			unmarkBlock(x2, y2, i, dim);
		}
	}
}
