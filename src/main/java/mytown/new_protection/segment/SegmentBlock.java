package mytown.new_protection.segment;

import mytown.entities.flag.FlagType;
import mytown.new_protection.segment.getter.Getters;

/**
 * Created by AfterWind on 1/11/2015.
 * Offers protection for blocks
 */
public class SegmentBlock extends Segment {
    public int meta;
    public SegmentBlock(Class<?> theClass, Getters getters, FlagType flag, String conditionString, int meta) {
        super(theClass, getters, conditionString);
        this.meta = meta;
        this.flag = flag;
    }
}
