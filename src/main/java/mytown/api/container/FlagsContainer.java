package mytown.api.container;

import myessentials.entities.Container;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

import java.util.Iterator;

public class FlagsContainer extends Container<Flag> {
    public boolean contains(FlagType type) {
        for (Flag flag : items) {
            if (flag.getFlagType() == type) {
                return true;
            }
        }
        return false;
    }

    public Flag get(FlagType type) {
        for (Flag flag : items)
            if (flag.getFlagType() == type)
                return flag;
        return null;
    }

    public void remove(FlagType type) {
        for (Iterator<Flag> it = items.iterator(); it.hasNext(); ) {
            if (it.next().getFlagType() == type) {
                it.remove();
            }
        }
    }

    public Object getValue(FlagType type) {
        for (Flag flag : items) {
            if (flag.getFlagType() == type)
                return flag.getValue();
        }
        return type.getDefaultValue();
    }
}
