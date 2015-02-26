package mytown.new_protection.segment;

import mytown.config.Config;
import mytown.entities.flag.FlagType;
import mytown.new_protection.ProtectionUtils;
import mytown.util.exceptions.GetterException;
import net.minecraft.tileentity.TileEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against a TileEntity
 */
public class SegmentTileEntity extends Segment implements IBlockModifier {

    public Shape shape;

    public SegmentTileEntity(Class<?> theClass, Map<String, List<Getter>> extraGettersMap, FlagType flag, String conditionString, Shape shape) {
        // List 0 = x1, List 1 = y1 etc...
        super(theClass, extraGettersMap, conditionString);
        this.shape = shape;
        this.flag = flag;
    }

    @Override
    public Shape getShape() {
        return this.shape;
    }

    @Override
    public int getX1(TileEntity te) {
        try {
            return (Integer) getInfoFromGetters("X1", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.xCoord - Config.defaultProtectionSize;
        }
    }

    @Override
    public int getZ1(TileEntity te) {
        try {
            return (Integer) getInfoFromGetters("Z1", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.zCoord - Config.defaultProtectionSize;
        }
    }

    @Override
    public int getX2(TileEntity te) {
        try {
            return (Integer) getInfoFromGetters("X2", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.xCoord + Config.defaultProtectionSize;
        }
    }

    @Override
    public int getZ2(TileEntity te) {
        try {
            return (Integer) getInfoFromGetters("Z2", Integer.class, te, null);
        } catch (GetterException ex) {
            return te.zCoord + Config.defaultProtectionSize;
        }
    }
}
