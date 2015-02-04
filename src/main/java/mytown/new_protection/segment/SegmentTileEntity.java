package mytown.new_protection.segment;

import mytown.entities.flag.FlagType;
import mytown.new_protection.ProtectionUtils;
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
        return (Integer) ProtectionUtils.getInfoFromGetters(extraGettersMap.get("X1"), te, Integer.class, this.theClass.getName(), null);
    }

    @Override
    public int getZ1(TileEntity te) {
        return (Integer)ProtectionUtils.getInfoFromGetters(extraGettersMap.get("Z1"), te, Integer.class, this.theClass.getName(), null);
    }

    @Override
    public int getX2(TileEntity te) {
        return (Integer)ProtectionUtils.getInfoFromGetters(extraGettersMap.get("X2"), te, Integer.class, this.theClass.getName(), null);
    }

    @Override
    public int getZ2(TileEntity te) {
        return (Integer) ProtectionUtils.getInfoFromGetters(extraGettersMap.get("Z2"), te, Integer.class, this.theClass.getName(), null);
    }
}
