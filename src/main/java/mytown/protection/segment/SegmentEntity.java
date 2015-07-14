package mytown.protection.segment;

import mytown.entities.Resident;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.getter.Getters;
import mytown.proxies.DatasourceProxy;
import mytown.util.exceptions.GetterException;
import net.minecraft.entity.Entity;

/**
 * Segment that protects against an Entity
 */
public class SegmentEntity extends Segment {

    private final EntityType type;

    public SegmentEntity(Class<?> theClass, Getters getters, FlagType flag, Object denialValue, String conditionString, EntityType type) {
        super(theClass, getters, flag, denialValue, conditionString);
        this.type = type;
    }

    public EntityType getType() {
        return type;
    }

    public boolean hasOwner() {
        return getters.hasValue("owner");
    }


    public Resident getOwner(Entity entity) {
        try {
            String name = getters.hasValue("owner") ? (String) getters.getValue("owner", String.class, entity, entity) : null;
            if(name == null)
                return null;
            return DatasourceProxy.getDatasource().getOrMakeResident(name);
        } catch (GetterException ex) {
            /*
            String uuid = getters.hasValue("owner") ? (String) getters.getValue("owner", String.class, entity, entity) : null;
            if(uuid == null)
                return null;
            return DatasourceProxy.getDatasource().getOrMakeResident();
            */
            return null;
        }
    }
}
