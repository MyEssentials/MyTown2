package mytown.entities.flag;

import com.google.gson.*;
import myessentials.utils.ColorUtils;
import mytown.api.container.FlagsContainer;

import java.lang.reflect.Type;

public class Flag<T> implements Comparable<Flag>{
    protected Gson gson = new GsonBuilder().create();

    public T value;
    public boolean configurable = true;
    public final FlagType<T> flagType;

    public Flag(FlagType<T> flagType, String serializedValue) {
        this.flagType = flagType;
        this.value = gson.fromJson(serializedValue, flagType.type);
    }

    public Flag(FlagType<T> flagType, T value) {
        this.flagType = flagType;
        this.value = value;
    }

    public boolean setValue(String value) {
        if(flagType.type == Boolean.class) {
            this.value = (T)Boolean.valueOf(value);
            return true;
        } else if(flagType.type == String.class) {
            this.value = (T)value;
            return true;
        } else if(flagType.type == Integer.class) {
            this.value = (T)Integer.valueOf(value);
            return true;
        } else if(flagType.type == Float.class) {
            this.value = (T)Float.valueOf(value);
            return true;
        }
        return false;
    }


    @Override
    public String toString() {
        return toString(ColorUtils.colorConfigurableFlag);
    }

    public String toString(String nameColor) {
        String description = flagType.getLocalizedDescription();
        String valueColor = ColorUtils.colorValueRegular;
        if(value instanceof Boolean) {
            valueColor = (Boolean)value ? ColorUtils.colorValueRegular : ColorUtils.colorValueFalse;
        }
        return String.format(nameColor + "%s" + ColorUtils.colorComma + "[" + valueColor + "%s" + ColorUtils.colorComma + "]:" + ColorUtils.colorComma + " %s", flagType.name.toLowerCase(), value.toString(), description);
    }

    @Override
    public int compareTo(Flag other) {
        return flagType.compareTo(other.flagType);
    }

    @SuppressWarnings("unchecked")
    public static class Serializer implements JsonSerializer<Flag>, JsonDeserializer<Flag> {


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
            Flag flag = new Flag(flagType, jsonObject.get("value").getAsString());

            return flag;
        }
    }
}
