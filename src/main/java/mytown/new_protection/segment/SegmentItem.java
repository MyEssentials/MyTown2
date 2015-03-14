package mytown.new_protection.segment;

import mytown.entities.flag.FlagType;
import mytown.new_protection.segment.enums.ItemType;
import mytown.new_protection.segment.getter.Caller;
import mytown.new_protection.segment.getter.Getters;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against an Item
 */
public class SegmentItem extends Segment {

    public ItemType type;
    public boolean onAdjacent = false;

    public SegmentItem(Class<?> theClass, Getters getters, FlagType flag, String conditionString, ItemType type, boolean onAdjacent) {
        super(theClass, getters, conditionString);
        this.flag = flag;
        this.type = type;
        this.onAdjacent = onAdjacent;
    }

    /**
     * Returns the range in which the item, when an action is performed, can affect the town.
     */
    public int getRange(ItemStack stack) {
        return (Integer) getters.getValue("range", Integer.class, stack.getItem(), stack);
    }

}
