package mytown.protection.segment;

import myessentials.entities.Volume;
import mytown.MyTown;
import mytown.api.container.GettersContainer;
import mytown.config.Config;
import mytown.entities.Resident;
import mytown.entities.flag.FlagType;
import mytown.protection.ProtectionHandler;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;
import net.minecraft.tileentity.TileEntity;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Segment that protects against a TileEntity
 */
public class SegmentTileEntity extends Segment {

    private boolean hasOwner = false;

    public SegmentTileEntity(boolean hasOwner) {
        this.hasOwner = hasOwner;
    }

    public boolean shouldExist(TileEntity te) {
        try {
            if(condition != null && !condition.execute(te, getters)) {
                return true;
            }
        } catch (Exception ex) {
            if(ex instanceof ConditionException || ex instanceof GetterException) {
                MyTown.instance.LOG.error("An error occurred while checking condition for tile entity [DIM:{}; {}, {}, {}] of type {}", te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, te.getClass().getName());
                MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
                disable();
                return true;
            } else {
                throw (RuntimeException) ex;
            }
        }


        Volume teBox = new Volume(getX1(te), getY1(te), getZ1(te), getX2(te), getY2(te), getZ2(te));
        int dim = te.getWorldObj().provider.dimensionId;
        Resident owner;
        if(hasOwner()) {
            owner = ProtectionHandler.instance.getOwnerForTileEntity(te);
        } else {
            owner = getOwner(te);
        }

        if (!hasPermissionAtLocation(owner, dim, teBox)) {
            return false;
        }

        return true;
    }

    public int getX1(TileEntity te) {
        try {
            return (Integer) getters.get("xMin").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.xCoord - Config.defaultProtectionSize;
        }
    }

    public int getY1(TileEntity te) {
        try {
            return (Integer) getters.get("yMin").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.yCoord - Config.defaultProtectionSize;
        }
    }

    public int getZ1(TileEntity te) {
        try {
            return (Integer) getters.get("zMin").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.zCoord - Config.defaultProtectionSize;
        }
    }

    public int getX2(TileEntity te) {
        try {
            return (Integer) getters.get("xMax").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.xCoord + Config.defaultProtectionSize;
        }
    }

    public int getY2(TileEntity te) {
        try {
            return (Integer) getters.get("yMax").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.yCoord + Config.defaultProtectionSize;
        }
    }

    public int getZ2(TileEntity te) {
        try {
            return (Integer) getters.get("zMax").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.zCoord + Config.defaultProtectionSize;
        }
    }



    public boolean hasOwner() {
        return this.hasOwner;
    }
}
