package mytown.protection.segment;

import myessentials.entities.Volume;
import mytown.MyTown;
import mytown.api.container.GettersContainer;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.enums.EntityType;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.UUID;

/**
 * Segment that protects against an Entity
 */
public class SegmentEntity extends Segment {

    private final EntityType type;

    public SegmentEntity(Class<?> clazz, FlagType<Boolean> flagType, Object denialValue, String conditionString, GettersContainer getters, EntityType entityType) {
        this(entityType);
        if(getters != null) {
            this.getters.addAll(getters);
        }
        setCheckClass(clazz);
        setFlag(flagType);
        setConditionString(conditionString);
    }

    public SegmentEntity(EntityType entityType) {
        this.type = entityType;
    }

    public boolean shouldExist(Entity entity) {
        if(type != EntityType.TRACKED) {
            return true;
        }

        try {
            if (condition != null && !condition.execute(entity, getters)) {
                return true;
            }
        } catch (ConditionException ex) {
            MyTown.instance.LOG.error("An error occurred while checking condition for entity [DIM:{}; {}, {}, {}] of type {}", entity.dimension, entity.posX, entity.posY, entity.posZ, entity.getClass().getName());
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
            disable();
            return true;
        }

        Resident owner = getOwner(entity);
        int range = getRange(entity);
        int dim = entity.dimension;
        int x = (int) Math.floor(entity.posX);
        int y = (int) Math.floor(entity.posY);
        int z = (int) Math.floor(entity.posZ);

        if(range == 0) {
            if (!hasPermissionAtLocation(owner, dim, x, y, z)) {
                return false;
            }
        } else {
            Volume rangeBox = new Volume(x-range, y-range, z-range, x+range, y+range, z+range);
            if (!hasPermissionAtLocation(owner, dim, rangeBox)) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldInteract(Entity entity, Resident res) {
        if(type != EntityType.PROTECT) {
            return true;
        }

        try {
            if (condition != null && !condition.execute(entity, getters)) {
                return true;
            }
        } catch (ConditionException ex) {
            MyTown.instance.LOG.error("An error occurred while checking condition for entity interaction by {} at [DIM:{}; {}, {}, {}] of type {}", res.getPlayerName(), entity.dimension, entity.posX, entity.posY, entity.posZ, entity.getClass().getName());
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
            disable();
            return true;
        }

        int dim = entity.dimension;
        int x = (int) Math.floor(entity.posX);
        int y = (int) Math.floor(entity.posY);
        int z = (int) Math.floor(entity.posZ);

        if (!hasPermissionAtLocation(res, dim, x, y, z)) {
            return false;
        }

        return true;
    }

    public boolean shouldAttack(Entity entity, Resident res) {
        if(type != EntityType.PVP) {
            return true;
        }

        try {
            if (condition != null && !condition.execute(entity, getters)) {
                return true;
            }
        } catch (ConditionException ex) {
            MyTown.instance.LOG.error("An error occurred while checking condition for entity [DIM:{}; {}, {}, {}] of type {}", entity.dimension, entity.posX, entity.posY, entity.posZ, entity.getClass().getName());
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
            disable();
            return true;
        }

        Resident owner = getOwner(entity);
        EntityPlayer attackedPlayer = res.getPlayer();
        int dim = attackedPlayer.dimension;
        int x = (int) Math.floor(attackedPlayer.posX);
        int y = (int) Math.floor(attackedPlayer.posY);
        int z = (int) Math.floor(attackedPlayer.posZ);

        if (!hasPermissionAtLocation(owner, dim, x, y, z)) {
            return false;
        }

        return true;
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


    public EntityType getType() {
        return type;
    }
}
