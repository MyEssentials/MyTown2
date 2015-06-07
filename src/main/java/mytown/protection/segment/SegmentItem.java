package mytown.protection.segment;

import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.getter.Getters;

/**
 * Segment that protects against an Item
 */
public class SegmentItem extends Segment {

    private final ItemType type;
    private final boolean onAdjacent;

    public SegmentItem(Class<?> theClass, Getters getters, FlagType flag, Object denialValue, String conditionString, ItemType type, boolean onAdjacent) {
        super(theClass, getters, flag, denialValue, conditionString);
        this.type = type;
        this.onAdjacent = onAdjacent;
    }

    public ItemType getType() {
        return type;
    }

    public boolean isOnAdjacent() {
        return onAdjacent;
    }
}
