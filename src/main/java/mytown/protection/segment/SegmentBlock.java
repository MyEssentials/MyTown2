package mytown.protection.segment;

import myessentials.entities.Volume;
import mytown.api.container.GettersContainer;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.BlockType;

/**
 * Offers protection for blocks
 */
public class SegmentBlock extends Segment {
    private final int meta;
    private final BlockType type;
    private Volume clientUpdateCoords;

    public SegmentBlock(Class<?> clazz, FlagType flagType, Object denialValue, String conditionString, GettersContainer getters, BlockType blockType, int meta, Volume clientUpdateCoords) {
        this(blockType, meta, clientUpdateCoords);
        if(getters != null) {
            this.getters.addAll(getters);
        }
        setCheckClass(clazz);
        setFlag(flagType);
        setDenialValue(denialValue);
        setConditionString(conditionString);
    }

    public SegmentBlock(BlockType blockType, int meta, Volume clientUpdateCoords) {
        this.meta = meta;
        this.type = blockType;
        this.clientUpdateCoords = clientUpdateCoords;
    }

    public boolean hasClientUpdate() {
        return clientUpdateCoords != null;
    }

    public Volume getClientUpdateCoords() {
        return clientUpdateCoords;
    }

    public int getMeta() {
        return meta;
    }

    public BlockType getType() {
        return type;
    }
}
