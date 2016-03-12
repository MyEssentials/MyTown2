package mytown.api.container;

import mytown.MyTown;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;

public class TicketMap extends HashMap<Integer, ForgeChunkManager.Ticket> {

    private final Town town;

    public TicketMap(Town town) {
        this.town = town;
    }

    @Override
    public ForgeChunkManager.Ticket get(Object key) {
        if (key instanceof Integer) {
            if (super.get(key) == null) {
                World world = DimensionManager.getWorld((Integer) key);
                if (world == null) {
                    return null;
                }

                ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(MyTown.instance, world, ForgeChunkManager.Type.NORMAL);
                ticket.getModData().setString("townName", town.getName());
                ticket.getModData().setTag("chunkCoords", new NBTTagList());
                put((Integer) key, ticket);
                return ticket;
            } else {
                return super.get(key);
            }
        }
        return null;
    }

    public void chunkLoad(TownBlock block) {
        ForgeChunkManager.Ticket ticket = get(block.getDim());
        NBTTagList list = ticket.getModData().getTagList("chunkCoords", Constants.NBT.TAG_LIST);
        list.appendTag(block.toChunkPos().toNBTTagCompound());

        ForgeChunkManager.forceChunk(ticket, block.toChunkCoords());
    }

    public void chunkUnload(TownBlock block) {
        ForgeChunkManager.Ticket ticket = get(block.getDim());
        ForgeChunkManager.unforceChunk(ticket, block.toChunkCoords());

        NBTTagList list = ticket.getModData().getTagList("chunkCoords", Constants.NBT.TAG_LIST);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound chunkNBT = list.getCompoundTagAt(i);
            int x = chunkNBT.getInteger("x");
            int z = chunkNBT.getInteger("z");

            if (x == block.getX() && z == block.getZ()) {
                list.removeTag(i);
                break;
            }
        }
    }

    public void releaseTickets() {
        for (ForgeChunkManager.Ticket ticket : values()) {
            ForgeChunkManager.releaseTicket(ticket);
        }
    }
}
