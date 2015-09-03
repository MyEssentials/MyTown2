package mytown.protection.segment;

import mytown.datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.getter.Getters;
import mytown.util.exceptions.GetterException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

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
            EntityPlayer player = getters.hasValue("owner") ? (EntityPlayer) getters.getValue("owner", EntityPlayer.class, entity, entity) : null;
            if(player == null)
                return null;
            return MyTownUniverse.instance.getOrMakeResident(player);
        } catch (GetterException ex) {
            try {
                String username = getters.hasValue("owner") ? (String) getters.getValue("owner", String.class, entity, entity) : null;
                if (username == null)
                    return null;
                return MyTownUniverse.instance.getOrMakeResident(username);
            } catch (GetterException ex2) {
                try {
                    UUID uuid = getters.hasValue("owner") ? (UUID) getters.getValue("owner", UUID.class, entity, entity) : null;
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
