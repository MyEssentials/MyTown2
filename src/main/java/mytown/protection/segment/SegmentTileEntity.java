package mytown.protection.segment;

import mytown.api.container.GettersContainer;
import mytown.config.Config;
import mytown.entities.flag.FlagType;
import mytown.util.exceptions.GetterException;
import net.minecraft.tileentity.TileEntity;

/**
 * Segment that protects against a TileEntity
 */
public class SegmentTileEntity extends Segment {

    private boolean hasOwner = false;

    public SegmentTileEntity(Class<?> clazz, FlagType flagType, Object denialValue, String conditionString, GettersContainer getters, boolean hasOwner) {
        this(hasOwner);
        if(getters != null) {
            this.getters.addAll(getters);
        }
        setCheckClass(clazz);
        setFlag(flagType);
        setDenialValue(denialValue);
        setConditionString(conditionString);
    }

    public SegmentTileEntity(boolean hasOwner) {
        this.hasOwner = hasOwner;
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
