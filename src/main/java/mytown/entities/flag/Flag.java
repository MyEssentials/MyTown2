package mytown.entities.flag;

import com.google.gson.*;
import myessentials.json.SerializerTemplate;
import myessentials.utils.ColorUtils;
import mytown.entities.Town;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

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
    public static class Serializer extends SerializerTemplate<Flag> {

        @Override
        public void register(GsonBuilder builder) {
            builder.registerTypeAdapter(Flag.class, this);
            new FlagType.Serializer().register(builder);
        }

        @Override
        public JsonElement serialize(Flag flag, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.add("flagType", context.serialize(flag.flagType));
            json.addProperty("value", flag.flagType.serializeValue(flag.value));

            return json;
        }

        @Override
        public Flag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            FlagType flagType = context.deserialize(jsonObject.get("flagType"), FlagType.class);
            Flag flag = new Flag(flagType, jsonObject.get("value").getAsString());

            return flag;
        }
    }

    public static class Container extends ArrayList<Flag> {

        public boolean contains(FlagType<?> flagType) {
            for (Flag flag : this) {
                if (flag.flagType == flagType) {
                    return true;
                }
            }
            return false;
        }

        public <T> Flag<T> get(FlagType<T> flagType) {
            for (Flag flag : this) {
                if (flag.flagType == flagType) {
                    return flag;
                }
            }
            return null;
        }

        public void remove(FlagType<?> flagType) {
            for (Iterator<Flag> it = iterator(); it.hasNext(); ) {
                if (it.next().flagType.equals(flagType)) {
                    it.remove();
                }
            }
        }

        public <T> T getValue(FlagType<T> flagType) {
            for (Flag flag : this) {
                if (flag.flagType == flagType) {
                    return (T)flag.value;
                }
            }
            return null;
        }

        public String toStringForTowns() {
            String formattedFlagList = "";

            for (Flag flag : this) {
                if (flag.flagType.configurable) {
                    if (!formattedFlagList.equals("")) {
                        formattedFlagList += "\\n";
                    }
                    formattedFlagList += flag.toString(ColorUtils.colorConfigurableFlag);
                }
            }

            String unconfigurableFlags = "";
            for(FlagType flagType : FlagType.values()) {
                if(!flagType.configurable) {
                    unconfigurableFlags += "\\n" + (new Flag(flagType, flagType.defaultValue)).toString(ColorUtils.colorUnconfigurableFlag);
                }
            }

            formattedFlagList += unconfigurableFlags;

            return formattedFlagList;
        }

        public String toStringForPlot(Town town) {
            String formattedFlagList = "";

            for (Flag flag : this) {
                if (flag.flagType.configurable) {
                    if (!formattedFlagList.equals("")) {
                        formattedFlagList += "\\n";
                    }
                    formattedFlagList += flag.toString(ColorUtils.colorConfigurableFlag);
                }
            }

            String unconfigurableFlags = "";
            for(FlagType flagType : FlagType.values()) {
                if(!flagType.configurable) {
                    Object value = town.flagsContainer.contains(flagType) ? town.flagsContainer.getValue(flagType) : flagType.defaultValue;
                    unconfigurableFlags += "\\n" + (new Flag(flagType, value).toString(ColorUtils.colorUnconfigurableFlag));
                }
            }

            formattedFlagList += unconfigurableFlags;
            return formattedFlagList;
        }

        public String toStringForWild() {
            String formattedFlagList = "";

            for (Flag flag : this) {
                if (!formattedFlagList.equals("")) {
                    formattedFlagList += "\\n";
                }
                formattedFlagList += flag.toString();
            }

            return formattedFlagList;
        }
    }
}
