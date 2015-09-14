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

/**
 * Offers protection for blocks
 */
public class SegmentBlock extends Segment {
    private final int meta;
    private final BlockType type;

    public final ClientBlockUpdate clientUpdate;

    public SegmentBlock(Class<?> clazz, FlagType<Boolean> flagType, Object denialValue, String conditionString, GettersContainer getters, BlockType blockType, int meta, Volume clientUpdateCoords) {
        this(blockType, meta, clientUpdateCoords);
        if(getters != null) {
            this.getters.addAll(getters);
        }
        setCheckClass(clazz);
        setFlag(flagType);
        setConditionString(conditionString);
    }

    public SegmentBlock(BlockType blockType, int meta, Volume clientUpdateCoords) {
        this.meta = meta;
        this.type = blockType;
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

        if(type == BlockType.LEFT_CLICK && action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
                || type == BlockType.RIGHT_CLICK && action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
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

    public BlockType getType() {
        return type;
    }
}
