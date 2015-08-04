package mytown.api.container;

import com.google.common.collect.ImmutableList;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class FlagsContainer {

    private ArrayList<Flag> flags = new ArrayList<Flag>();

    public void addFlag(Flag flag) {
        flags.add(flag);
        Collections.sort(flags);
    }

    public boolean hasFlag(FlagType type) {
        for (Flag flag : flags) {
            if (flag.getFlagType() == type) {
                return true;
            }
        }
        return false;
    }

    public Flag getFlag(FlagType type) {
        for (Flag flag : flags)
            if (flag.getFlagType() == type)
                return flag;
        return null;
    }

    public void removeFlag(FlagType type) {
        for (Iterator<Flag> it = flags.iterator(); it.hasNext(); ) {
            if (it.next().getFlagType() == type) {
                it.remove();
            }
        }
    }

    public Object getValue(FlagType type) {
        for (Flag flag : flags) {
            if (flag.getFlagType() == type)
                return flag.getValue();
        }
        return type.getDefaultValue();
    }

    public ImmutableList<Flag> getFlags() {
        return ImmutableList.copyOf(flags);
    }
}
