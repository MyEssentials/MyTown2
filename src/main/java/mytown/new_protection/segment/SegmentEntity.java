package mytown.new_protection.segment;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against an Entity
 */
public class SegmentEntity extends Segment {

    public EntityType type;

    public SegmentEntity(Class<?> theClass, EntityType type) {
        super(theClass);
        this.type = type;
    }
}
