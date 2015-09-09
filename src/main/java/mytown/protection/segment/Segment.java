package mytown.protection.segment;

import myessentials.entities.Volume;
import mytown.MyTown;
import mytown.api.container.FlagsContainer;
import mytown.api.container.GettersContainer;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.FlagType;

/**
 * A part of the protection that protects against a specific thing.
 */
public abstract class Segment {
    protected boolean isDisabled = false;
    protected Class<?> checkClass;
    protected FlagType flag;
    protected Object denialValue;
    protected Condition condition;

    public final GettersContainer getters = new GettersContainer();

    protected boolean hasPermissionAtLocation(Resident res, int dim, int x, int y, int z) {
        if(MyTownUniverse.instance.blocks.contains(dim, x >> 4, z >> 4)) {
            Town town = MyTownUniverse.instance.blocks.get(dim, x >> 4, z >> 4).getTown();
            return hasPermission(res, town, dim, x, y, z);
        } else {
            return hasPermission(res, Wild.instance);
        }
    }

    protected boolean hasPermissionAtLocation(Resident res, int dim, Volume volume) {
        boolean inWild = false;

        for (int townBlockX = volume.getMinX() >> 4; townBlockX <= volume.getMaxX() >> 4; townBlockX++) {
            for (int townBlockZ = volume.getMinZ() >> 4; townBlockZ <= volume.getMaxZ() >> 4; townBlockZ++) {
                TownBlock townBlock = MyTownUniverse.instance.blocks.get(dim, townBlockX, townBlockZ);

                if (townBlock == null) {
                    inWild = true;
                    continue;
                }

                Town town = townBlock.getTown();
                Volume rangeBox = volume.intersect(townBlock.toVolume());
                int totalIntersectArea = 0;

                // Check every plot in the current TownBlock and sum all plot areas
                for (Plot plot : townBlock.plotsContainer) {
                    Volume plotIntersection = volume.intersect(plot.toVolume());
                    if (plotIntersection != null) {
                        if(!hasPermission(res, plot)) {
                            return false;
                        }
                        totalIntersectArea += plotIntersection.getVolumeAmount();
                    }
                }

                // If plot area sum is not equal to range area, check town permission
                if (totalIntersectArea != rangeBox.getVolumeAmount()) {
                    if(!hasPermission(res, town)) {
                        return false;
                    }
                }
            }
        }

        if (inWild) {
            return hasPermission(res, Wild.instance);
        }

        return true;
    }

    public boolean hasPermission(Resident res, Wild wild) {
        if(res == null) {
            return !wild.flagsContainer.getValue(flag).equals(denialValue);
        } else {
            if(!wild.hasPermission(res, flag, denialValue)) {
                res.protectionDenial(flag);
                return false;
            }
        }
        return true;
    }

    public boolean hasPermission(Resident res, Town town, int dim, int x, int y, int z) {
        if(res == null) {
            return !town.flagsContainer.getValue(flag).equals(denialValue);
        } else {
            if(!town.hasPermission(res, flag, denialValue, dim, x, y, z)) {
                res.protectionDenial(flag, town.formatOwners(dim, x, y, z));
                return false;
            }
        }
        return true;
    }

    public boolean hasPermission(Resident res, Town town) {
        if(res == null) {
            return !town.flagsContainer.getValue(flag).equals(denialValue);
        } else {
            if(!town.hasPermission(res, flag, denialValue)) {
                res.protectionDenial(flag, town.formatOwner());
                return false;
            }
        }
        return true;
    }

    public boolean hasPermission(Resident res, Plot plot) {
        if(res == null) {
            return !plot.flagsContainer.getValue(flag).equals(denialValue);
        } else {
            if(!plot.hasPermission(res, flag, denialValue)) {
                res.protectionDenial(flag, plot.ownersContainer.toString());
                return false;
            }
        }
        return true;
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

    public void setFlag(FlagType flag) {
        this.flag = flag;
    }

    public void setDenialValue(Object denialValue) {
        this.denialValue = denialValue;
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

    public Object getDenialValue() {
        return denialValue;
    }

    public int getRange(Object object) {
        return getters.contains("range") ? (Integer) getters.get("range").invoke(Integer.class, object, object) : 0;
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
