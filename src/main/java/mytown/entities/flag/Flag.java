package mytown.entities.flag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mytown.MyTown;
import mytown.proxies.LocalizationProxy;
import net.minecraft.util.EnumChatFormatting;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 8/26/2014.
 * Another attempt at the dreaded town flags :P
 */
public class Flag<T> {

    public FlagType flagType;
    private T value;


    public Flag(FlagType flagType, T defaultValue) {
        this.flagType = flagType;
        this.value = defaultValue;
    }

    public T getValue() {
        return value;
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
    public T getValueFromString(String str) {
        try {
            if (value instanceof String) {
                return (T) str;
            } else if (value instanceof Integer) {
                return (T) (Integer) Integer.parseInt(str); // double cast... lol
            } else if (value instanceof Boolean) {
                // Extra check since any String that is not "true" gets converted to false
                if(str.equals("true") || str.equals("false")) {
                    return (T) (Boolean) Boolean.parseBoolean(str);
                } else {
                    return null;
                }
            } else if (value instanceof Float) {
                return (T) (Float) Float.parseFloat(str);
            } else if (value instanceof Character) {
                return (T) (Character) str.charAt(0);
            } else {
                return null;
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setValueFromString(String str) {
        T val = getValueFromString(str);
        if(val == null)
            return false;
        if(flagType.isValueAllowed(val)) {
            value = val;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format(EnumChatFormatting.GRAY + "%s" + EnumChatFormatting.WHITE + "[" + EnumChatFormatting.GREEN + "%s" + EnumChatFormatting.WHITE + "]:" + EnumChatFormatting.GRAY + " %s", flagType.toString(), valueToString(), flagType.getLocalizedDescription());
    }
}
