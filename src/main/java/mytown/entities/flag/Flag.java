package mytown.entities.flag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import myessentials.utils.ColorUtils;

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
        return toString(ColorUtils.colorValueVar);
    }

    public String toString(String valueColor) {
        String description = flagType.getLocalizedDescription();


        return String.format(ColorUtils.colorFlag + "%s" + ColorUtils.colorComma + "[" + valueColor+ "%s" + ColorUtils.colorComma + "]:" + ColorUtils.colorComma + " %s", flagType.name.toLowerCase(), value.toString(), description);
    }

    @Override
    public int compareTo(Flag other) {
        return flagType.compareTo(other.flagType);
    }
}
