package mytown.entities.flag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mytown.proxies.LocalizationProxy;
import net.minecraft.util.EnumChatFormatting;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by AfterWind on 8/26/2014.
 * Another attempt at the dreaded town flags :P
 */
public class Flag<T> {

    private String name;
    private String descriptionKey;
    private T value;

    public Flag(String name, String localizedDescription, T defaultValue) {
        this.name = name;
        this.descriptionKey = localizedDescription;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getLocalizedDescription() {
        return LocalizationProxy.getLocalization().getLocalization(descriptionKey);
    }

    public T getValue() {
        return value;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public String serializeValue() {
        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<T>() {}.getType();
        return gson.toJson(value, type);
    }

    public String valueToString() {
        if(value instanceof String)
            return (String)value;
        else if(value instanceof Integer || value instanceof Boolean || value instanceof Float || value instanceof Character) {
            return String.valueOf(value);
        } else {
            // TODO: not sure if this will fully work
            return value.toString();
        }
    }

    @SuppressWarnings("unchecked")
    public boolean setValueFromString(String str) {
        try {
            if (value instanceof String) {
                value = (T) str;
            } else if (value instanceof Integer) {
                value = (T) (Integer) Integer.parseInt(str); // double cast... lol
            } else if (value instanceof Boolean) {
                value = (T) (Boolean) Boolean.parseBoolean(str);
            } else if (value instanceof Float) {
                value = (T) (Float) Float.parseFloat(str);
            } else if (value instanceof Character) {
                value = (T) (Character) str.charAt(0);
            }
            return true;
        } catch (ClassCastException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    This is so pointless it makes my eyes bleed
    I'll still keep it though lol

    public T deserializeValue(String jsonValue) {
        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<T>() {}.getType();
        return gson.fromJson(jsonValue, type);
    }
    */

    @Override
    public String toString() {
        return String.format(EnumChatFormatting.GRAY + "%s" + EnumChatFormatting.WHITE + "[" + EnumChatFormatting.GREEN + "%s" + EnumChatFormatting.WHITE + "]:" + EnumChatFormatting.GRAY + " %s", name, valueToString(), getLocalizedDescription());
    }

    /**
     * In  this map you can add new flag types and get the type of each flag by its name.
     * Not exactly sure if it's useful enough
     */
    public static Map<String, Class> flagValueTypes = new HashMap<String, Class>();



}
