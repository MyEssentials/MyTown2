package mytown.new_protection.segment;

import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against an Item
 */
public class SegmentItem extends Segment {
    public SegmentItem(Class<?> theClass, Map<String, List<Getter>> extraGettersMap) {
        super(theClass, extraGettersMap);
    }
}
