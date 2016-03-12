package mytown.handlers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class MyTownLoadingCallback implements ForgeChunkManager.LoadingCallback {

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        for (ForgeChunkManager.Ticket ticket : tickets) {
            NBTTagList list = ticket.getModData().getTagList("chunkCoords", Constants.NBT.TAG_LIST);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound chunkNBT = list.getCompoundTagAt(i);
                ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(chunkNBT.getInteger("x"), chunkNBT.getInteger("z")));
            }
        }
    }
}
