package mytown.new_protection.segment;

import mytown.config.Config;
import mytown.entities.flag.FlagType;
import mytown.new_protection.segment.getter.Getters;
import mytown.util.exceptions.GetterException;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against a TileEntity
 */
public class SegmentTileEntity extends Segment {

    public SegmentTileEntity(Class<?> theClass, Getters getters, FlagType flag, String conditionString) {
        // List 0 = x1, List 1 = y1 etc...
        super(theClass, getters, conditionString);
        this.flag = flag;
    }

    public int getX1(TileEntity te) {
        try {
            return (Integer) getters.getValue("X1", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.xCoord - Config.defaultProtectionSize;
        }
    }

    public int getZ1(TileEntity te) {
        try {
            return (Integer) getters.getValue("Z1", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.zCoord - Config.defaultProtectionSize;
        }
    }

    public int getX2(TileEntity te) {
        try {
            return (Integer) getters.getValue("X2", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.xCoord + Config.defaultProtectionSize;
        }
    }

    public int getZ2(TileEntity te) {
        try {
            return (Integer) getters.getValue("Z2", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.zCoord + Config.defaultProtectionSize;
        }
    }
}
