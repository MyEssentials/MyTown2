package mytown.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mytown.MyTown;
import myessentials.utils.WorldUtils;
import mytown.api.container.TownBlocksContainer;
import mytown.entities.Plot;
import mytown.entities.TownBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.*;

public class VisualsHandler {

    public static final VisualsHandler instance = new VisualsHandler();

    private final List<VisualObject> markedBlocks = new ArrayList<VisualObject>();

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent ev) {
        if (ev.side != Side.SERVER || ev.phase != TickEvent.Phase.START)
            return;

        if (!markedBlocks.isEmpty()) {
            for(Iterator<VisualObject> visualObjectIterator = markedBlocks.iterator(); visualObjectIterator.hasNext(); ) {
                VisualObject visualObject = visualObjectIterator.next();

                for (Iterator<BlockCoords> blockCoordsIterator = visualObject.blockCoords.iterator(); blockCoordsIterator.hasNext(); ) {
                    BlockCoords coord = blockCoordsIterator.next();
                    if (!coord.packetSent) {
                        S23PacketBlockChange packet = new S23PacketBlockChange(coord.x, coord.y, coord.z, MinecraftServer.getServer().worldServerForDimension(coord.dim));
                        packet.field_148883_d = coord.block;
                        visualObject.player.playerNetServerHandler.sendPacket(packet);
                        coord.packetSent = true;
                    }
                    if (coord.deleted) {
                        S23PacketBlockChange packet = new S23PacketBlockChange(coord.x, coord.y, coord.z, MinecraftServer.getServer().worldServerForDimension(coord.dim));
                        packet.field_148883_d = MinecraftServer.getServer().worldServerForDimension(coord.dim).getBlock(coord.x, coord.y, coord.z);
                        packet.field_148884_e = MinecraftServer.getServer().worldServerForDimension(coord.dim).getBlockMetadata(coord.x, coord.y, coord.z);
                        visualObject.player.playerNetServerHandler.sendPacket(packet);
                        blockCoordsIterator.remove();
                    }
                }
                if (visualObject.blockCoords.isEmpty())
                    visualObjectIterator.remove();
            }
        }
    }

    public void markBlock(int x, int y, int z, int dim, Block block, EntityPlayerMP caller, Object key) {
        BlockCoords singleCoord = new BlockCoords(x, y, z, dim, block);
        for(VisualObject visualObject : markedBlocks) {
            if(visualObject.player == caller && visualObject.object == key) {
                visualObject.blockCoords.add(singleCoord);
                return;
            }
        }

        List<BlockCoords> blockCoords = new ArrayList<BlockCoords>();
        blockCoords.add(singleCoord);
        markedBlocks.add(new VisualObject(caller, key, blockCoords));
    }

    public void markBlocks(EntityPlayerMP caller, Object key, List<BlockCoords> blockList) {
        for(VisualObject visualObject : markedBlocks) {
            if(visualObject.player == caller && visualObject.object == key) {
                visualObject.blockCoords.addAll(blockList);
                return;
            }
        }

        markedBlocks.add(new VisualObject(caller, key, blockList));
    }

    public void markCorners(int selectionX1, int selectionY1, int selectionZ1, int selectionX2, int selectionY2, int selectionZ2, int dim, EntityPlayerMP caller) {

        List<BlockCoords> blockList = new ArrayList<BlockCoords>();

        blockList.add(new BlockCoords(selectionX1, selectionY1, selectionZ1, dim, Blocks.redstone_block));
        blockList.add(new BlockCoords(selectionX2, selectionY2, selectionZ2, dim, Blocks.redstone_block));

        // On the X
        blockList.add(new BlockCoords(selectionX1 + (selectionX1 > selectionX2 ? -1 : 1), selectionY1, selectionZ1, dim, Blocks.redstone_block));
        blockList.add(new BlockCoords(selectionX2 + (selectionX1 > selectionX2 ? 1 : -1), selectionY2, selectionZ2, dim, Blocks.redstone_block));
        blockList.add(new BlockCoords(selectionX1 + (selectionX1 > selectionX2 ? -2 : 2), selectionY1, selectionZ1, dim, Blocks.redstone_block));
        blockList.add(new BlockCoords(selectionX2 + (selectionX1 > selectionX2 ? 2 : -2), selectionY2, selectionZ2, dim, Blocks.redstone_block));

        // On the Z
        blockList.add(new BlockCoords(selectionX2, selectionY2, selectionZ2 + (selectionZ1 > selectionZ2 ? 1 : -1), dim, Blocks.redstone_block));
        blockList.add(new BlockCoords(selectionX1, selectionY1, selectionZ1 + (selectionZ1 > selectionZ2 ? -1 : 1), dim, Blocks.redstone_block));
        blockList.add(new BlockCoords(selectionX2, selectionY2, selectionZ2 + (selectionZ1 > selectionZ2 ? 2 : -2), dim, Blocks.redstone_block));
        blockList.add(new BlockCoords(selectionX1, selectionY1, selectionZ1 + (selectionZ1 > selectionZ2 ? -2 : 2), dim, Blocks.redstone_block));

        if (selectionY1 != selectionY2) {
            // On the Y
            blockList.add(new BlockCoords(selectionX1, selectionY1 + (selectionY1 > selectionY2 ? -1 : 1), selectionZ1, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(selectionX2, selectionY2 + (selectionY1 > selectionY2 ? 1 : -1), selectionZ2, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(selectionX1, selectionY1 + (selectionY1 > selectionY2 ? -2 : 2), selectionZ1, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(selectionX2, selectionY2 + (selectionY1 > selectionY2 ? 2 : -2), selectionZ2, dim, Blocks.redstone_block));
        }

        // Marking it to itself since null would not be possible
        markBlocks(caller, caller, blockList);
    }

    public void markPlotBorders(Plot plot, EntityPlayerMP caller) {
        markPlotBorders(plot.getStartX(), plot.getStartY(), plot.getStartZ(), plot.getEndX(), plot.getEndY(), plot.getEndZ(), plot.getDim(), caller, plot);
    }

    public void markPlotBorders(int x1, int y1, int z1, int x2, int y2, int z2, int dim, EntityPlayerMP caller, Object key) {
        // assuming x1 < x2, y1 < y2, z1 < z2
        List<BlockCoords> blockList = new ArrayList<BlockCoords>();
        for (int i = x1; i <= x2; i++) {
            blockList.add(new BlockCoords(i, y1, z1, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(i, y2, z1, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(i, y1, z2, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(i, y2, z2, dim, Blocks.redstone_block));
        }
        for (int i = y1; i <= y2; i++) {
            blockList.add(new BlockCoords(x1, i, z1, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(x2, i, z1, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(x1, i, z2, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(x2, i, z2, dim, Blocks.redstone_block));
        }
        for (int i = z1; i <= z2; i++) {
            blockList.add(new BlockCoords(x1, y1, i, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(x2, y1, i, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(x1, y2, i, dim, Blocks.redstone_block));
            blockList.add(new BlockCoords(x2, y2, i, dim, Blocks.redstone_block));
        }
        addMarkedBlocks(caller, key, blockList);
    }


    public void markTownBorders(TownBlocksContainer townBlocksContainer, EntityPlayerMP caller) {
        int[] dx = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] dz = {0, 1, 1, 1, 0, -1, -1, -1};

        int x, y, z;

        List<BlockCoords> blockList = new ArrayList<BlockCoords>();

        for (TownBlock block : townBlocksContainer) {

            // Showing lines in borders
            for (int i = 0; i < 8; i += 2) {
                if (townBlocksContainer.get(block.getDim(), block.getX() + dx[i], block.getZ() + dz[i]) == null) {
                    if (dx[i] == 0) {
                        z = dz[i] == -1 ? block.getZ() << 4 : (block.getZ() << 4) + 15;
                        x = block.getX() << 4;
                        for (int k = x + 1; k <= x + 14; k++) {
                            y = WorldUtils.getMaxHeightWithSolid(block.getDim(), k, z);
                            BlockCoords blockCoord = new BlockCoords(k, y, z, block.getDim(), Blocks.lapis_block);
                            blockList.add(blockCoord);
                        }
                    } else {
                        x = dx[i] == -1 ? block.getX() << 4 : (block.getX() << 4) + 15;
                        z = block.getZ() << 4;
                        for (int k = z + 1; k <= z + 14; k++) {
                            y = WorldUtils.getMaxHeightWithSolid(block.getDim(), x, k);
                            BlockCoords blockCoord = new BlockCoords(x, y, k, block.getDim(), Blocks.lapis_block);
                            blockList.add(blockCoord);
                        }
                    }
                }
            }

            // Showing corners in borders
            for (int i = 1; i < 8; i += 2) {
                x = dx[i] == 1 ? block.getX() << 4 : (block.getX() << 4) + 15;
                z = dz[i] == 1 ? block.getZ() << 4 : (block.getZ() << 4) + 15;
                y = WorldUtils.getMaxHeightWithSolid(block.getDim(), x, z);
                BlockCoords blockCoord = new BlockCoords(x, y, z, block.getDim(), Blocks.lapis_block);
                blockList.add(blockCoord);
            }
        }

        addMarkedBlocks(caller, townBlocksContainer, blockList);
    }

    /**
     * This method is gonna wait until the tick function clears the spot.
     */
    public void addMarkedBlocks(final EntityPlayerMP caller, final Object key, final List<BlockCoords> blockCoords) {
        // Waits 5 milliseconds if there are still blocks to be deleted.
        Thread t = new Thread() {
            @Override
            public void run() {

                VisualObject visualObject = null;
                for(VisualObject marked : markedBlocks) {
                    if(marked.player == caller && marked.object == key) {
                        visualObject = marked;
                    }
                }

                if (visualObject != null) {
                    boolean blocksNotDeleted = true;
                    while (blocksNotDeleted && markedBlocks.contains(visualObject)) {
                        blocksNotDeleted = false;
                        for (BlockCoords coords : visualObject.blockCoords) {
                            if (coords != null && coords.deleted) {
                                blocksNotDeleted = true;
                            }
                        }
                        try {
                            Thread.sleep(5);
                        } catch (Exception ex) {
                            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
                        }
                    }
                    visualObject.blockCoords.addAll(blockCoords);
                } else {
                    visualObject = new VisualObject(caller, key, blockCoords);
                    markedBlocks.add(visualObject);
                }
            }
        };
        t.start();
    }

    public void updatePlotBorders(Plot plot) {
        List<EntityPlayerMP> callers = new ArrayList<EntityPlayerMP>();
        for(VisualObject visualObject : markedBlocks) {
            if(visualObject.isPlot() && visualObject.object.equals(plot)) {
                for(BlockCoords coords : visualObject.blockCoords) {
                    coords.deleted = true;
                }
                callers.add(visualObject.player);
            }
        }
        for(EntityPlayerMP player : callers) {
            markPlotBorders(plot, player);
        }
    }

    public void updateTownBorders(TownBlocksContainer townBlocksContainer) {
        List<EntityPlayerMP> callers = new ArrayList<EntityPlayerMP>();
        for(VisualObject visualObject : markedBlocks) {
            if(visualObject.isTown() && visualObject.object.equals(townBlocksContainer)) {
                for(BlockCoords coords : visualObject.blockCoords) {
                    coords.deleted = true;
                }
                callers.add(visualObject.player);
            }
        }
        for(EntityPlayerMP player : callers) {
            markTownBorders(townBlocksContainer, player);
        }
    }

    /**
     * Unmarks all the blocks that are linked to the player and object key.
     */
    public synchronized void unmarkBlocks(EntityPlayerMP caller, Object key) {
        for(VisualObject visualObject : markedBlocks) {
            if(visualObject.player == caller && visualObject.object == key) {
                for(BlockCoords blockCoords : visualObject.blockCoords)
                    blockCoords.deleted = true;
            }
        }

    }

    public synchronized void unmarkBlocks(Object key) {
        for(VisualObject visualObject : markedBlocks) {
            if(visualObject.object == key) {
                for(BlockCoords blockCoords : visualObject.blockCoords)
                    blockCoords.deleted = true;
            }
        }
    }

    public void unmarkTowns(EntityPlayerMP caller) {
        for(VisualObject visualObject : markedBlocks) {
            if(visualObject.player == caller && visualObject.isTown()) {
                for(BlockCoords blockCoords : visualObject.blockCoords)
                    blockCoords.deleted = true;
            }
        }
    }

    public void unmarkPlots(EntityPlayerMP caller) {
        for(VisualObject visualObject : markedBlocks) {
            if(visualObject.player == caller && visualObject.isPlot()) {
                for(BlockCoords blockCoords : visualObject.blockCoords)
                    blockCoords.deleted = true;
            }
        }
    }

    public boolean isBlockMarked(int x, int y, int z, int dim, EntityPlayerMP player) {
        for(VisualObject visualObject : markedBlocks) {
            if(visualObject.player == player) {
                for (BlockCoords coord : visualObject.blockCoords) {
                    if (coord.x == x && coord.y == y && coord.z == z && coord.dim == dim) {
                        coord.packetSent = false;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Class used to store all the blocks that are marked.
     */
    private class BlockCoords {
        private final int x;
        private final int y;
        private final int z;
        private final int dim;
        private boolean deleted = false;
        private boolean packetSent = false;
        /**
         * The block to which the sistem should change
         */
        private final Block block;

        public BlockCoords(int x, int y, int z, int dim, Block block) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dim = dim;
            this.block = block;
        }
    }

    private class VisualObject {
        private EntityPlayerMP player;
        private Object object;
        private List<BlockCoords> blockCoords;

        public VisualObject(EntityPlayerMP player, Object object, List<BlockCoords> blockCoords) {
            this.player = player;
            this.object = object;
            this.blockCoords = blockCoords;
        }

        public boolean isTown() {
            return object != null && object instanceof TownBlocksContainer;
        }

        public boolean isPlot() {
            return object != null && object instanceof Plot;
        }
    }
}