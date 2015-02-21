package mytown.new_protection.segment;

import mytown.new_protection.segment.enums.EntityType;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against an Entity
 */
public class SegmentEntity extends Segment {

    public EntityType type;

    public SegmentEntity(Class<?> theClass, Map<String, List<Getter>> extraGettersMap, String conditionString, EntityType type) {
        super(theClass, extraGettersMap, conditionString);
        this.type = type;
    }

    /**
     * Returns the range in which the entity should be checked (only for explosives so far)
     *
     * @param entity
     * @return
     */
    public int getRange(Entity entity) { return (Integer)getInfoFromGetters("range",  Integer.class, entity, null); }

}
