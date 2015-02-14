package mytown.new_protection.segment;

import mytown.entities.flag.FlagType;
import mytown.new_protection.ProtectionUtils;
import mytown.new_protection.segment.enums.ItemType;
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

    public SegmentItem(Class<?> theClass, Map<String, List<Getter>> extraGettersMap, FlagType flag, String conditionString, ItemType type, boolean onAdjacent) {
        super(theClass, extraGettersMap, conditionString);
        this.flag = flag;
        this.type = type;
        this.onAdjacent = onAdjacent;
    }

    public int getRange(ItemStack stack) {
        return (Integer) getInfoFromGetters("range", Integer.class, stack.getItem(), stack);
    }

}
