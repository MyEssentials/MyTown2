package mytown.entities.flag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mytown.MyTown;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Type;

/**
 * Another attempt at the dreaded town flags :P
 */
public class Flag<T> {

    private FlagType flagType;
    private T value;


    public Flag(FlagType flagType, T defaultValue) {
        this.flagType = flagType;
        this.value = defaultValue;
    }

    /**
     * Serializes the value in a JSON String so that it can be saved to database safely
     */
    public String serializeValue() {
        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<T>() { }.getType();
        return gson.toJson(value, type);
    }

    /**
     * Gets the String equivalent of the value
     */
    public String valueToString() {
        if (value instanceof String)
            return (String) value;
        else if (value instanceof Integer || value instanceof Boolean || value instanceof Float || value instanceof Character) {
            return String.valueOf(value);
        } else {
            return value.toString();
        }
    }

    /**
     * Gets the value from a JSON String.
     */
    @SuppressWarnings("unchecked")
    public T getValueFromString(String str) {
        try {
            if (value instanceof String) {
                return (T) str;
            } else if (value instanceof Integer) {
                return (T) (Integer) Integer.parseInt(str); // double cast... lol
            } else if (value instanceof Boolean) {
                // Extra check since any String that is not "true" gets converted to false
                if ("true".equals(str) || "false".equals(str)) {
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
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * Sets the value of the flag from a String
     */
    public boolean setValueFromString(String str) {
        T val = getValueFromString(str);
        if (val == null)
            return false;
        if (flagType.isValueAllowed(val)) {
            value = val;
            return true;
        }
        return false;
    }


    public T getValue() {
        return value;
    }

    public FlagType getFlagType() {
        return flagType;
    }
}
