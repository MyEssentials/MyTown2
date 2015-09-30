package mytown.config.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import myessentials.json.JsonConfig;
import mytown.MyTown;
import mytown.api.container.FlagsContainer;
import mytown.entities.Wild;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

import java.util.Iterator;
import java.util.List;

/**
 * Wilderness flags
 */
public class WildPermsConfig extends JsonConfig<Flag, FlagsContainer> {

    public WildPermsConfig(String path) {
        super(path, "WildPermsConfig");
        gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(FlagsContainer.class, new FlagsContainer.Serializer()).create();
        gsonType = new TypeToken<FlagsContainer>() {}.getType();
    }

    @Override
    protected FlagsContainer newList() {
        return new FlagsContainer();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void create(FlagsContainer items) {
        for (FlagType type : FlagType.values()) {
            if (type.isWildPerm) {
                items.add(new Flag(type, type.defaultWildValue));
            }
        }
        super.create(items);
    }

    @Override
    public FlagsContainer read() {
        FlagsContainer items = super.read();

        Wild.instance.flagsContainer.clear();
        Wild.instance.flagsContainer.addAll(items);

        return items;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean validate(FlagsContainer items) {
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
