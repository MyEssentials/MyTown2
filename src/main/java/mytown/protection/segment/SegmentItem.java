package mytown.protection.segment;

import myessentials.entities.Volume;
import mytown.api.container.GettersContainer;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.ItemType;

/**
 * Segment that protects against an Item
 */
public class SegmentItem extends Segment {

    private final ItemType type;
    private final boolean onAdjacent;
    private Volume clientUpdateCoords;
    private boolean directionalClientUpdate;

    public SegmentItem(Class<?> clazz, FlagType flagType, Object denialValue, String conditionString, GettersContainer getters, ItemType type, boolean onAdjacent, Volume clientUpdateCoords, boolean directionalClientUpdate) {
        this(type, onAdjacent, clientUpdateCoords, directionalClientUpdate);
        if(getters != null) {
            this.getters.addAll(getters);
        }
        setCheckClass(clazz);
        setFlag(flagType);
        setDenialValue(denialValue);
        setConditionString(conditionString);
    }

    public SegmentItem(ItemType type, boolean onAdjacent, Volume clientUpdateCoords, boolean directionalClientUpdate) {
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
