package mytown.config.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import myessentials.json.JSONConfig;
import mytown.MyTown;
import mytown.entities.Wild;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

/**
 * Wilderness flags
 */
public class WildPermsConfig extends JSONConfig<Flag> implements JsonSerializer<Flag>, JsonDeserializer<Flag> {

    public WildPermsConfig(String path) {
        super(path, "WildPermsConfig");
        gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Flag.class, this).create();
        gsonType = new TypeToken<List<Flag>>() {}.getType();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void create(List<Flag> items) {
        for (FlagType type : FlagType.values()) {
            if (type.isWildPerm) {
                items.add(new Flag(type, type.defaultWildValue));
            }
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

    @Override
    public JsonElement serialize(Flag flag, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("flagType", flag.flagType.name);
        json.addProperty("value", flag.flagType.serializeValue(flag.value));
        return json;
    }

    @Override
    public Flag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        FlagType flagType = FlagType.valueOf(jsonObject.get("flagType").getAsString());
        return new Flag(flagType, jsonObject.get("value").getAsString());
    }


}
