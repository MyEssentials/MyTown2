package mytown.protection.segment;

import myessentials.entities.BlockPos;
import myessentials.entities.Volume;
import mytown.MyTown;
import mytown.entities.Resident;
import mytown.protection.segment.enums.ItemType;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Segment that protects against an Item
 */
public class SegmentItem extends Segment {

    protected final List<ItemType> types = new ArrayList<ItemType>();
    protected boolean isAdjacent = false;
    protected ClientBlockUpdate clientUpdate;
    protected boolean directionalClientUpdate = false;

    public boolean shouldInteract(ItemStack item, Resident res, PlayerInteractEvent.Action action, BlockPos bp, int face) {
        if(action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && (!types.contains(ItemType.RIGHT_CLICK_AIR) && !types.contains(ItemType.RIGHT_CLICK_ENTITY))
                || action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && !types.contains(ItemType.RIGHT_CLICK_BLOCK)
                || action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK && !types.contains(ItemType.LEFT_CLICK_BLOCK)) {
            return true;
        }

        if(!shouldCheck(item)) {
            return true;
        }

        EntityPlayer player = res.getPlayer();
        int range = getRange(item);
        int dim = bp.getDim();
        int x = bp.getX();
        int y = bp.getY();
        int z = bp.getZ();

        if(range == 0) {
            if (!hasPermissionAtLocation(res, dim, x, y, z)) {
                if(clientUpdate != null) {
                    ForgeDirection direction = ForgeDirection.getOrientation(face);
                    clientUpdate.send(bp, (EntityPlayerMP)player, directionalClientUpdate ? direction : ForgeDirection.UNKNOWN);
                }
                return false;
            }
        } else {
            Volume rangeBox = new Volume(x-range, y-range, z-range, x+range, y+range, z+range);
            if (!hasPermissionAtLocation(res, dim, rangeBox)) {
                if(clientUpdate != null) {
                    ForgeDirection direction = ForgeDirection.getOrientation(face);
                    clientUpdate.send(bp, (EntityPlayerMP)player, directionalClientUpdate ? direction : ForgeDirection.UNKNOWN);
                }
                return false;
            }
        }

        return true;
    }

    public boolean shouldBreakBlock(ItemStack item, Resident res, BlockPos bp) {
        if(!types.contains(ItemType.BREAK_BLOCK)) {
            return true;
        }

        if(!shouldCheck(item)) {
            return true;
        }

        EntityPlayerMP player = (EntityPlayerMP) res.getPlayer();
        int range = getRange(item);
        int dim = bp.getDim();
        int x = bp.getX();
        int y = bp.getY();
        int z = bp.getZ();

        if(range == 0) {
            if (!hasPermissionAtLocation(res, dim, x, y, z)) {
                if(clientUpdate != null) {
                    clientUpdate.send(bp, player);
                }
                return false;
            }
        } else {
            Volume rangeBox = new Volume(x-range, y-range, z-range, x+range, y+range, z+range);
            if (!hasPermissionAtLocation(res, dim, rangeBox)) {
                if(clientUpdate != null) {
                    clientUpdate.send(bp, player);
                }
                return false;
            }
        }
        return true;
    }
}
