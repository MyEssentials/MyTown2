package mytown.protection.segment;

import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.getter.Getters;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against an Entity
 */
public class SegmentEntity extends Segment {

    public EntityType type;

    public SegmentEntity(Class<?> theClass, Getters getters, FlagType flag, Object denialValue, String conditionString, EntityType type) {
        super(theClass, getters, flag, denialValue, conditionString);
        this.type = type;
    }
}
