package mytown.protection.segment;

import myessentials.entities.api.Volume;
import mytown.config.Config;
import mytown.entities.Resident;
import mytown.protection.ProtectionHandlers;
import mytown.protection.segment.getter.Getter;
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
            Getter xMinGetter = getters.get("xMin");
            if (xMinGetter != null) {
                return (Integer) (xMinGetter.invoke(Integer.class, te));
            }
        } catch (GetterException ex) {}
        return te.xCoord - Config.instance.defaultProtectionSize.get();
    }

    public int getY1(TileEntity te) {
        try {
            Getter yMinGetter = getters.get("yMin");
            if (yMinGetter != null) {
                return (Integer) (yMinGetter.invoke(Integer.class, te));
            }
        } catch (GetterException ex) {}
        return te.yCoord - Config.instance.defaultProtectionSize.get();
    }

    public int getZ1(TileEntity te) {
        try {
            Getter zMinGetter = getters.get("zMin");
            if (zMinGetter != null) {
                return (Integer) (zMinGetter.invoke(Integer.class, te));
            }
        } catch (GetterException ex) {}
        return te.zCoord - Config.instance.defaultProtectionSize.get();
    }

    public int getX2(TileEntity te) {
        try {
            Getter xMaxGetter = getters.get("xMax");
            if (xMaxGetter != null) {
                return (Integer) (xMaxGetter.invoke(Integer.class, te));
            }
        } catch (GetterException ex) {}
        return te.xCoord + Config.instance.defaultProtectionSize.get();
    }

    public int getY2(TileEntity te) {
        try {
            Getter yMaxGetter = getters.get("yMax");
            if (yMaxGetter != null) {
                return (Integer) (yMaxGetter.invoke(Integer.class, te));
            }
        } catch (GetterException ex) {}
        return te.yCoord + Config.instance.defaultProtectionSize.get();
    }

    public int getZ2(TileEntity te) {
        try {
            Getter zMaxGetter = getters.get("zMax");
            if (zMaxGetter != null) {
                return (Integer) (zMaxGetter.invoke(Integer.class, te));
            }
        } catch (GetterException ex) {}
        return te.xCoord + Config.instance.defaultProtectionSize.get();
    }

    public boolean retainsOwner() {
        return retainsOwner;
    }
}
