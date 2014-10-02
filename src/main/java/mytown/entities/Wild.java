package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.entities.interfaces.IHasFlags;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 10/2/2014.
 * Wilderness permissions
 */
public class Wild implements IHasFlags {
    private static Wild instance;

    public static Wild getInstance() {
        if(instance == null) {
            instance = new Wild();
        }
        return instance;
    }


    private List<Flag> flagList = new ArrayList<Flag>();

    @Override
    public void addFlag(Flag flag) {
        flagList.add(flag);
    }

    @Override
    public boolean hasFlag(FlagType type) {
        for(Flag f : flagList) {
            if(f.flagType == type)
                return true;
        }
        return false;
    }

    @Override
    public List<Flag> getFlags() {
        return flagList;
    }

    @Override
    public Flag getFlag(FlagType type) {
        for(Flag f : flagList) {
            if(f.flagType == type)
                return f;
        }
        return null;
    }

    public boolean checkPermission(Resident res, FlagType type) {
        // TODO: Check for permissions
        if(getFlag(type).getValue() instanceof Boolean) {
            if(!(Boolean)getFlag(type).getValue()) {
                return false;
            }
        }
        // TODO: Implement other types of flags
        return true;
    }
}
