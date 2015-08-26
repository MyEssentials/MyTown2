package mytown.config.json;

import com.google.common.reflect.TypeToken;
import myessentials.MyEssentialsCore;
import myessentials.json.JSONConfig;
import mytown.MyTown;
import mytown.entities.flag.FlagType;

import java.util.Iterator;
import java.util.List;

/**
 * Default flags on town creation config file.
 */
public class FlagsConfig extends JSONConfig<FlagsConfig.Wrapper> {
    public FlagsConfig(String path) {
        super(path, "FlagsConfig");
        gsonType = new TypeToken<List<Wrapper>>() {}.getType();
    }

    @Override
    public void create(List<Wrapper> items) {
        for (FlagType type : FlagType.values()) {
            items.add(new Wrapper(type, type.getDefaultValue(), type.canTownsModify()));
        }

        super.create(items);
    }

    @Override
    public List<Wrapper> read() {
        List<Wrapper> items = super.read();

        for(Wrapper item : items) {
            item.flagType.setDefaultValue(item.defaultState);
            item.flagType.setModifiableForTowns(item.isAllowedInTowns);
        }

        return items;
    }

    @Override
    public boolean validate(List<Wrapper> items) {
        boolean ok, isValid = true;

        for(Iterator<Wrapper> it = items.iterator(); it.hasNext(); ) {
            Wrapper item = it.next();
            if(item.flagType == null) {
                MyEssentialsCore.instance.LOG.error("Found a type of flag that does not exist. Removing...");
                it.remove();
                isValid = false;
                continue;
            }
            if(!item.flagType.getType().isAssignableFrom(item.defaultState.getClass())) {
                MyTown.instance.LOG.error("The default value for the flag is of invalid type for flag " + item.flagType.toString() + "! Needed " + item.flagType.getType().getSimpleName() + " Removing...");
                it.remove();
                isValid = false;
            }
        }

        for(FlagType type : FlagType.values()) {
            ok = false;
            for(Wrapper w : items) {
                if(w.flagType == type)
                    ok = true;
            }
            if(!ok) {
                items.add(new Wrapper(type, type.getDefaultValue(), type.canTownsModify()));
                MyTown.instance.LOG.error("Flag config is missing (or is an invalid entry) {} flag! Adding with default settings...", type.toString());
                isValid = false;
            }
        }
        return isValid;
    }

    /**
     * Wraps around a flagType object.
     */
    public class Wrapper {
        public final FlagType flagType;
        public final Object defaultState;
        public final boolean isAllowedInTowns;

        public Wrapper(FlagType flagType, Object defaultState, boolean isAllowedInTowns) {
            this.flagType = flagType;
            this.defaultState = defaultState;
            this.isAllowedInTowns = isAllowedInTowns;
        }
    }
}
