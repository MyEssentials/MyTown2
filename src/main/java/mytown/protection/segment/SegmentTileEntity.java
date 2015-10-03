package mytown.protection.segment;

import myessentials.entities.Volume;
import mytown.config.Config;
import mytown.entities.Resident;
import mytown.protection.ProtectionHandlers;
import mytown.util.exceptions.GetterException;
import net.minecraft.tileentity.TileEntity;

/**
 * Segment that protects against a TileEntity
 */
public class SegmentTileEntity extends Segment {

    protected boolean retainsOwner = false;

    public boolean shouldExist(TileEntity te) {
        if(!shouldCheck(te)) {
            return true;
        }

        Volume teBox = new Volume(getX1(te), getY1(te), getZ1(te), getX2(te), getY2(te), getZ2(te));
        int dim = te.getWorldObj().provider.dimensionId;
        Resident owner;
        if(retainsOwner) {
            owner = ProtectionHandlers.instance.getOwnerForTileEntity(te);
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
            return te.xCoord - Config.instance.defaultProtectionSize.get();
        }
    }

    public int getY1(TileEntity te) {
        try {
            return (Integer) getters.get("yMin").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.yCoord - Config.instance.defaultProtectionSize.get();
        }
    }

    public int getZ1(TileEntity te) {
        try {
            return (Integer) getters.get("zMin").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.zCoord - Config.instance.defaultProtectionSize.get();
        }
    }

    public int getX2(TileEntity te) {
        try {
            return (Integer) getters.get("xMax").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.xCoord + Config.instance.defaultProtectionSize.get();
        }
    }

    public int getY2(TileEntity te) {
        try {
            return (Integer) getters.get("yMax").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.yCoord + Config.instance.defaultProtectionSize.get();
        }
    }

    public int getZ2(TileEntity te) {
        try {
            return (Integer) getters.get("zMax").invoke(Integer.class, te);
        } catch (GetterException ex) {
            return te.zCoord + Config.instance.defaultProtectionSize.get();
        }
    }

    public boolean retainsOwner() {
        return retainsOwner;
    }
}
