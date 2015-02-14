package mytown.new_protection.segment;

import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/11/2015.
 * Offers protection for blocks
 */
public class SegmentBlock extends Segment {
    public int meta;
    public SegmentBlock(Class<?> theClass, Map<String, List<Getter>> extraGettersMap, String conditionString, int meta) {
        super(theClass, extraGettersMap, conditionString);
        this.meta = meta;
    }
}
