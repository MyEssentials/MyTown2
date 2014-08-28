package mytown.entities.flag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mytown.proxies.LocalizationProxy;

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

    /*
    This is so pointless it makes my eyes bleed
    I'll still keep it though lol

    public T deserializeValue(String jsonValue) {
        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<T>() {}.getType();
        return gson.fromJson(jsonValue, type);
    }
    */

    /**
     * In  this map you can add new flag types and get the type of each flag by its name.
     * Not exactly sure if it's useful enough
     */
    public static Map<String, Class> flagValueTypes = new HashMap<String, Class>();



}
