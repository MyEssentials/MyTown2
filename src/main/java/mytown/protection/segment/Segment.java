package mytown.protection.segment;

import com.google.gson.internal.LazilyParsedNumber;
import myessentials.entities.Volume;
import mytown.MyTown;
import mytown.api.container.GettersContainer;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.protection.ProtectionUtils;
import mytown.util.exceptions.GetterException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A part of the protection that protects against a specific thing.
 */
public abstract class Segment {
    protected boolean isDisabled = false;
    protected Class<?> checkClass;
    protected FlagType<Boolean> flag;
    protected Condition condition;

    public final GettersContainer getters = new GettersContainer();

    protected boolean hasPermissionAtLocation(Resident res, int dim, int x, int y, int z) {
        return ProtectionUtils.hasPermission(res, flag, dim, x, y, z);
    }

    protected boolean hasPermissionAtLocation(Resident res, int dim, Volume volume) {
        return ProtectionUtils.hasPermission(res, flag, dim, volume);
    }


    public Resident getOwner(Object object) {
        try {
            EntityPlayer player = getters.contains("owner") ? (EntityPlayer) getters.get("owner").invoke(EntityPlayer.class, object, object) : null;
            if(player == null)
                return null;
            return MyTownUniverse.instance.getOrMakeResident(player);
        } catch (GetterException ex) {
            try {
                String username = getters.contains("owner") ? (String) getters.get("owner").invoke(String.class, object, object) : null;
                if (username == null)
                    return null;
                return MyTownUniverse.instance.getOrMakeResident(username);
            } catch (GetterException ex2) {
                try {
                    UUID uuid = getters.contains("owner") ? (UUID) getters.get("owner").invoke(UUID.class, object, object) : null;
                    if (uuid == null)
                        return null;
                    return MyTownUniverse.instance.getOrMakeResident(uuid);
                } catch (GetterException ex3) {
                    return null;
                }
            }
        }
    }

    public boolean shouldCheck(Class<?> clazz) {
        return checkClass.isAssignableFrom(clazz);
    }

    public void setCheckClass(Class<?> checkClass) {
        this.checkClass = checkClass;
    }

    public void setConditionString(String conditionString) {
        if(conditionString != null) {
            this.condition = new Condition(conditionString);
        }
    }

    public void setFlag(FlagType<Boolean> flag) {
        this.flag = flag;
    }

    public Class<?> getCheckClass() {
        return checkClass;
    }

    public Condition getCondition() {
        return condition;
    }

    public FlagType getFlag() {
        return flag;
    }

    public int getRange(Object object) {
        return getters.contains("range") ? ((LazilyParsedNumber) getters.get("range").invoke(LazilyParsedNumber.class, object, object)).intValue() : 0;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void disable() {
        MyTown.instance.LOG.error("Disabling segment for {}", getCheckClass().getName());
        MyTown.instance.LOG.info("Reload protections to enable it again.");
        this.isDisabled = true;
    }

    public void enable() {
        this.isDisabled = false;
    }
}
