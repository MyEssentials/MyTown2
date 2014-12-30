package mytown.entities;

import com.google.common.collect.ImmutableList;
import mytown.MyTown;
import mytown.api.interfaces.IHasFlags;
import mytown.core.Utils;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.plugin.BukkitCompat;
import mytown.proxies.plugin.PEXCompat;
import mytown.util.MyTownUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by AfterWind on 10/2/2014.
 * Wilderness permissions
 */
public class Wild implements IHasFlags {
    private static Wild instance;

    public static Wild getInstance() {
        if (instance == null) {
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
        for (Flag f : flagList) {
            if (f.flagType == type)
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
            if (f.flagType == type)
                return f;
        }
        return null;
    }

    @Override
    public boolean removeFlag(FlagType type) {
        for (Iterator<Flag> it = flagList.iterator(); it.hasNext(); ) {
            if (it.next().flagType == type) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public boolean checkPermission(Resident res, FlagType type) {
        if (getFlag(type).getValue() instanceof Boolean) {
            if (!(Boolean) getFlag(type).getValue()) {
                if (MyTown.instance.isCauldron && BukkitCompat.getInstance().hasPEX())
                    return PEXCompat.getInstance().checkPermission(res, type.getBypassPermission()) || Utils.isOp(res.getPlayer());
                else
                    return Utils.isOp(res.getPlayer());
            }
        }
        // TODO: Implement other types of flags
        return true;
    }

    @Override
    public Object getValue(FlagType type) {
        for (Flag flag : flagList) {
            if (flag.flagType == type)
                return flag.getValue();
        }
        return null;
    }

    @Override
    public Object getValueAtCoords(int dim, int x, int y, int z, FlagType flagType) {
        return getValue(flagType);
    }

}
