package mytown.config.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import myessentials.MyEssentialsCore;
import myessentials.json.api.JsonConfig;
import mytown.MyTown;
import mytown.entities.flag.FlagType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Default flags on town creation config file.
 */
public class FlagsConfig extends JsonConfig<FlagsConfig.Wrapper, List<FlagsConfig.Wrapper>> {
    public FlagsConfig(String path) {
        super(path, "FlagsConfig");
        gsonType = new TypeToken<List<Wrapper>>() {}.getType();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    protected List<Wrapper> newList() {
        return new ArrayList<Wrapper>();
    }

    @Override
    public void create(List<Wrapper> items) {
        for (FlagType type : FlagType.values()) {
            items.add(new Wrapper(type.name, type.defaultValue, type.configurable));
        }

        for (Wrapper item : items) {
            item.getFlagType().defaultValue = item.defaultState;
            item.getFlagType().configurable = item.configurable;
        }

        super.create(items);
    }

    @Override
    public List<Wrapper> read() {
        List<Wrapper> items = super.read();

        for (Wrapper item : items) {
            item.getFlagType().defaultValue = item.defaultState;
            item.getFlagType().configurable = item.configurable;
        }

        return items;
    }

    @Override
    public boolean validate(List<Wrapper> items) {
        boolean ok;
        boolean isValid = true;

        for (Iterator<Wrapper> it = items.iterator(); it.hasNext(); ) {
            Wrapper item = it.next();
            if (item.flagName == null) {
                MyEssentialsCore.instance.LOG.error("Found a type of flag that does not exist. Removing...");
                it.remove();
                isValid = false;
                continue;
            }
            if (!item.getFlagType().type.isAssignableFrom(item.defaultState.getClass())) {
                MyTown.instance.LOG.error("The default value for the flag is of invalid type for flag " + item.flagName.toString() + "! Needed " + item.getFlagType().type.getSimpleName() + " Removing...");
                it.remove();
                isValid = false;
            }
        }

        for (FlagType type : FlagType.values()) {
            ok = false;
            for (Wrapper w : items) {
                if (w.getFlagType() == type) {
                    ok = true;
                }
            }
            if (!ok) {
                items.add(new Wrapper(type.name, type.defaultValue, type.configurable));
                MyTown.instance.LOG.error("Flag config is missing (or is an invalid entry) {} flag! Adding with default settings...", type.name);
                isValid = false;
            }
        }
        return isValid;
    }

    /**
     * Wraps around a flagType object.
     */
    public class Wrapper {
        public final String flagName;
        public final Object defaultState;
        public final boolean configurable;

        public Wrapper(String flagType, Object defaultState, boolean configurable) {
            this.flagName = flagType;
            this.defaultState = defaultState;
            this.configurable = configurable;
        }

        public FlagType getFlagType() {
            return FlagType.valueOf(flagName);
        }

    }
}
