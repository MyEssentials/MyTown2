package mytown.protection.segment;

/**
 * Offers special protection for blocks
 */
public class SegmentSpecialBlock extends Segment {
    protected boolean isAlwaysBreakable = false;
    protected int meta = -1;

    public boolean isAlwaysBreakable()
    {
        return isAlwaysBreakable;
    }

    public int getMeta() {
        return meta;
    }
}
