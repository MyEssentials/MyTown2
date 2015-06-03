package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.api.interfaces.FlagsContainer;
import mytown.core.utils.PlayerUtils;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Wilderness permissions
 */
public class Wild implements FlagsContainer {

    public static final Wild instance = new Wild();

    private final List<Flag> flagList = new ArrayList<Flag>();

    @Override
    public void addFlag(Flag flag) {
        flagList.add(flag);
    }

    @Override
    public boolean hasFlag(FlagType type) {
        for (Flag f : flagList) {
            if (f.getFlagType() == type)
                return true;
        }
        return false;
    }

    @Override
    public ImmutableList<Flag> getFlags() {
        return ImmutableList.copyOf(flagList);
    }

    @Override
    public Flag getFlag(FlagType type) {
        for (Flag f : flagList) {
            if (f.getFlagType() == type)
                return f;
        }
        return null;
    }

    @Override
    public boolean removeFlag(FlagType type) {
        for (Iterator<Flag> it = flagList.iterator(); it.hasNext(); ) {
            if (it.next().getFlagType() == type) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if Resident is allowed to do the action specified by the FlagType in the Wild
     */
    public boolean checkPermission(Resident res, FlagType type, Object denialValue) {
        if (getFlag(type).getValue() == denialValue) {
            return PlayerUtils.isOp(res.getPlayer());
        }
        return true;
    }

    @Override
    public Object getValue(FlagType type) {
        for (Flag flag : flagList) {
            if (flag.getFlagType() == type)
                return flag.getValue();
        }
        return null;
    }

    @Override
    public Object getValueAtCoords(int dim, int x, int y, int z, FlagType flagType) {
        return getValue(flagType);
    }

}
