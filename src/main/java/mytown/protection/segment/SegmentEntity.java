package mytown.protection.segment;

import mytown.api.container.GettersContainer;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.EntityType;
import mytown.util.exceptions.GetterException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

/**
 * Segment that protects against an Entity
 */
public class SegmentEntity extends Segment {

    private final EntityType type;

    public SegmentEntity(Class<?> clazz, FlagType flagType, Object denialValue, String conditionString, GettersContainer getters, EntityType entityType) {
        this(entityType);
        if(getters != null) {
            this.getters.addAll(getters);
        }
        setCheckClass(clazz);
        setFlag(flagType);
        setDenialValue(denialValue);
        setConditionString(conditionString);
    }

    public SegmentEntity(EntityType entityType) {
        this.type = entityType;
    }

    public EntityType getType() {
        return type;
    }

    public boolean hasOwner() {
        return getters.contains("owner");
    }


    public Resident getOwner(Entity entity) {
        try {
            EntityPlayer player = getters.contains("owner") ? (EntityPlayer) getters.get("owner").invoke(EntityPlayer.class, entity, entity) : null;
            if(player == null)
                return null;
            return MyTownUniverse.instance.getOrMakeResident(player);
        } catch (GetterException ex) {
            try {
                String username = getters.contains("owner") ? (String) getters.get("owner").invoke(String.class, entity, entity) : null;
                if (username == null)
                    return null;
                return MyTownUniverse.instance.getOrMakeResident(username);
            } catch (GetterException ex2) {
                try {
                    UUID uuid = getters.contains("owner") ? (UUID) getters.get("owner").invoke(UUID.class, entity, entity) : null;
                    if (uuid == null)
                        return null;
                    return MyTownUniverse.instance.getOrMakeResident(uuid);
                } catch (GetterException ex3) {
                    return null;
                }
            }
        }
    }
}
