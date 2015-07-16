package mytown.protection.segment;

import myessentials.entities.Volume;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.getter.Getters;

/**
 * Offers protection for blocks
 */
public class SegmentBlock extends Segment {
    private final int meta;
    private final BlockType type;
    private Volume clientUpdateCoords;

    public SegmentBlock(Class<?> theClass, Getters getters, FlagType flag, Object denialValue, String conditionString, BlockType blockType, int meta, Volume clientUpdateCoords) {
        super(theClass, getters, flag, denialValue, conditionString);
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
