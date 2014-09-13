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

    private String name;
    private T value;


    public Flag(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getLocalizedDescription() {
        return LocalizationProxy.getLocalization().getLocalization(descriptionKeys.get(this.name));
    }

    public String getDescriptionKey() {
        return descriptionKeys.get(this.name);
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
    public boolean isValueValid(T val) {
        List<T> valids = (List<T>)validValues.get(name);
        if(valids == null)
            return true; // There are no limitations
        else {
            for(T t : valids) {
                if(val.equals(t))
                    return true;
            }
        }
        return false;
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
        if(isValueValid(val)) {
            value = val;
            return true;
        }
        return false;
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

    public static List<String> flagsForWhitelist = new ArrayList<String>();

    /**
     * Map for keeping description keys, since all flags have exactly the same description.
     */
    public static Map<String, String> descriptionKeys = new HashMap<String, String>();

    /**
     * Map for putting restricted values to the flags... If key is not found then it has no restrictions
     */
    public static Map<String, List<?>> validValues = new HashMap<String, List<?>>();

    /**
     * Initialize all flags
     *
     */
    public static void initFlags() {
        flagValueTypes.put("enter", Boolean.class);
        flagValueTypes.put("breakBlocks", Boolean.class);
        flagValueTypes.put("accessBlocks", Boolean.class);
        flagValueTypes.put("placeBlocks", Boolean.class);
        flagValueTypes.put("pickupItems", Boolean.class);
        flagValueTypes.put("explosions", Boolean.class);
        flagValueTypes.put("mobs", String.class);
        flagValueTypes.put("attackEntities", Boolean.class);
        flagValueTypes.put("useItems", Boolean.class);
        flagValueTypes.put("activateBlocks", Boolean.class);

        flagsForWhitelist.add("breakBlocks");
        flagsForWhitelist.add("accessBlocks");
        flagsForWhitelist.add("activateBlocks");

        for(String s : flagValueTypes.keySet()) {
           descriptionKeys.put(s, "mytown.flag." + s); // Because I'm lazy
        }

        List<String> mobsRestrictedValues = new ArrayList<String>();
        mobsRestrictedValues.add("all");
        mobsRestrictedValues.add("none");
        mobsRestrictedValues.add("hostiles");

        validValues.put("mobs", mobsRestrictedValues);
    }
}
