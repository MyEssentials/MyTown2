package mytown.handlers;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import mytown.MyTown;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.util.MyTownUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;

import java.util.*;

public class VisualsTickHandler {

    private static VisualsTickHandler instance = new VisualsTickHandler();
    public static VisualsTickHandler getInstance() {
        if(instance == null)
            instance = new VisualsTickHandler();
        return instance;
    }

    public Map<PlayerObjectPair, List<BlockCoords>> markedBlocks = new HashMap<PlayerObjectPair, List<BlockCoords>>();


    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent ev) {
        if (ev.side != Side.SERVER || ev.phase != TickEvent.Phase.START) return;

        if (markedBlocks.size() != 0) {
            for(Map.Entry<PlayerObjectPair, List<BlockCoords>> set : markedBlocks.entrySet()) {
                Iterator<BlockCoords> iterator = set.getValue().iterator();
                while (iterator.hasNext()) {
                    BlockCoords coord = iterator.next();
                    if (!coord.packetSent) {
                        S23PacketBlockChange packet = new S23PacketBlockChange(coord.x, coord.y, coord.z, MinecraftServer.getServer().worldServerForDimension(coord.dim));
                        packet.field_148883_d = coord.block;
                        set.getKey().player.playerNetServerHandler.sendPacket(packet);
                        coord.packetSent = true;
                    }
                    if (coord.deleted) {
                        S23PacketBlockChange packet = new S23PacketBlockChange(coord.x, coord.y, coord.z, MinecraftServer.getServer().worldServerForDimension(coord.dim));
                        packet.field_148883_d = MinecraftServer.getServer().worldServerForDimension(coord.dim).getBlock(coord.x, coord.y, coord.z);
                        packet.field_148884_e = MinecraftServer.getServer().worldServerForDimension(coord.dim).getBlockMetadata(coord.x, coord.y, coord.z);
                        set.getKey().player.playerNetServerHandler.sendPacket(packet);
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void markBlock(int x, int y, int z, int dim, Block block, EntityPlayerMP caller, Object key) {
        PlayerObjectPair pair = new PlayerObjectPair(caller, key);
        if(markedBlocks.get(pair) == null) {
            markedBlocks.put(pair, new ArrayList<BlockCoords>());
        }
        markedBlocks.get(pair).add(new BlockCoords(x, y, z, dim, block));
    }

    /**
     * Unmarks all the blocks that are linked to the player and object key.
     */
    public void unmarkBlocks(Object key, EntityPlayerMP caller) {
        PlayerObjectPair pair = new PlayerObjectPair(caller, key);
        if(markedBlocks.get(pair) == null)
            return;
        for(BlockCoords coord : markedBlocks.get(pair)) {
            coord.deleted = true;
        }
    }

    public void unmarkTowns(EntityPlayerMP caller) {
        for(Map.Entry<PlayerObjectPair, List<BlockCoords>> entry : markedBlocks.entrySet()) {
            if(entry.getKey().player == caller && entry.getKey().object instanceof Town) {
                for(BlockCoords coord : entry.getValue()) {
                    coord.deleted = true;
                }
            }
        }
    }

    public void unmarkPlots(EntityPlayerMP caller) {
        for(Map.Entry<PlayerObjectPair, List<BlockCoords>> entry : markedBlocks.entrySet()) {
            if(entry.getKey().player == caller && entry.getKey().object instanceof Plot) {
                for(BlockCoords coord : entry.getValue()) {
                    coord.deleted = true;
                }
            }
        }
    }

    public void markPlotCorners(int selectionX1, int selectionY1, int selectionZ1, int selectionX2, int selectionY2, int selectionZ2, int dim, EntityPlayerMP caller) {

        markBlock(selectionX1, selectionY1, selectionZ1, dim, Blocks.redstone_block, caller, null);
        markBlock(selectionX2, selectionY2, selectionZ2, dim, Blocks.redstone_block, caller, null);

        // On the X
        markBlock(selectionX1 + (selectionX1 > selectionX2 ? -1 : 1), selectionY1, selectionZ1, dim, Blocks.redstone_block, caller, null);
        markBlock(selectionX2 + (selectionX1 > selectionX2 ? 1 : -1), selectionY2, selectionZ2, dim, Blocks.redstone_block, caller, null);
        markBlock(selectionX1 + (selectionX1 > selectionX2 ? -2 : 2), selectionY1, selectionZ1, dim, Blocks.redstone_block, caller, null);
        markBlock(selectionX2 + (selectionX1 > selectionX2 ? 2 : -2), selectionY2, selectionZ2, dim, Blocks.redstone_block, caller, null);

        // On the Z
        markBlock(selectionX2, selectionY2, selectionZ2 + (selectionZ1 > selectionZ2 ? 1 : -1), dim, Blocks.redstone_block, caller, null);
        markBlock(selectionX1, selectionY1, selectionZ1 + (selectionZ1 > selectionZ2 ? -1 : 1), dim, Blocks.redstone_block, caller, null);
        markBlock(selectionX2, selectionY2, selectionZ2 + (selectionZ1 > selectionZ2 ? 2 : -2), dim, Blocks.redstone_block, caller, null);
        markBlock(selectionX1, selectionY1, selectionZ1 + (selectionZ1 > selectionZ2 ? -2 : 2), dim, Blocks.redstone_block, caller, null);

        if (selectionY1 != selectionY2) {
            // On the Y
            markBlock(selectionX1, selectionY1 + (selectionY1 > selectionY2 ? -1 : 1), selectionZ1, dim, Blocks.redstone_block, caller, null);
            markBlock(selectionX2, selectionY2 + (selectionY1 > selectionY2 ? 1 : -1), selectionZ2, dim, Blocks.redstone_block, caller, null);
            markBlock(selectionX1, selectionY1 + (selectionY1 > selectionY2 ? -2 : 2), selectionZ1, dim, Blocks.redstone_block, caller, null);
            markBlock(selectionX2, selectionY2 + (selectionY1 > selectionY2 ? 2 : -2), selectionZ2, dim, Blocks.redstone_block, caller, null);
        }
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
        addMarkedBlocks(key, caller, blockList);
    }


    public void markTownBorders(Town town, EntityPlayerMP caller) {
        int dx[] = {-1, -1, 0, 1, 1, 1, 0, -1};
        int dz[] = {0, 1, 1, 1, 0, -1, -1, -1};

        int x, y, z;

        List<BlockCoords> blockList = new ArrayList<BlockCoords>();

        for (TownBlock block : town.getBlocks()) {

            // Showing lines in borders
            for(int i = 0; i < 8; i+=2) {
                if(town.getBlockAtCoords(block.getDim(), block.getX() + dx[i], block.getZ() + dz[i]) == null) {
                    if(dx[i] == 0) {
                        z = dz[i] == -1 ? block.getZ() << 4 : (block.getZ() << 4) + 15;
                        x = block.getX() << 4;
                        for(int k = x + 1; k <= x + 14; k++) {
                            y = MyTownUtils.getMaxHeightWithSolid(block.getDim(), k, z);
                            BlockCoords blockCoord = new BlockCoords(k, y, z, block.getDim(), Blocks.lapis_block);
                            blockList.add(blockCoord);
                        }
                    } else {
                        x = dx[i] == -1 ? block.getX() << 4 : (block.getX() << 4) + 15;
                        z = block.getZ() << 4;
                        for(int k = z + 1; k <= z + 14; k++) {
                            y = MyTownUtils.getMaxHeightWithSolid(block.getDim(), x, k);
                            BlockCoords blockCoord = new BlockCoords(x, y, k, block.getDim(), Blocks.lapis_block);
                            blockList.add(blockCoord);
                        }
                    }
                }
            }

            // Showing corners in borders
            for(int i = 1; i < 8; i+=2) {
                x = dx[i] == 1 ? block.getX() << 4 : (block.getX() << 4) + 15;
                z = dz[i] == 1 ? block.getZ() << 4 : (block.getZ() << 4) + 15;
                y = MyTownUtils.getMaxHeightWithSolid(block.getDim(), x, z);
                BlockCoords blockCoord = new BlockCoords(x, y, z, block.getDim(), Blocks.lapis_block);
                blockList.add(blockCoord);
            }
        }
        /*
        for(BlockCoords coord : blockList)
            markBlock(coord.x, coord.y, coord.z, coord.dim, coord.block, caller, town);
            */
        addMarkedBlocks(town, caller, blockList);
    }

    public void updatePlotBorders(Plot plot) {
        List<EntityPlayerMP> callers = new ArrayList<EntityPlayerMP>();
        for(Map.Entry<PlayerObjectPair, List<BlockCoords>> entry : markedBlocks.entrySet()) {
            if(entry.getKey().object.equals(plot)) {
                for(BlockCoords coords : entry.getValue()) {
                    coords.deleted = true;
                }
                callers.add(entry.getKey().player);
            }
        }
        for(EntityPlayerMP player : callers) {
            markPlotBorders(plot, player);
        }
    }

    public void updateTownBorders(Town town) {
        List<EntityPlayerMP> callers = new ArrayList<EntityPlayerMP>();
        for(Map.Entry<PlayerObjectPair, List<BlockCoords>> entry : markedBlocks.entrySet()) {
            if(entry.getKey().object.equals(town)) {
                for(BlockCoords coords : entry.getValue()) {
                    coords.deleted = true;
                }
                callers.add(entry.getKey().player);
            }
        }
        for(EntityPlayerMP player : callers) {
            markTownBorders(town, player);
        }
    }

    /**
     * This is used only externally in case there already is a list of block coords yet to be unmarked
     * This method is gonna wait until the tick function clears the spot.
     */
    public void addMarkedBlocks(final Object key, final EntityPlayerMP caller, final List<BlockCoords> coordsList) {
        // Waits 5 milliseconds if there are still blocks to be deleted.
        Thread t = new Thread() {
            @Override
            public void run() {
                PlayerObjectPair pair = new PlayerObjectPair(caller, key);
                if (markedBlocks.containsKey(pair)) {
                    boolean blocksNotDeleted = true;
                    while (blocksNotDeleted && markedBlocks.get(pair) != null) {
                        blocksNotDeleted = false;
                        for (BlockCoords coords : markedBlocks.get(pair)) {
                            if (coords.deleted)
                                blocksNotDeleted = true;
                        }
                        try {
                            Thread.sleep(5);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                markedBlocks.put(pair, coordsList);
            }
        };
        t.start();
    }

    public List<BlockCoords> getMarkedBlocks() {
        List<BlockCoords> result = new ArrayList<BlockCoords>();
        for(List<BlockCoords> list : markedBlocks.values())
            result.addAll(list);
        return result;
    }

    public boolean isBlockMarked(int x, int y, int z, int dim) {
        for(List<BlockCoords> coordsList : markedBlocks.values()) {
            for (BlockCoords coord : coordsList) {
                if (coord.x == x && coord.y == y && coord.z == z && coord.dim == dim) {
                    coord.packetSent = false;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Class used to store all the blocks that are marked.
     */
    public class BlockCoords {
        public int x, y, z, dim;
        public boolean deleted = false;
        public boolean packetSent = false;
        /**
         * The block to which the sistem should change
         */
        public Block block;

        public BlockCoords(int x, int y, int z, int dim, Block block) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dim = dim;
            this.block = block;
        }
    }

}