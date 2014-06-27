package mytown;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import mytown.interfaces.ITownPlot;
import net.minecraft.block.Block;
import net.minecraft.network.packet.Packet53BlockChange;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.PacketDispatcher;

public class VisualsTickHandler implements ITickHandler {

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

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (tickData.length > 0) {

			if (markedBlocks.size() != 0) {
				Iterator<BlockCoords> iterator = markedBlocks.iterator();

				while (iterator.hasNext()) {
					BlockCoords coord = iterator.next();
					if (!coord.packetSent) {
						Packet53BlockChange packet = new Packet53BlockChange();
						packet.type = Block.blockRedstone.blockID;
						packet.xPosition = coord.x;
						packet.yPosition = coord.y;
						packet.zPosition = coord.z;

						PacketDispatcher.sendPacketToAllInDimension(packet, coord.dim); // Maybe only send to the player selecting the plot

						coord.packetSent = true;
					}
					if (coord.deleted) {
						Packet53BlockChange packet = new Packet53BlockChange();
						packet.type = MinecraftServer.getServer().worldServerForDimension(coord.dim).getBlockId(coord.x, coord.y, coord.z);
						packet.metadata = MinecraftServer.getServer().worldServerForDimension(coord.dim).getBlockMetadata(coord.x, coord.y, coord.z);
						packet.xPosition = coord.x;
						packet.yPosition = coord.y;
						packet.zPosition = coord.z;

						PacketDispatcher.sendPacketToAllInDimension(packet, coord.dim);

						iterator.remove();
					}
				}
			}
		}

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD, TickType.SERVER);
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

	@Override
	public String getLabel() {
		return Constants.TICK_HANDLER_LABEL;
	}
}
