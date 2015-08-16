package mytown.api.container;

import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlagsContainer extends ArrayList<Flag> {

    public boolean contains(FlagType type) {
        for (Flag flag : this) {
            if (flag.getFlagType() == type) {
                return true;
            }
        }
        return false;
    }

    public Flag get(FlagType type) {
        for (Flag flag : this)
            if (flag.getFlagType() == type)
                return flag;
        return null;
    }

    public void remove(FlagType type) {
        for (Iterator<Flag> it = iterator(); it.hasNext(); ) {
            if (it.next().getFlagType() == type) {
                it.remove();
            }
        }
    }

    public Object getValue(FlagType type) {
        for (Flag flag : this) {
            if (flag.getFlagType() == type)
                return flag.getValue();
        }
        return type.getDefaultValue();
    }
}
