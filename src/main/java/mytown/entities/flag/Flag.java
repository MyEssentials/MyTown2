package mytown.entities.flag;

import com.google.gson.*;

import myessentials.chat.api.ChatFormat;
import myessentials.chat.api.IChatFormat;
import myessentials.json.api.SerializerTemplate;
import myessentials.utils.ColorUtils;
import mytown.MyTown;
import mytown.entities.Town;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

public class Flag<T> extends ChatFormat implements Comparable<Flag> {
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

    public boolean toggle() {
        if (flagType.type != Boolean.class) {
            return false;
        }
        this.value = (T)Boolean.valueOf(!((Boolean)this.value));
        return true;
    }

    @Override
    public String toString() {
        return toChatMessage().getUnformattedText();
    }

    @Override
    public int compareTo(Flag other) {
        return flagType.compareTo(other.flagType);
    }

    @Override
    public IChatComponent toChatMessage(boolean shortened) {
        IChatComponent description = MyTown.instance.LOCAL.getLocalization(flagType.getDescriptionKey());
        return MyTown.instance.LOCAL.getLocalization("mytown.format.flag", flagType.name.toLowerCase(), value.toString(), description);
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

    public static class Container extends ArrayList<Flag> implements IChatFormat {

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

        @Override
        public IChatComponent toChatMessage(boolean shortened) {
            IChatComponent result = new ChatComponentText("");
            for (Flag flag : this) {
                result.appendSibling(flag.toChatMessage());
                result.appendSibling(new ChatComponentText("\n"));
            }
            return result;
        }

        @Override
        public IChatComponent toChatMessage() {
            return toChatMessage(false);
        }
    }
}
