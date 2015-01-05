package mytown.new_protection.segment;

import net.minecraft.tileentity.TileEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against a TileEntity
 */
public class SegmentTileEntity extends Segment implements IBlockModifier {

    public List<Getter>[] coordGetters;
    public Shape shape;

    public SegmentTileEntity(Class<?> theClass, Map<String, List<Getter>> extraGettersMap, List<Getter>[] coordGetters, Shape shape) {
        // List 0 = x1, List 1 = y1 etc...
        super(theClass, extraGettersMap);
        this.coordGetters = coordGetters;
        this.shape = shape;
    }

    @Override
    public Shape getShape() {
        return this.shape;
    }

    @Override
    public int getX1(TileEntity te) {
        return (Integer)getInfoFromGetters(coordGetters[0], te, Integer.class);
    }

    @Override
    public int getZ1(TileEntity te) {
        return (Integer)getInfoFromGetters(coordGetters[1], te, Integer.class);
    }

    @Override
    public int getX2(TileEntity te) {
        return (Integer)getInfoFromGetters(coordGetters[2], te, Integer.class);
    }

    @Override
    public int getZ2(TileEntity te) {
        return (Integer)getInfoFromGetters(coordGetters[3], te, Integer.class);
    }
}
