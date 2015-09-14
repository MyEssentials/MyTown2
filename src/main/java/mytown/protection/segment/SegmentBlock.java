package mytown.protection.segment;

import myessentials.entities.BlockPos;
import myessentials.entities.Volume;
import mytown.api.container.GettersContainer;
import mytown.entities.Resident;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.BlockType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Offers protection for blocks
 */
public class SegmentBlock extends Segment {
    private final int meta;
    public final ClientBlockUpdate clientUpdate;
    public final List<BlockType> types = new ArrayList<BlockType>();

    public SegmentBlock(int meta, Volume clientUpdateCoords) {
        this.meta = meta;
        if(clientUpdateCoords != null) {
            this.clientUpdate = new ClientBlockUpdate(clientUpdateCoords);
        } else {
            this.clientUpdate = null;
        }
    }

    public boolean shouldInteract(Resident res, BlockPos bp, PlayerInteractEvent.Action action) {
        if(meta != -1 && meta != MinecraftServer.getServer().worldServerForDimension(bp.getDim()).getBlockMetadata(bp.getX(), bp.getY(), bp.getZ())) {
            return true;
        }

        if((action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && !types.contains(BlockType.LEFT_CLICK)
                || action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && !types.contains(BlockType.RIGHT_CLICK))
                && !types.contains(BlockType.ANY_CLICK)) {
            return true;
        }

        if (!hasPermissionAtLocation(res, bp.getDim(), bp.getX(), bp.getY(), bp.getZ())) {
            if(clientUpdate != null) {
                clientUpdate.send(bp, (EntityPlayerMP) res.getPlayer());
            }
            return false;
        }

        return true;
    }

    public int getMeta() {
        return meta;
    }
}
