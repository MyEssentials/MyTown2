package mytown.config.json;

import com.google.common.reflect.TypeToken;
import myessentials.json.JSONConfig;
import mytown.MyTown;
import mytown.entities.Wild;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

import java.util.Iterator;
import java.util.List;

/**
 * Wilderness flags
 */
public class WildPermsConfig extends JSONConfig<Flag> {

    public WildPermsConfig(String path) {
        super(path, "WildPermsConfig");
        gsonType = new TypeToken<List<Flag>>() {}.getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void create(List<Flag> items) {
        for (FlagType type : FlagType.values()) {
            if (type.isWildPerm())
                items.add(new Flag(type, type.getDefaultWildPerm()));
        }
        super.create(items);
    }

    @Override
    public List<Flag> read() {
        List<Flag> items = super.read();

        Wild.instance.flagsContainer.clear();
        for(Flag item : items) {
            Wild.instance.flagsContainer.add(item);
        }

        return items;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean validate(List<Flag> items) {
        boolean isValid = true;

        for(Iterator<Flag> it = items.iterator(); it.hasNext();) {
            Flag item = it.next();
            if(item.getFlagType() == null) {
                MyTown.instance.LOG.error("An unrecognized flagType has been found. Removing...");
                it.remove();
                isValid = false;
                continue;
            }
            if (!item.getFlagType().isWildPerm()) {
                MyTown.instance.LOG.error("A non wild flagType has been found in WildPerms config file. Removing...");
                it.remove();
                isValid = false;
            }
        }

        for (FlagType type : FlagType.values()) {
            if (type.isWildPerm()) {
                boolean ok = false;
                for (Flag f : items) {
                    if (f.getFlagType() == type)
                        ok = true;
                }
                if (!ok) {
                    MyTown.instance.LOG.error("FlagType {} for Wild does not exist in the WildPerms file. Adding...", type.toString());
                    items.add(new Flag(type, type.getDefaultValue()));
                    isValid = false;
                }
            }
        }
        return isValid;
    }
}
