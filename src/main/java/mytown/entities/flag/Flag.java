package mytown.entities.flag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import myessentials.Localization;
import mytown.MyTown;
import myessentials.utils.ColorUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Type;

public abstract class Flag<T> implements Comparable<Flag<T>>{

    public static final String DESCRIPTION_KEY = "mytown.flag.";

    protected String name;
    protected T value;
    protected Gson gson;
    protected Type gsonType;

    public Flag(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public String serializeValue() {
        return gson.toJson(value, gsonType);
    }

    public abstract boolean setValue(String str);

    public T getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Flag<T> other) {
        return name.compareTo(other.getName());
    }

    @Override
    public String toString() {
        return toString(ColorUtils.colorValueVar);
    }

    public String toString(String valueColor) {
        String description = MyTown.instance.LOCAL.getLocalization(DESCRIPTION_KEY + name);

        return String.format(ColorUtils.colorFlag + "%s" + ColorUtils.colorComma + "[" + valueColor+ "%s" + ColorUtils.colorComma + "]:" + ColorUtils.colorComma + " %s", name, value.toString(), description);
    }
}
