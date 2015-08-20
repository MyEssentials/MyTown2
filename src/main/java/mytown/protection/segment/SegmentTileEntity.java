package mytown.protection.segment;

import mytown.config.Config;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.getter.Getters;
import mytown.util.exceptions.GetterException;
import net.minecraft.tileentity.TileEntity;

/**
 * Segment that protects against a TileEntity
 */
public class SegmentTileEntity extends Segment {

    private boolean hasOwner = false;

    public SegmentTileEntity(Class<?> theClass, Getters getters, FlagType flag, Object denialValue, String conditionString, boolean hasOwner) {
        super(theClass, getters, flag, denialValue, conditionString);
        this.hasOwner = hasOwner;
    }

    public int getX1(TileEntity te) {
        try {
            return (Integer) getters.getValue("xMin", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.xCoord - Config.defaultProtectionSize;
        }
    }

    public int getY1(TileEntity te) {
        try {
            return (Integer) getters.getValue("yMin", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.yCoord - Config.defaultProtectionSize;
        }
    }

    public int getZ1(TileEntity te) {
        try {
            return (Integer) getters.getValue("zMin", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.zCoord - Config.defaultProtectionSize;
        }
    }

    public int getX2(TileEntity te) {
        try {
            return (Integer) getters.getValue("xMax", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.xCoord + Config.defaultProtectionSize;
        }
    }

    public int getY2(TileEntity te) {
        try {
            return (Integer) getters.getValue("yMax", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.yCoord + Config.defaultProtectionSize;
        }
    }

    public int getZ2(TileEntity te) {
        try {
            return (Integer) getters.getValue("zMax", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.zCoord + Config.defaultProtectionSize;
        }
    }

    public boolean hasOwner() {
        return this.hasOwner;
    }
}
