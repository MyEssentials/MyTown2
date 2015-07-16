package mytown.protection.segment;

import myessentials.entities.Volume;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.getter.Getters;

/**
 * Segment that protects against an Item
 */
public class SegmentItem extends Segment {

    private final ItemType type;
    private final boolean onAdjacent;
    private Volume clientUpdateCoords;
    private boolean directionalClientUpdate;

    public SegmentItem(Class<?> theClass, Getters getters, FlagType flag, Object denialValue, String conditionString, ItemType type, boolean onAdjacent, Volume clientUpdateCoords, boolean directionalClientUpdate) {
        super(theClass, getters, flag, denialValue, conditionString);
        this.type = type;
        this.onAdjacent = onAdjacent;
        this.clientUpdateCoords = clientUpdateCoords;
        this.directionalClientUpdate = directionalClientUpdate;
    }

    public boolean hasClientUpdate() {
        return clientUpdateCoords != null;
    }

    public Volume getClientUpdateCoords() {
        return clientUpdateCoords;
    }

    public boolean isDirectionalClientUpdate() {
        return directionalClientUpdate;
    }

    public ItemType getType() {
        return type;
    }

    public boolean isOnAdjacent() {
        return onAdjacent;
    }
}
