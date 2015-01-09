package mytown.new_protection.segment;

import mytown.new_protection.segment.enums.ItemType;

import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against an Item
 */
public class SegmentItem extends Segment {

    public ItemType type;

    public SegmentItem(Class<?> theClass, Map<String, List<Getter>> extraGettersMap, String conditionString, ItemType type) {
        super(theClass, extraGettersMap, conditionString);
        this.type = type;
    }
}
