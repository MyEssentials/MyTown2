package mytown.config.json;

import myessentials.json.api.JsonConfig;
import mytown.MyTown;
import mytown.entities.Wild;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

import java.util.Iterator;

/**
 * Wilderness flags
 */
public class WildPermsConfig extends JsonConfig<Flag, Flag.Container> {

    public WildPermsConfig(String path) {
        super(path, "WildPermsConfig");
        this.gsonType = Flag.Container.class;
        this.gson = new Flag.Serializer().createBuilder().setPrettyPrinting().create();
    }

    @Override
    protected Flag.Container newList() {
        return new Flag.Container();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void create(Flag.Container items) {
        for (FlagType type : FlagType.values()) {
            if (type.isWildPerm) {
                items.add(new Flag(type, type.defaultWildValue));
            }
        }
        Wild.instance.flagsContainer.addAll(items);
        super.create(items);
    }

    @Override
    public Flag.Container read() {
        Flag.Container items = super.read();

        Wild.instance.flagsContainer.clear();
        Wild.instance.flagsContainer.addAll(items);

        return items;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean validate(Flag.Container items) {
        boolean isValid = true;

        for (Iterator<Flag> it = items.iterator(); it.hasNext();) {
            Flag item = it.next();
            if (item.flagType == null) {
                MyTown.instance.LOG.error("An unrecognized flagType has been found. Removing...");
                it.remove();
                isValid = false;
                continue;
            }
            if (!item.flagType.isWildPerm) {
                MyTown.instance.LOG.error("A non wild flagType has been found in WildPerms config file. Removing...");
                it.remove();
                isValid = false;
            }
        }

        for (FlagType type : FlagType.values()) {
            if (type.isWildPerm) {
                boolean ok = false;
                for (Flag f : items) {
                    if (f.flagType == type) {
                        ok = true;
                    }
                }
                if (!ok) {
                    MyTown.instance.LOG.error("FlagType {} for Wild does not exist in the WildPerms file. Adding...", type.name);
                    items.add(new Flag(type, type.defaultValue));
                    isValid = false;
                }
            }
        }
        return isValid;
    }
}
